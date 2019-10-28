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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.*;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.Response.Status.*;

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
    public Response wadoSeries(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                               @PathParam("studyInstanceUID") String studyInstanceUID,
                               @PathParam("seriesInstanceUID") String seriesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }

    @GET
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}/metadata")
    public Response wadoSeriesMetadata(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                               @PathParam("studyInstanceUID") String studyInstanceUID,
                               @PathParam("seriesInstanceUID") String seriesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }

    @GET
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}/instances/{sopInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response wadoInstance(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                                 @PathParam("studyInstanceUID") String studyInstanceUID,
                                 @PathParam("seriesInstanceUID") String seriesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }

    @GET
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}/instances/{sopInstanceUID:([0-9]+[.])*[0-9]+}/metadata")
    public Response wadoInstanceMetadata(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                                 @PathParam("studyInstanceUID") String studyInstanceUID,
                                 @PathParam("seriesInstanceUID") String seriesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }
    
    @GET
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}/instances/{sopInstanceUID:([0-9]+[.])*[0-9]+}/frames/{frameNumber:[0-9]+}")
    public Response wadoInstanceFrame(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                                 @PathParam("studyInstanceUID") String studyInstanceUID,
                                 @PathParam("seriesInstanceUID") String seriesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }


    @GET
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}/thumbnail")
    public Response wadoSeriesThumbnail(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                                 @PathParam("studyInstanceUID") String studyInstanceUID,
                                 @PathParam("seriesInstanceUID") String seriesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }

    @GET
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}/instances/{instancesInstanceUID:([0-9]+[.])*[0-9]+}/thumbnail")
    public Response wadoInstancesThumbnail(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                                           @PathParam("studyInstanceUID") String studyInstanceUID,
                                           @PathParam("seriesInstanceUID") String seriesInstanceUID,
                                           @PathParam("instancesInstanceUID") String instancesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }

    @GET
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}/instances/{instancesInstanceUID:([0-9]+[.])*[0-9]+}/frames/{framesInstanceUID:([0-9]+[.])*[0-9]+}/thumbnail")
    public Response wadoFramesThumbnail(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                                        @PathParam("studyInstanceUID") String studyInstanceUID,
                                        @PathParam("seriesInstanceUID") String seriesInstanceUID,
                                        @PathParam("instancesInstanceUID") String instancesInstanceUID,
                                        @PathParam("framesInstanceUID") String framesInstanceUID) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }
    
    //@GET
    //@Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}/instances")
    //public Response wadoInstances(@HeaderParam(AUTHORIZATION) String authorizationHeader,
    //                             @PathParam("studyInstanceUID") String studyInstanceUID,
    //                             @PathParam("seriesInstanceUID") String seriesInstanceUID) {
    //    return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    //}    
    
    private Response webAccess(String studyInstanceUID, String seriesInstanceUID, AuthorizationToken authorizationToken) {
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");
        URI wadoServiceURI = getParameterURI("online.kheops.pacs.uri");
        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        final AccessToken accessToken;
        try {
            accessToken = AccessToken.createBuilder(authorizationURI)
                    .withClientId(context.getInitParameter("online.kheops.client.dicomwebproxyclientid"))
                    .withClientSecret(context.getInitParameter("online.kheops.client.dicomwebproxysecret"))
                    .withCapability(authorizationToken.getToken())
                    .withSeriesID(new SeriesID(studyInstanceUID, seriesInstanceUID))
                    .build();
        } catch (AccessTokenException e) {
            LOG.log(WARNING, "Unable to get an access token", e);
            throw new NotAuthorizedException("Bearer", "Basic");
        } catch (Exception e) {
            LOG.log(SEVERE, "unknown error while getting an access token", e);
            throw new InternalServerErrorException("unknown error while getting an access token");
        }

        final Matcher m = Pattern.compile("(studies/(?:(?:[0-9]+[.])*[0-9]+)/series.*)").matcher(uriInfo.getPath());

        final String resource;
        if (m.find()) {
            resource = m.group(1);
        } else {
            throw new WebApplicationException(BAD_REQUEST);
        }

        WebTarget webTarget = CLIENT.target(wadoServiceURI).path(resource);
        for (Map.Entry<String, List<String>> parameter: queryParameters.entrySet()) {
            webTarget = webTarget.queryParam(parameter.getKey(), parameter.getValue().toArray());
        }

        Invocation.Builder invocationBuilder = webTarget.request();
        invocationBuilder.header(AUTHORIZATION, accessToken.getHeaderValue());
        if (acceptParam != null) {
            invocationBuilder.header(ACCEPT, acceptParam);
        }
        if (acceptCharsetParam != null) {
            invocationBuilder.header(ACCEPT_CHARSET, acceptCharsetParam);
        }

        final Response upstreamResponse;
        try {
            upstreamResponse = invocationBuilder.get();
        } catch (ProcessingException e) {
            LOG.log(SEVERE, "error processing response from upstream", e);
            throw new WebApplicationException(BAD_GATEWAY);
        } catch (Exception e) {
            LOG.log(SEVERE, "unknown error while getting from upstream", e);
            throw new InternalServerErrorException("unknown error while getting from upstream");
        }

        StreamingOutput streamingOutput = output -> {
            try (final InputStream inputStream = upstreamResponse.readEntity(InputStream.class)) {
                byte[] buffer = new byte[4096];
                int len = inputStream.read(buffer);
                while (len != -1) {
                    output.write(buffer, 0, len);
                    len = inputStream.read(buffer);
                }
            } catch (Exception e) {
                throw new IOException(e);
            } finally {
                upstreamResponse.close();
            }
        };

        final CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);

        return Response.status(upstreamResponse.getStatus()).entity(streamingOutput)
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
