package online.kheops.proxy.wadouri;

import online.kheops.proxy.id.SeriesID;
import online.kheops.proxy.tokens.AccessToken;
import online.kheops.proxy.tokens.AccessTokenException;
import online.kheops.proxy.tokens.AuthorizationToken;
import org.dcm4che3.ws.rs.MediaTypes;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.*;
import static javax.ws.rs.core.Response.Status.BAD_GATEWAY;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Path("/")
public class WadoUriResource {
    private static final Logger LOG = Logger.getLogger(WadoUriResource.class.getName());

    private static final Client CLIENT = ClientBuilder.newClient();

    @Context
    UriInfo uriInfo;

    @Context
    ServletContext context;

    @HeaderParam(ACCEPT)
    String acceptParam;

    @HeaderParam(ACCEPT_CHARSET)
    String acceptCharsetParam;

    @GET
    @Path("/password/dicomweb/wado")
    public Response wado(@HeaderParam(AUTHORIZATION) String authorizationHeader) {
        return webAccess(AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }

    @GET
    @Path("/{capability:[a-zA-Z0-9]{22}}/dicomweb/wado")
    public Response wadoWithCapability(@PathParam("capability") String capabilityToken) {
        return webAccess(AuthorizationToken.from(capabilityToken));
    }

    private Response webAccess(AuthorizationToken authorizationToken) {
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");
        URI wadoServiceURI = getParameterURI("online.kheops.pacs.uri");

        WebTarget webTarget = CLIENT.target(wadoServiceURI).path("wado");

        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        final List<String> studyInstanceUIDs = queryParameters.get("studyUID");
        if (studyInstanceUIDs == null || studyInstanceUIDs.size() != 1) {
            throw new WebApplicationException(BAD_REQUEST);
        }
        final List<String> seriesInstanceUIDs = queryParameters.get("seriesUID");
        if (seriesInstanceUIDs == null || seriesInstanceUIDs.size() != 1) {
            throw new WebApplicationException(BAD_REQUEST);
        }
        final List<String> objectUIDs = queryParameters.get("objectUID");
        if (objectUIDs == null || objectUIDs.size() != 1) {
            throw new WebApplicationException(BAD_REQUEST);
        }

        final String studyInstanceUID = studyInstanceUIDs.get(0);
        final String seriesInstanceUID = seriesInstanceUIDs.get(0);
        final String objectInstanceUID = objectUIDs.get(0);

        final AccessToken accessToken;
        try {
            accessToken = AccessToken.createBuilder(authorizationURI)
                    .withCapability(authorizationToken.getToken())
                    .withSeriesID(new SeriesID(studyInstanceUID, seriesInstanceUID))
                    .build();
        } catch (AccessTokenException e) {
            throw new WebApplicationException(BAD_GATEWAY);
        } catch (Exception e) {
            LOG.log(SEVERE, "unknown error while getting an access token", e);
            throw new InternalServerErrorException("unknown error while getting an access token", e);
        }

        for (Map.Entry<String, List<String>> parameter: queryParameters.entrySet()) {
            webTarget = webTarget.queryParam(parameter.getKey(), parameter.getValue().toArray());
        }

        Invocation.Builder invocationBuilder = webTarget.request();
        invocationBuilder.header(AUTHORIZATION, accessToken.getHeaderValue());
        if (acceptParam != null) {
            invocationBuilder.accept(acceptParam);
        } else {
            invocationBuilder.accept(MediaTypes.APPLICATION_DICOM);
        }

        if (acceptCharsetParam != null) {
            invocationBuilder.header(ACCEPT_CHARSET, acceptCharsetParam);
        }


        final Response upstreamResponse;
        try {
            upstreamResponse = invocationBuilder.get();
        } catch (ProcessingException e) {
            LOG.log(SEVERE, "error processing response from upstream", e);
            throw new InternalServerErrorException();
        } catch (Exception e) {
            LOG.log(SEVERE, "unknown error while getting from upstream", e);
            throw new InternalServerErrorException("unknown error while getting an access token", e);
        }

        final Object entity = upstreamResponse.getEntity();
        final InputStream inputStream;
        if (entity instanceof InputStream) {
            inputStream = (InputStream) entity;
        } else {
            LOG.log(SEVERE, "Upstream response's entity is not an InputStream");
            throw new InternalServerErrorException();
        }

        StreamingOutput streamingOutput = output -> {
            LOG.log(WARNING, "starting to stream: " + objectInstanceUID);
            try {
                byte[] buffer = new byte[4096];
                int len = inputStream.read(buffer);
                while (len != -1) {
                    output.write(buffer, 0, len);
                    len = inputStream.read(buffer);
                }
            } catch (Exception e) {
                LOG.log(SEVERE, "exception while streaming: " + objectInstanceUID, e);
                throw new IOException(e);
            }
            LOG.log(WARNING, "done streaming: objectInstanceUID" + objectInstanceUID);
        };

        final CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);

        return Response.ok(streamingOutput)
                .header(CONTENT_TYPE, upstreamResponse.getHeaderString(CONTENT_TYPE))
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


