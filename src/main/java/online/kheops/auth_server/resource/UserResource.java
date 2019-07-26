package online.kheops.auth_server.resource;

import online.kheops.auth_server.album.AlbumResponse;
import online.kheops.auth_server.album.Albums;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.accesstoken.AccessToken;
import online.kheops.auth_server.accesstoken.AccessTokenVerifier;
import online.kheops.auth_server.accesstoken.AccessTokenVerificationException;
import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.keycloak.KeycloakException;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.study.Studies;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponseBuilder;
import online.kheops.auth_server.util.KheopsLogBuilder.*;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.servlet.ServletContext;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlElement;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.accesstoken.AccessToken.TokenType.REPORT_PROVIDER_TOKEN;
import static online.kheops.auth_server.user.AlbumUserPermissions.LIST_USERS;
import static online.kheops.auth_server.util.Consts.*;

@Path("/")
public class UserResource {
    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

    private static class OIDCUserInfo {
        @XmlElement(name = "sub")
        public String sub;
        @XmlElement(name = "name")
        public String name;
        @XmlElement(name = "given_name")
        public String givenName;
        @XmlElement(name = "family_name")
        public String familyName;
        @XmlElement(name = "email")
        public String email;
        @XmlElement(name = "preferred_username")
        public String preferredUsername;

        private static OIDCUserInfo from(Keycloak.UserRepresentation userRepresentation) {
            OIDCUserInfo userInfo = new OIDCUserInfo();
            userInfo.sub = userRepresentation.getId();
            userInfo.name = userRepresentation.getFirstName() + " " + userRepresentation.getLastName();
            userInfo.givenName = userRepresentation.getFirstName();
            userInfo.familyName = userRepresentation.getLastName();
            userInfo.email = userRepresentation.getEmail();
            userInfo.preferredUsername = userRepresentation.getUsername();
            return userInfo;
        }
    }

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    @Context
    private ServletContext servletContext;

    @GET
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(LIST_USERS)
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getUserInfo(@QueryParam("reference") String reference,
                                @QueryParam(ALBUM) String albumId,
                                @QueryParam("studyInstanceUID") @UIDValidator String studyInstanceUID,
                                @QueryParam("search") String search,
                                @QueryParam(QUERY_PARAMETER_LIMIT) @Min(0) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                                @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer offset) {

        final KheopsLogBuilder kheopsLogBuilder = ((KheopsPrincipal)securityContext.getUserPrincipal()).getKheopsLogBuilder();

        if(reference == null && search != null) {
            if (search.length() < 3) {
                return Response.status(BAD_REQUEST).entity("'search' query param must have minimum 3 characters").build();
            }

            kheopsLogBuilder.action(ActionType.USERS_LIST);

            try {
                final Keycloak keycloak = Keycloak.getInstance();
                final List<UserResponseBuilder> result = keycloak.getUsers(search, limit, offset);
                kheopsLogBuilder.log();

                if(result.isEmpty()) {
                    return Response.status(NO_CONTENT).build();
                }

                final GenericEntity<List<UserResponseBuilder>> genericUsersResponsesList = new GenericEntity<List<UserResponseBuilder>>(result) {};
                return Response.status(OK).entity(genericUsersResponsesList).build();
            } catch (KeycloakException e) {
                LOG.log(Level.WARNING, "Keycloak error", e);
                return Response.status(BAD_GATEWAY).entity(e.getMessage()).build();
            }
        } else if(reference != null && search == null) {
            final UserResponseBuilder userResponseBuilder;
            kheopsLogBuilder.action(ActionType.TEST_USER);

            try {
                final Keycloak keycloak = Keycloak.getInstance();
                userResponseBuilder = keycloak.getUser(reference);

                if(albumId != null) {
                    kheopsLogBuilder.album(albumId);
                    userResponseBuilder.setAlbumAccess(Albums.isMemberOfAlbum(userResponseBuilder.getSub(), albumId));
                }

                if(studyInstanceUID != null) {
                    userResponseBuilder.setStudyAccess(Studies.canAccessStudy(userResponseBuilder.getSub(), studyInstanceUID));
                    kheopsLogBuilder.study(studyInstanceUID);
                }

                kheopsLogBuilder.log();
                return Response.status(OK).entity(userResponseBuilder.build()).build();
            } catch (UserNotFoundException e) {
                LOG.log(Level.WARNING, "User not found", e);
                return Response.status(NO_CONTENT).build();
            } catch (KeycloakException e) {
                LOG.log(Level.WARNING, "Keycloak error", e);
                return Response.status(BAD_GATEWAY).entity(e.getMessage()).build();
            }
        } else {
            return Response.status(BAD_REQUEST).build();
        }
    }


    @GET
    @Path("userinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public OIDCUserInfo getOIDCUserInfo(@HeaderParam(AUTHORIZATION) String authorizationHeader) {

        final String token;
        if (authorizationHeader.toUpperCase().startsWith("BEARER ")) {
            token = authorizationHeader.substring(7);
        } else {
            LOG.log(Level.INFO, "missing bearer authorization header");
            throw new NotAuthorizedException("Bearer");
        }

        final AccessToken accessToken;
        try {
            accessToken = AccessTokenVerifier.authenticateAccessToken(servletContext, token);
        } catch (AccessTokenVerificationException e) {
            LOG.log(Level.INFO, "bad accesstoken", e);
            throw new NotAuthorizedException("Bad AccessToken");
        }

        if (!accessToken.getTokenType().equals(REPORT_PROVIDER_TOKEN)) {
            LOG.log(Level.INFO, "Can only get userinfo for report provider tokens");
            throw new ForbiddenException("Can only get userinfo for report provider tokens");
        }

        try {
            new KheopsLogBuilder().user(accessToken.getSubject())
                    .provenance(accessToken)
                    .action(ActionType.USER_INFO)
                    .tokenType(accessToken.getTokenType())
                    .log();
            return OIDCUserInfo.from(Keycloak.getInstance().getUserRepresentation(accessToken.getSubject()));
        } catch (UserNotFoundException | KeycloakException e) {
            LOG.log(Level.INFO, "Unable to get the user info", e);
            throw new ServerErrorException("Unable to get the user info", BAD_GATEWAY, e);
        }
    }
}
