package online.kheops.proxy.wadors;

import online.kheops.proxy.id.SeriesID;
import online.kheops.proxy.tokens.AccessToken;
import online.kheops.proxy.tokens.AccessTokenException;
import online.kheops.proxy.tokens.AuthorizationToken;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_CHARSET;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path("/")
public final class WadoRsResource {
    private static final Logger LOG = Logger.getLogger(WadoRsResource.class.getName());

    private static final Client CLIENT = ClientBuilder.newClient();

    @Context
    UriInfo uriInfo;

    @Context
    ServletContext context;

    @Context
    HttpHeaders httpHeaders;

    @HeaderParam(ACCEPT)
    String acceptParam;

    @HeaderParam(ACCEPT_CHARSET)
    String acceptCharsetParam;

    @GET
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response wado(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                         @PathParam("studyInstanceUID") String studyInstanceUID,
                         @PathParam("seriesInstanceUID") String seriesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }

    @GET
    @Path("/{capability:[a-zA-Z0-9]{22}}/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response wadoWithCapability(@PathParam("capability") String capabilityToken,
                                       @PathParam("studyInstanceUID") String studyInstanceUID,
                                       @PathParam("seriesInstanceUID") String seriesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.from(capabilityToken));
    }

    private Response webAccess(String studyInstanceUID, String seriesInstanceUID, AuthorizationToken authorizationToken) {
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");
        URI wadoServiceURI = getParameterURI("online.kheops.pacs.uri");

        final AccessToken accessToken;
        try {
            accessToken = AccessToken.createBuilder(authorizationURI)
                    .withCapability(authorizationToken.getToken())
                    .withSeriesID(new SeriesID(studyInstanceUID, seriesInstanceUID))
                    .build();
        } catch (AccessTokenException e) {
            LOG.log(WARNING, "Unable to get an access token", e);
            throw new WebApplicationException(UNAUTHORIZED);
        }

        Pattern p = Pattern.compile("(studies/(?:(?:[0-9]+[.])*[0-9]+)/series.*)");
        Matcher m = p.matcher(uriInfo.getPath());

        final String resource;
        if (m.find()) {
            resource = m.group(1);
        } else {
            throw new WebApplicationException(BAD_REQUEST);
        }

        WebTarget webTarget = CLIENT.target(wadoServiceURI).path(resource);

        Invocation.Builder invocationBuilder = webTarget.request();
        invocationBuilder.header(AUTHORIZATION, accessToken.getHeaderValue());
        if (acceptParam != null) {
            invocationBuilder.header(ACCEPT, acceptParam);
        }
        if (acceptCharsetParam != null) {
            invocationBuilder.header(ACCEPT_CHARSET, acceptCharsetParam);
        }

        final CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);

        return Response.fromResponse(invocationBuilder.get())
                .cacheControl(cacheControl)
                .build();
    }

    private URI getParameterURI(String parameter) {
        try {
            return new URI(context.getInitParameter(parameter));
        } catch (URISyntaxException e) {
            LOG.log(SEVERE, "Error with the STOWServiceURI", e);
            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
        }
    }

}
