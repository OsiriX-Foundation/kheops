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
import online.kheops.auth_server.webhook.WebhookType;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
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

        String albumId;
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
            } else if (albumIdParam == null && albumIdScope == null) {
                return Response.ok().build();
            } else {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(ALBUM_NOT_FOUND)
                        .detail("The album does not exist or you don't have access")
                        .build();
                return Response.status(UNAUTHORIZED).entity(errorResponse).build();
            }


            final EntityManager em = EntityManagerListener.createEntityManager();
            final EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();

                final User callingUser = em.merge(kheopsPrincipal.getUser());
                final Album targetAlbum = getAlbum(albumId, em);
                final AlbumUser targetAlbumUser = getAlbumUser(targetAlbum, callingUser, em);
                final List<Series> seriesList = new ArrayList<>();
                for (String seriesInstanceUID: seriesInstanceUIDList) {
                    seriesList.add(getSeries(studyInstanceUID, seriesInstanceUID, em));
                }
                final Study study = seriesList.get(0).getStudy();

                final NewSeriesWebhook newSeriesWebhook = new NewSeriesWebhook(albumId, targetAlbumUser, context.getInitParameter(HOST_ROOT_PARAMETER), false);
                newSeriesWebhook.setFetch();

                for (Series series: seriesList){
                    if (series.isPopulated()) {
                        newSeriesWebhook.addSeries(series);
                    }
                }

                if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                    final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                    newSeriesWebhook.setCapabilityToken(capability);
                } else if (kheopsPrincipal.getClientId().isPresent()) {
                    ReportProvider reportProvider = getReportProvider(kheopsPrincipal.getClientId().orElseThrow(IllegalStateException::new));
                    newSeriesWebhook.setReportProvider(reportProvider);
                }

                if (study.isPopulated() && newSeriesWebhook.containSeries()) {
                    for (Webhook webhook : targetAlbum.getWebhooks()) {
                        if (webhook.getNewSeries() && webhook.isEnabled()) {
                            WebhookTrigger webhookTrigger = new WebhookTrigger(false, WebhookType.NEW_SERIES, webhook);
                            em.persist(webhookTrigger);
                            new WebhookAsyncRequest(webhook, newSeriesWebhook, webhookTrigger);
                        }
                    }
                }

                tx.commit();
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }

        }





        return Response.ok().build();
    }
}
