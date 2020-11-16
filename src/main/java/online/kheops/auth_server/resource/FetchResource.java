package online.kheops.auth_server.resource;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UIDValidator;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.fetch.Fetcher;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.report_provider.ClientIdNotFoundException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder.*;
import online.kheops.auth_server.webhook.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.getAlbumUser;
import static online.kheops.auth_server.report_provider.ReportProviders.getReportProvider;
import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.ErrorResponse.Message.*;

@Path("/")
public class FetchResource {

    @Context
    private SecurityContext securityContext;

    @Context
    private ServletContext context;

    @POST
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/fetch")
    public Response getStudies(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID) {
        Fetcher.fetchStudy(studyInstanceUID);
        ((KheopsPrincipal) securityContext.getUserPrincipal()).getKheopsLogBuilder()
                .study(studyInstanceUID)
                .action(ActionType.FETCH)
                .log();
        return Response.ok().build();
    }

    @POST
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/fetch")
    public Response getStudies(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                               @FormParam(SeriesInstanceUID) List<String> seriesInstanceUIDList,
                               @FormParam("album") String albumIdParam)
            throws AlbumNotFoundException, UserNotMemberException, ClientIdNotFoundException, SeriesNotFoundException {
        Fetcher.fetchStudy(studyInstanceUID);
        for (String seriesInstanceUID: seriesInstanceUIDList) {
            ((KheopsPrincipal) securityContext.getUserPrincipal()).getKheopsLogBuilder()
                    .study(studyInstanceUID)
                    .series(seriesInstanceUID)
                    .action(ActionType.FETCH)
                    .log();
        }

        if(seriesInstanceUIDList == null || seriesInstanceUIDList.isEmpty()) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("'" + SeriesInstanceUID + "' formparam is empty")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        KheopsPrincipal kheopsPrincipal = (KheopsPrincipal) securityContext.getUserPrincipal();

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            String albumId = null;
            if(albumIdParam != null || kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                try {
                    final String albumIdScope = kheopsPrincipal.getAlbumID();
                    if (albumIdParam != null && albumIdScope.compareTo(albumIdParam) == 0) {
                        albumId = albumIdParam;
                    } else if (albumIdParam == null) {
                        albumId = albumIdScope;
                    } else if (albumIdParam != null && albumIdScope.compareTo(albumIdParam) != 0) {
                        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                                .message(ALBUM_NOT_FOUND)
                                .detail("The album does not exist or you don't have access")
                                .build();
                        return Response.status(UNAUTHORIZED).entity(errorResponse).build();
                    }
                } catch (NotAlbumScopeTypeException e) {
                    if (albumIdParam != null) {
                        albumId = albumIdParam;
                    }
                }
            }

            final User callingUser = em.merge(kheopsPrincipal.getUser());

            final Album targetAlbum;
            AlbumUser targetAlbumUser = null;
            if (albumId != null) {
                targetAlbum = getAlbum(albumId, em);
                targetAlbumUser = getAlbumUser(targetAlbum, callingUser, em);
            }

            final List<Series> seriesList = new ArrayList<>();
            for (String seriesInstanceUID : seriesInstanceUIDList) {
                seriesList.add(getSeries(studyInstanceUID, seriesInstanceUID, em));
            }
            final Study study = seriesList.get(0).getStudy();

            if(study.isPopulated()) {

                final List<Series> seriesListWebhook = new ArrayList<>();
                for (Series series : seriesList) {
                    if (series.isPopulated()) {
                        seriesListWebhook.add(series);
                    }
                }

                final List<WebhookAsyncRequest> webhookAsyncRequests = new ArrayList<>();

                List<Webhook> webhookList = em.createNamedQuery("Webhook.findAllEnabledAndForNewSeriesByStudyUID", Webhook.class)
                        .setParameter(StudyInstanceUID, studyInstanceUID)
                        .getResultList();

                for (Webhook webhook : webhookList) {

                    final List<Series> seriesInWebhook = new ArrayList<>();
                    final NewSeriesWebhook.Builder newSeriesWebhookBuilder = NewSeriesWebhook.builder();

                    if (albumId != null && webhook.getAlbum().getId().compareTo(albumId) == 0) {
                        newSeriesWebhookBuilder.setStudy(study)
                                .isSent()
                                .setDestination(albumId)
                                .isAutomatedTrigger()
                                .setSource(targetAlbumUser)
                                .setKheopsInstance(context.getInitParameter(HOST_ROOT_PARAMETER));
                        for (Series series : seriesListWebhook) {
                            newSeriesWebhookBuilder.addSeries(series);
                            seriesInWebhook.add(series);
                        }
                        if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                            final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                            newSeriesWebhookBuilder.setCapabilityToken(capability);
                        } else if (kheopsPrincipal.getClientId().isPresent()) {
                            ReportProvider reportProvider = getReportProvider(kheopsPrincipal.getClientId().orElseThrow(IllegalStateException::new));
                            newSeriesWebhookBuilder.setReportProvider(reportProvider);
                        }
                    } else {
                        newSeriesWebhookBuilder
                                .setStudy(study)
                                .isSent()
                                .setDestination(webhook.getAlbum().getId())
                                .isAutomatedTrigger()
                                .setSource(new Source(callingUser));
                        List<Series> seriesInStudy = em.createNamedQuery("Series.findAllByStudyUIDFromAlbum", Series.class)
                                .setParameter(StudyInstanceUID, studyInstanceUID)
                                .setParameter("album", webhook.getAlbum())
                                .getResultList();

                        for (Series series : seriesListWebhook) {
                            if (seriesInStudy.contains(series)) {
                                newSeriesWebhookBuilder.addSeries(series);
                                seriesInWebhook.add(series);
                            }
                        }
                    }
                    newSeriesWebhookBuilder.isUpload();


                    if (!seriesInWebhook.isEmpty()) {
                        final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), false, WebhookType.NEW_SERIES, webhook);
                        em.persist(webhookTrigger);
                        for (Series series : seriesInWebhook) {
                            webhookTrigger.addSeries(series);
                        }
                        webhookAsyncRequests.add(new WebhookAsyncRequest(webhook, newSeriesWebhookBuilder.build(), webhookTrigger));
                    }
                }

                tx.commit();
                for (WebhookAsyncRequest webhookAsyncRequest : webhookAsyncRequests) {
                    webhookAsyncRequest.firstRequest();
                }
            }
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return Response.ok().build();
    }
}
