package online.kheops.auth_server.resource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.assertion.*;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.report_provider.*;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.servlet.ServletContext;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.report_provider.ReportProviderQueries.getReportProviderWithClientId;
import static online.kheops.auth_server.report_provider.ReportProviders.*;
import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.Consts.QUERY_PARAMETER_OFFSET;
import static online.kheops.auth_server.util.HttpHeaders.X_TOTAL_COUNT;
import static online.kheops.auth_server.util.Tools.checkValidUID;


@Path("/")
public class ReportProviderResource {
    private static final Logger LOG = Logger.getLogger(ReportProviderResource.class.getName());

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    @Context
    ServletContext context;

    @POST
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.MANAGE_DICOM_SR)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/reportproviders")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newReportProvider(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                            @FormParam("url") final String url,
                            @FormParam("name") final String name) {

        if (name == null || name.isEmpty()) {
            return Response.status(BAD_REQUEST).entity("'name' formparam must be set").build();
        }

        if (url == null || url.isEmpty()) {
            return Response.status(BAD_REQUEST).entity("'url' formparam must be set").build();
        } else if ( !isValidConfigUrl(url)) {
            return Response.status(BAD_REQUEST).entity("'url' formparam is not valid").build();
        }

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        final ReportProviderResponse dicomSrResponse;
        try {
            dicomSrResponse = ReportProviders.newReportProvider(kheopsPrincipal.getUser(), albumId, name, url);
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).build();
        }

        return Response.status(CREATED).entity(dicomSrResponse).build();
    }


    private static final String UNKNOWN_BEARER_URN = "urn:x-kheops:params:oauth:grant-type:unknown-bearer";

    @POST
    @Path("report")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newReport(@FormParam("access_token") final String accessToken,
                              @FormParam("clientId") final String clientId,
                              @FormParam(StudyInstanceUID) List<String> studyInstanceUID) {//Edit UidValidator for work with @FormParam

        if (studyInstanceUID == null || studyInstanceUID.isEmpty()) {
            return Response.status(BAD_REQUEST).entity(StudyInstanceUID +" param must be set").build();
        }

        for (String uid: studyInstanceUID) {
            if (!checkValidUID(uid)) {
                return Response.status(BAD_REQUEST).entity(uid + "is not a valid uid").build();
            }
        }

        final Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(accessToken, UNKNOWN_BEARER_URN);
        } catch (UnknownGrantTypeException e) {
            LOG.log(Level.WARNING, "Unknown grant type", e);
            return Response.status(BAD_REQUEST).entity("error with the grant_type param").build();
        } catch (BadAssertionException e) {
            LOG.log(Level.WARNING, "Error validating a token", e);
            return Response.status(UNAUTHORIZED).entity("error with the access_token").build();
        } catch (DownloadKeyException e) {
            LOG.log(Level.SEVERE, "Error downloading the public key", e);
            return Response.status(BAD_GATEWAY).entity("Error downloading the public key").build();
        }

        try {
            getOrCreateUser(assertion.getSub());
        } catch (UserNotFoundException e) {
            LOG.log(Level.WARNING, "User not found", e);
            return Response.status(UNAUTHORIZED).build();
        }

        //vérifier la permission de créer report_provider_code (user) pas capability token
        if (! (assertion.getTokenType() == Assertion.TokenType.KEYCLOAK_TOKEN  ||
                assertion.getTokenType() == Assertion.TokenType.SUPER_USER_TOKEN)) {

            return Response.status(FORBIDDEN).build();
        }

        final User callingUser;
        try {
            callingUser = getOrCreateUser(assertion.getSub());
        } catch (UserNotFoundException e) {
            LOG.log(Level.WARNING, "User not found", e);
            return Response.status(UNAUTHORIZED).entity("User not found").build();
        }

        //vérifier l'acces a l'album
        final KheopsPrincipalInterface principal = assertion.newPrincipal(callingUser);

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        final ReportProvider reportProvider;
        final String albumId;
        try {
            tx.begin();
            reportProvider = getReportProviderWithClientId(clientId, em);
            albumId = reportProvider.getAlbum().getId();
        } catch (NoResultException e){
            return Response.status(NOT_FOUND).entity("Report provider with clientId: " + clientId + "not found").build();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        final Album album;
        try {
            if (! (principal.hasUserAccess() && principal.hasAlbumAccess(albumId))) {
                return Response.status(FORBIDDEN).build();
            }

            album = getAlbum(albumId);
            for (String uid : studyInstanceUID) {
                if (!canAccessStudy(album, uid)) {
                    return Response.status(NOT_FOUND).entity("Study uid: " + uid + "not found").build();
                }
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        //générer le jwt
        final String authSecret = context.getInitParameter("online.kheops.auth.hmacsecret");
        final Algorithm algorithmHMAC;
        try {
            algorithmHMAC = Algorithm.HMAC256(authSecret);
        } catch (UnsupportedEncodingException e) {
            LOG.log(Level.SEVERE, "online.kheops.auth.hmacsecret is not a valid HMAC secret", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        final JWTCreator.Builder jwtBuilder = JWT.create()
                .withExpiresAt(Date.from(Instant.now().plus(2, ChronoUnit.MINUTES)))
                .withNotBefore(new Date())
                .withArrayClaim("study_uids", studyInstanceUID.toArray(new String[0]))
                .withClaim("client_id", reportProvider.getClientId())
                .withSubject(assertion.getSub())
                .withClaim("type", "report_provider_code");

        final String token = jwtBuilder.sign(algorithmHMAC);

        try {
            final String kheopsConfigUrl = context.getInitParameter("online.kheops.root.uri") + "/api/reportproviders/" + clientId + "/configuration";
            final String StandardCharsetsUTF8 = java.nio.charset.StandardCharsets.UTF_8.toString();

            final String confUri = URLEncoder.encode(kheopsConfigUrl, StandardCharsetsUTF8);
            final UriBuilder reportProviderUrlBuilder = UriBuilder.fromUri(getRedirectUri(reportProvider))
                    .queryParam("code", token)
                    .queryParam("conf_uri", confUri);

            for (String uid: studyInstanceUID) {
                reportProviderUrlBuilder.queryParam(StudyInstanceUID, URLEncoder.encode(uid, StandardCharsetsUTF8));
            }

            final String reportProviderUrl = reportProviderUrlBuilder.toString();

            return Response.status(FOUND).header("Location", reportProviderUrl).build();
        } catch (ReportProviderUriNotValidException e) {
            return Response.status(BAD_REQUEST ).entity(e.getMessage()).build();
        } catch (UnsupportedEncodingException e) {
            return Response.status(FORBIDDEN).entity("ERROR").build();
        }
    }

    @GET
    @Path("reportproviders/{clientId:"+ ClientId.CLIENT_ID_PATTERN+"}/configuration")
    @Produces(MediaType.APPLICATION_JSON)
    public Response configuration(@SuppressWarnings("RSReferenceInspection") @PathParam("clientId") String clientId) {

        final ConfigurationResponse configurationResponse;
        try {
            configurationResponse = new ConfigurationResponse(clientId, context.getInitParameter("online.kheops.root.uri"));
        } catch (ClientIdNotFoundException e) {
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        }
        return  Response.status(OK).entity(configurationResponse).build();
    }

    @GET
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.GET_DICOM_SR)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/reportproviders")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllReportProviders(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                          @QueryParam(QUERY_PARAMETER_LIMIT) @Min(0) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                                          @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer offset) {

        final PairListXTotalCount<ReportProviderResponse> pair;

        pair = ReportProviders.getReportProviders(albumId, limit, offset);

        final GenericEntity<List<ReportProviderResponse>> genericReportProvidersResponsesList = new GenericEntity<List<ReportProviderResponse>>(pair.getAttributesList()) {};
        return  Response.status(OK).entity(genericReportProvidersResponsesList).header(X_TOTAL_COUNT, pair.getXTotalCount()).build();
    }

    @GET
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.GET_DICOM_SR)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/reportproviders/{clientId:"+ ClientId.CLIENT_ID_PATTERN+"}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportProviders(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                       @SuppressWarnings("RSReferenceInspection") @PathParam("clientId") String clientId) {

        final ReportProviderResponse reportProvider;
        try {
            reportProvider = getReportProvider(albumId, clientId);
        } catch (ClientIdNotFoundException e) {
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        }

        return  Response.status(OK).entity(reportProvider).build();
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.MANAGE_DICOM_SR)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/reportproviders/{clientId:"+ ClientId.CLIENT_ID_PATTERN+"}")
    public Response deleteReportProviders(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                          @SuppressWarnings("RSReferenceInspection") @PathParam("clientId") String clientId) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final User callingUser = kheopsPrincipal.getUser();
        try {
            deleteReportProvider(callingUser, albumId, clientId);
        } catch (ClientIdNotFoundException e) {
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return  Response.status(NO_CONTENT).build();
    }

    @PATCH
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.MANAGE_DICOM_SR)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/reportproviders/{clientId:"+ ClientId.CLIENT_ID_PATTERN+"}")
    public Response editReportProviders(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                        @SuppressWarnings("RSReferenceInspection") @PathParam("clientId") String clientId,
                                        @FormParam("url") final String url,
                                        @FormParam("name") final String name,
                                        @FormParam("new_client_id") final boolean newClientId) {


        if(!(url == null || url.isEmpty() )) {
            if(!isValidConfigUrl(url)) {
                return Response.status(BAD_REQUEST).entity("url not valid").build();
            }
        }

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final User callingUser = kheopsPrincipal.getUser();

        final ReportProviderResponse reportProvider;
        try {
            reportProvider = editReportProvider(callingUser, albumId, clientId, url, name, newClientId);
        } catch (ClientIdNotFoundException e) {
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return  Response.status(OK).entity(reportProvider).build();
    }

    @POST
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("reportproviders/testuri")
    public Response testUri(@FormParam("url") final String url) {

        if (url == null || url.isEmpty() || !isValidConfigUrl(url)) {
            return Response.status(BAD_REQUEST).entity("url not valid").build();
        }
        return  Response.status(OK).build();
    }
}
