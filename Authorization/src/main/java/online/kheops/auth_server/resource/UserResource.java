package online.kheops.auth_server.resource;

import online.kheops.auth_server.OIDCProviderContextListener;
import online.kheops.auth_server.accesstoken.AccessTokenVerificationException;
import online.kheops.auth_server.accesstoken.OidcAccessToken;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.accesstoken.AccessToken;
import online.kheops.auth_server.accesstoken.AccessTokenVerifier;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.user.*;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder.*;
import online.kheops.auth_server.util.KheopsLogBuilder;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import javax.servlet.ServletContext;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlElement;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.accesstoken.AccessToken.TokenType.KEYCLOAK_TOKEN;
import static online.kheops.auth_server.accesstoken.AccessToken.TokenType.REPORT_PROVIDER_TOKEN;
import static online.kheops.auth_server.album.Albums.isMemberOfAlbum;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.QUERY_PARAM;
import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.user.AlbumUserPermissions.LIST_USERS;
import static online.kheops.auth_server.user.Users.*;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.ErrorResponse.Message.*;
import static online.kheops.auth_server.util.HttpHeaders.X_FORWARDED_FOR;

@Path("/")
public class UserResource {
    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

    private static class OIDCUserInfo {
        @XmlElement(name = "sub")
        public String sub;
        @XmlElement(name = "name")
        public String name;
        @XmlElement(name = "email")
        public String email;

        private static OIDCUserInfo from(User user) {
            OIDCUserInfo userInfo = new OIDCUserInfo();
            userInfo.sub = user.getSub();
            userInfo.name = user.getName();
            userInfo.email = user.getEmail();
            return userInfo;
        }
    }

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    @Context
    private ServletContext servletContext;

    @HeaderParam(X_FORWARDED_FOR)
    private String headerXForwardedFor;

    @GET
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = LIST_USERS, context = QUERY_PARAM)
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getUserInfo(@QueryParam("reference") String reference,
                                @QueryParam(ALBUM) String albumId,
                                @QueryParam("studyInstanceUID") @UIDValidator String studyInstanceUID,
                                @QueryParam("search") String search,
                                @QueryParam(QUERY_PARAMETER_LIMIT) @Min(0) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                                @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer offset)
            throws AlbumNotFoundException, UserNotMemberException {

        final KheopsPrincipal kheopsPrincipal = (KheopsPrincipal)securityContext.getUserPrincipal();
        final KheopsLogBuilder kheopsLogBuilder = kheopsPrincipal.getKheopsLogBuilder();

        if(reference == null && search != null) {
            if (search.length() < 3) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("'search' query param must have minimum 3 characters")
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }

            kheopsLogBuilder.action(ActionType.USERS_LIST);

            final List<UserResponse> result;

            if (albumId != null) {
                result = searchUsersInAlbum(search, albumId, limit, offset);
            } else if (studyInstanceUID != null) {
                if (!kheopsPrincipal.hasStudyViewAccess(studyInstanceUID)) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(STUDY_NOT_FOUND)
                            .detail("The study does not exist or you don't have access")
                            .build();
                    return Response.status(NOT_FOUND).entity(errorResponse).build();
                }
                result = searchUsersInStudy(search, studyInstanceUID, limit, offset);
            } else {
                result = searchUsers(search, limit, offset);
            }

            result.remove(new UserResponse(kheopsPrincipal.getUser()));

            kheopsLogBuilder.log();

            if(result.isEmpty()) {
                return Response.status(NO_CONTENT).build();
            }

            final GenericEntity<List<UserResponse>> genericUsersResponsesList = new GenericEntity<List<UserResponse>>(result) {};
            return Response.status(OK).entity(genericUsersResponsesList).build();
        } else if(reference != null && search == null) {
            final UserResponseBuilder userResponseBuilder;
            kheopsLogBuilder.action(ActionType.TEST_USER);

            try {
                final String referenceString = reference.toLowerCase().trim();

                final User user = getUser(referenceString);
                userResponseBuilder = new UserResponseBuilder().setName(user.getName())
                        .setSub(user.getSub())
                        .setEmail(user.getEmail());

                if(albumId != null) {
                    kheopsLogBuilder.album(albumId);
                    userResponseBuilder.setAlbumAccess(isMemberOfAlbum(userResponseBuilder.getSub(), albumId));
                }

                if(studyInstanceUID != null) {
                    userResponseBuilder.setStudyAccess(canAccessStudy(userResponseBuilder.getSub(), studyInstanceUID));
                    kheopsLogBuilder.study(studyInstanceUID);
                }

                kheopsLogBuilder.log();
                return Response.status(OK).entity(userResponseBuilder.build()).build();
            } catch (UserNotFoundException e) {
                return Response.status(NO_CONTENT).build();
            }
        } else {
            return Response.status(BAD_REQUEST).entity("Use query param 'search' xor 'reference'").build();
        }
    }

    private static class ConfigurationEntity {
        @XmlElement(name="userinfo_endpoint")
        String userinfoEndpoint;
    }
    private static class UserInfoEntity {
        @XmlElement(name="name")
        String name;
        @XmlElement(name="sub")
        String sub;
        @XmlElement(name="email")
        String email;
    }
    private static final Client CLIENT = ClientBuilder.newClient();

    private static final String USER_INFO_URLS_CACHE_ALIAS = "userInfoURLsCache";
    private static CacheManager userInfoCacheManager = getUserInfoCacheManager();
    private static Cache<String, String> userInfoURLsCache = userInfoCacheManager.getCache(USER_INFO_URLS_CACHE_ALIAS, String.class, String.class);

    private static CacheManager getUserInfoCacheManager() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(USER_INFO_URLS_CACHE_ALIAS, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(10))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10))))
                .build();
        cacheManager.init();
        return cacheManager;
    }

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register2(@FormParam("access_token") String token) {

        if (token == null || token.isEmpty()) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'access_token' must be set")
                    .build();
            LOG.log(Level.WARNING, "token is null");
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final OidcAccessToken accessToken;
        try {
            accessToken = new OidcAccessToken.Builder(servletContext).build(token, true);
        } catch (AccessTokenVerificationException e) {
            LOG.log(Level.WARNING, "Access token error", e);
            return Response.status(BAD_REQUEST).build();
        }

        final String oidcProvider = OIDCProviderContextListener.getOIDCProvider();
        String userInfoUrl = userInfoURLsCache.get(oidcProvider);
        if (userInfoUrl == null) {
            final String openidConfiguration = OIDCProviderContextListener.getOIDCConfigurationString();
            final URI openidConfigurationURI;

            try {
                openidConfigurationURI = new URI(openidConfiguration);
                ConfigurationEntity res = CLIENT.target(openidConfigurationURI).request(MediaType.APPLICATION_JSON).get(ConfigurationEntity.class);
                userInfoUrl = res.userinfoEndpoint;
                userInfoURLsCache.put(oidcProvider, userInfoUrl);
            } catch (ProcessingException | WebApplicationException e) {
                LOG.log(Level.SEVERE, "Unable to get userInfoURL", e);
                return Response.status(BAD_GATEWAY).build();
            } catch (URISyntaxException e) {
                LOG.log(Level.SEVERE, "bad configuration URL", e);
                return Response.status(INTERNAL_SERVER_ERROR).build();
            }
        }
        try {
            final URI userinfoEndpointURI;
            userinfoEndpointURI = new URI(userInfoUrl);
            UserInfoEntity userInfoEntity = CLIENT.target(userinfoEndpointURI).request(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + token).get(UserInfoEntity.class);

            final String welcomebotWebhook = servletContext.getInitParameter("online.kheops.welcomebot.webhook");
            final KheopsLogBuilder logBuilder = new KheopsLogBuilder();
            final User user = upsertUser(userInfoEntity.sub, userInfoEntity.name, userInfoEntity.email, welcomebotWebhook, logBuilder);

            final UserResponse userResponse = new UserResponseBuilder().setUser(user).build();

            logBuilder.tokenType(KEYCLOAK_TOKEN);
            if (headerXForwardedFor != null) {
                logBuilder.ip(headerXForwardedFor);
            }
            logBuilder.provenance(accessToken);
            logBuilder.log();

            return Response.ok().entity(userResponse).build();

        } catch (NotAuthorizedException e) {
            return Response.status(UNAUTHORIZED).build();
        } catch (ProcessingException | WebApplicationException e) {
            LOG.log(Level.SEVERE, "Unable to get user response", e);
            return Response.status(BAD_GATEWAY).build();
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, "bad configuration URL", e);
            return Response.status(INTERNAL_SERVER_ERROR).build();
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
        } catch (online.kheops.auth_server.accesstoken.AccessTokenVerificationException e) {
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
            final User user = getUser(accessToken.getSubject());
            return OIDCUserInfo.from(user);
        } catch (UserNotFoundException e) {
            LOG.log(Level.INFO, "Unable to get the user info", e);
            throw new ServerErrorException("Unable to get the user info", BAD_GATEWAY, e);
        }
    }
}
