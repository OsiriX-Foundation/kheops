package online.kheops.auth_server.resource;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UIDValidator;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.fetch.Fetcher;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.report_provider.ClientIdNotFoundException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.util.KheopsLogBuilder.*;
import online.kheops.auth_server.webhook.NewSeriesWebhook;
import online.kheops.auth_server.webhook.WebhookAsyncRequest;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContext;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.getAlbumUser;
import static online.kheops.auth_server.report_provider.ReportProviders.getReportProvider;
import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.study.Studies.getOrCreateStudy;
import static online.kheops.auth_server.util.Consts.*;

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
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/fetch")
    public Response getStudies(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                               @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID,
                               @FormParam("album") String albumId)
            throws AlbumNotFoundException, UserNotMemberException, ClientIdNotFoundException, SeriesNotFoundException {
        Fetcher.fetchStudy(studyInstanceUID);
        ((KheopsPrincipal) securityContext.getUserPrincipal()).getKheopsLogBuilder()
                .study(studyInstanceUID)
                .series(seriesInstanceUID)
                .action(ActionType.FETCH)
                .log();



        KheopsPrincipal kheopsPrincipal = (KheopsPrincipal) securityContext.getUserPrincipal();

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
            final Album targetAlbum = getAlbum(albumId, em);
            final AlbumUser targetAlbumUser = getAlbumUser(targetAlbum, callingUser, em);
            final Series series = getSeries(studyInstanceUID, seriesInstanceUID, em);
            final Study study = series.getStudy();

            final NewSeriesWebhook newSeriesWebhook = new NewSeriesWebhook(albumId, targetAlbumUser, series, context.getInitParameter(HOST_ROOT_PARAMETER),false);

            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                newSeriesWebhook.setCapabilityToken(capability);
            } else if(kheopsPrincipal.getClientId().isPresent()) {
                ReportProvider reportProvider = getReportProvider(kheopsPrincipal.getClientId().orElseThrow(IllegalStateException::new));
                newSeriesWebhook.setReportProvider(reportProvider);
            }

            tx.commit();

            if(series.isPopulated() && study.isPopulated()) {
                for (Webhook webhook : targetAlbum.getWebhooks()) {
                    if (webhook.getNewSeries() && webhook.isEnable()) {
                        new WebhookAsyncRequest(webhook, newSeriesWebhook, false);
                    }
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
