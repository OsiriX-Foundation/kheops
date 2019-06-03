package online.kheops.auth_server.resource;

import online.kheops.auth_server.album.Albums;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.AssertionVerifier;
import online.kheops.auth_server.assertion.BadAssertionException;
import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.keycloak.KeycloakException;
import online.kheops.auth_server.study.Studies;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponseBuilder;
import online.kheops.auth_server.user.Users;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlElement;

import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.assertion.Assertion.TokenType.REPORT_PROVIDER_TOKEN;
import static online.kheops.auth_server.user.UserPermissionEnum.LIST_USERS;
import static online.kheops.auth_server.util.Consts.ALBUM;

@Path("/")
public class UserResource {
    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

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
                                @QueryParam("studyInstanceUID") @UIDValidator String studyInstanceUID) {

        final UserResponseBuilder userResponseBuilder;

        try {
            final Keycloak keycloak = Keycloak.getInstance();
            userResponseBuilder = keycloak.getUser(reference);

            if(albumId != null) {
                userResponseBuilder.setAlbumAccess(Albums.isMemberOfAlbum(userResponseBuilder.getSub(), albumId));
            }

            if(studyInstanceUID != null) {
                userResponseBuilder.setStudyAccess(Studies.canAccessStudy(userResponseBuilder.getSub(), studyInstanceUID));
            }

            return Response.status(OK).entity(userResponseBuilder.build()).build();
        } catch (UserNotFoundException e) {
            LOG.log(Level.WARNING, "User not found", e);
            return Response.status(NO_CONTENT).build();
        } catch (KeycloakException e) {
            LOG.log(Level.WARNING, "Keycloak error", e);
            return Response.status(BAD_GATEWAY).entity(e.getMessage()).build();
        }
    }

    private static class OIDCUserInfoResponse {
        @XmlElement(name = "sub")
        String sub;
        @XmlElement(name = "email")
        String email;
    }

    @GET
    @Path("userinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public OIDCUserInfoResponse getOIDCUserinfo(@HeaderParam(AUTHORIZATION) String authorizationHeader) {

        final String token;
        if (authorizationHeader.toUpperCase().startsWith("BEARER ")) {
            token = authorizationHeader.substring(7);
        } else {
            LOG.log(Level.INFO, "missing bearer authorization header");
            throw new NotAuthorizedException("Bearer");
        }

        final Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(servletContext, token);
        } catch (BadAssertionException e) {
            LOG.log(Level.INFO, "bad assertion", e);
            throw new ForbiddenException("Bad Assertion");
        }

        if (!assertion.getTokenType().equals(REPORT_PROVIDER_TOKEN)) {
            LOG.log(Level.INFO, "Can only get userinfo for report provider tokens");
            throw new ForbiddenException("Can only get userinfo for report provider tokens");
        }

        final OIDCUserInfoResponse userInfo = new OIDCUserInfoResponse();
        try {
            userInfo.sub = assertion.getSub();
            userInfo.email = Users.getOrCreateUser(assertion.getSub()).getEmail();
        } catch (UserNotFoundException e) {
            LOG.log(Level.INFO, "unknown sub", e);
            throw new InternalServerErrorException(e);
        }

        return userInfo;
    }
}
