package online.kheops.auth_server.resource;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UIDValidator;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.fetch.FetchSeriesMetadata;
import online.kheops.auth_server.fetch.Fetcher;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.webhook.delayed_webhook.DelayedWebhook;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder.*;
import online.kheops.auth_server.webhook.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.report_provider.ReportProviderQueries.getReportProviderWithClientId;
import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.ErrorResponse.Message.*;

@Path("/")
public class FetchResource {

    @Context
    private SecurityContext securityContext;

    @Context
    private ServletContext context;

    @Inject
    DelayedWebhook delayedWebhook;

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
            throws AlbumNotFoundException, SeriesNotFoundException {

        if(seriesInstanceUIDList == null || seriesInstanceUIDList.isEmpty()) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("'" + SeriesInstanceUID + "' formparam is empty")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final Map<Series, FetchSeriesMetadata> seriesNumberOfInstance = Fetcher.fetchStudy(studyInstanceUID);
        final KheopsLogBuilder kheopsLogBuilder = ((KheopsPrincipal) securityContext.getUserPrincipal()).getKheopsLogBuilder()
                .study(studyInstanceUID)
                .action(ActionType.FETCH);
        seriesInstanceUIDList.forEach(kheopsLogBuilder::series);
        kheopsLogBuilder.log();

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
            final Source source = new Source(callingUser);
            final Album targetAlbum;
            if (albumId != null) {
                targetAlbum = getAlbum(albumId, em);
            } else {
                targetAlbum = kheopsPrincipal.getUser().getInbox();
            }


            kheopsPrincipal.getCapability().ifPresent(source::setCapabilityToken);
            kheopsPrincipal.getClientId().ifPresent(clienrtId -> source.setReportProviderClientId(getReportProviderWithClientId(clienrtId, em)));
            for(String seriesUID : seriesInstanceUIDList) {

                final Series series = getSeries(seriesUID, em);
                delayedWebhook.addWebhookData(series.getStudy(), series, targetAlbum, albumId==null,
                        seriesNumberOfInstance.get(series).getNumberOfNewInstances(), source, false, false);
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return Response.ok().build();
    }
}
