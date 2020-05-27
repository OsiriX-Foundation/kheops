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
import online.kheops.auth_server.webhook.NewSeriesWebhook;
import online.kheops.auth_server.webhook.WebhookAsyncRequest;
import online.kheops.auth_server.webhook.WebhookRequestId;
import online.kheops.auth_server.webhook.WebhookType;

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

            List<Webhook> webhookList = em.createNamedQuery("Webhook.findAllEnabledAndForNewSeriesByStudyUID", Webhook.class)
                    .setParameter(StudyInstanceUID, studyInstanceUID)
                    .getResultList();

            String albumId = null;
            if(albumIdParam != null || kheopsPrincipal.getScope() == ScopeType.ALBUM) {

                String albumIdScope;
                try {
                    albumIdScope = kheopsPrincipal.getAlbumID();
                } catch (NotAlbumScopeTypeException e) {
                    albumIdScope = null;
                }

                if (albumIdParam != null && albumIdScope != null && albumIdScope.compareTo(albumIdParam) == 0) {
                    albumId = albumIdParam;
                } else if (albumIdParam != null && albumIdScope == null) {
                    albumId = albumIdParam;
                } else if (albumIdParam == null && albumIdScope != null) {
                    albumId = albumIdScope;
                } else if (albumIdParam != null && albumIdScope != null && albumIdScope.compareTo(albumIdParam) != 0) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(ALBUM_NOT_FOUND)
                            .detail("The album does not exist or you don't have access")
                            .build();
                    return Response.status(UNAUTHORIZED).entity(errorResponse).build();
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

                for (Webhook webhook : webhookList) {

                    final List<Series> seriesInWebhook = new ArrayList<>();
                    final NewSeriesWebhook newSeriesWebhook;
                    if (albumId != null && webhook.getAlbum().getId().compareTo(albumId) == 0) {
                        newSeriesWebhook = new NewSeriesWebhook(albumId, targetAlbumUser, context.getInitParameter(HOST_ROOT_PARAMETER), false);
                        for (Series series : seriesListWebhook) {
                            newSeriesWebhook.addSeries(series);
                            seriesInWebhook.add(series);
                        }
                        if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                            final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                            newSeriesWebhook.setCapabilityToken(capability);
                        } else if (kheopsPrincipal.getClientId().isPresent()) {
                            ReportProvider reportProvider = getReportProvider(kheopsPrincipal.getClientId().orElseThrow(IllegalStateException::new));
                            newSeriesWebhook.setReportProvider(reportProvider);
                        }
                    } else {
                        newSeriesWebhook = new NewSeriesWebhook(webhook.getAlbum().getId(), callingUser, context.getInitParameter(HOST_ROOT_PARAMETER), false);
                        List<Series> seriesInStudy = em.createNamedQuery("Series.findAllByStudyUIDFromAlbum", Series.class)
                                .setParameter(StudyInstanceUID, studyInstanceUID)
                                .setParameter("album", webhook.getAlbum())
                                .getResultList();

                        for (Series series : seriesListWebhook) {
                            if (seriesInStudy.contains(series)) {
                                newSeriesWebhook.addSeries(series);
                                seriesInWebhook.add(series);
                            }
                        }
                    }
                    newSeriesWebhook.setFetch();

                    if (!seriesInWebhook.isEmpty()) {
                        final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), false, WebhookType.NEW_SERIES, webhook);
                        em.persist(webhookTrigger);
                        for (Series series : seriesInWebhook) {
                            webhookTrigger.addSeries(series);
                        }
                        webhookAsyncRequests.add(new WebhookAsyncRequest(webhook, newSeriesWebhook, webhookTrigger));
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
