package online.kheops.proxy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.json.JSONReader;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DicomWeb extends ProxyServlet
{
    private static final String KHEOPS_ACCESS_TOKEN = "kheops.accessToken";
    private static final String KHEOPS_USER = "kheops.user";

    //    private final static Pattern URI_PATTERN = Pattern.compile("/capabilities/([\\w]+)/proxy/studies(?:/)+?|(?:/([0-9.]+)(?:/)+?)+?");
    private final static Pattern URI_PATTERN = Pattern.compile(".*?/capabilities/([\\w]+)/dicomweb/studies(?:/)??");
    private final static Pattern PASSWORD_PATTERN = Pattern.compile(".*?/capabilities/password/dicomweb/studies(?:/)??");

    private static final Logger LOG = Logger.getLogger(DicomWeb.class.getName());

    @Override
    protected void initTarget() throws ServletException {
        targetUri = getContextParam("online.kheops.pacs.uri");
        if (targetUri == null)
            throw new ServletException("online.kheops.pacs.uri"+" is required.");
        //test it's valid
        try {
            targetUriObj = new URI(targetUri);
        } catch (Exception e) {
            throw new ServletException("Trying to process targetUri init parameter: "+e,e);
        }
        targetHost = URIUtils.extractHost(targetUriObj);
    }


    @Override
    protected String rewriteUrlFromRequest(HttpServletRequest servletRequest) {
        String uriString = servletRequest.getRequestURI();
        Matcher uriMatcher = URI_PATTERN.matcher(uriString);
        Matcher passwordMatcher = PASSWORD_PATTERN.matcher(uriString);

        if (!uriMatcher.matches() && !passwordMatcher.matches()) {
            throw new IllegalArgumentException("can't parse the URL:" + uriString);
        }

        if (uriMatcher.matches() && !passwordMatcher.matches()) {
            final String capabilitySecret = uriMatcher.group(1);

            final URI authorizationServerRoot;
            try {
                authorizationServerRoot = new URI(getContextParam("online.kheops.auth_server.uri"));
            } catch (URISyntaxException e) {
                throw new IllegalStateException("online.kheops.auth_server.uri is not a valid URI", e);
            }

            AccessToken accessToken = AccessToken.createBuilder(authorizationServerRoot).withCapability(capabilitySecret).build();

            servletRequest.setAttribute(KHEOPS_ACCESS_TOKEN, accessToken.getToken());
            servletRequest.setAttribute(KHEOPS_USER, accessToken.getUser());
        }

        String targetURL = getTargetUri(servletRequest)+"/studies";
        LOG.info("Returning target URI: " + targetURL);


        return targetURL;
    }

    @Override
    protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
        final String AuthorizationHeader =  servletRequest.getHeader("Authorization");

        if (AuthorizationHeader != null) {
            if (!AuthorizationHeader.toUpperCase().startsWith("BASIC ")) {
                throw new IllegalStateException("Authorization header is not basic authorization");
            }

            final String encodedAuthorization = AuthorizationHeader.substring(6);

            final String decoded = new String(Base64.getDecoder().decode(encodedAuthorization), StandardCharsets.UTF_8);
            String[] split = decoded.split(":");
            if (split.length != 2) {
                throw new IllegalStateException("Authorization header has more than two elements");
            }

            final String username = split[0];
            final String capability = split[1];

            if (!username.equals("Capability")) {
                throw new IllegalStateException("Authorization header username is not Capability");
            }

            final URI authorizationServerRoot;
            try {
                authorizationServerRoot = new URI(getContextParam("online.kheops.auth_server.uri"));
            } catch (URISyntaxException e) {
                throw new IllegalStateException("online.kheops.auth_server.uri is not a valid URI", e);
            }

            AccessToken accessToken = AccessToken.createBuilder(authorizationServerRoot).withCapability(capability).build();

            servletRequest.setAttribute(KHEOPS_ACCESS_TOKEN, accessToken.getToken());
            servletRequest.setAttribute(KHEOPS_USER, accessToken.getUser());
        }

        super.copyRequestHeaders(servletRequest, proxyRequest);
        proxyRequest.addHeader("Authorization", "Bearer " + getKheopsAccessToken(servletRequest));
    }

    @Override
    protected void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse,
                                      HttpRequest proxyRequest, HttpServletRequest servletRequest)
                  throws IOException {

        LOG.info("Starting to copy the response");

        final URI authenticationServerURI;
        try {
            authenticationServerURI = new URI(getContextParam("online.kheops.auth_server.uri"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("online.kheops.auth_server.uri is not a valid URI", e);
        }
        Client client = ClientBuilder.newClient();

        HttpEntity entity = proxyResponse.getEntity();
        if (entity != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            entity.writeTo(os);

            servletResponse.getOutputStream().write(os.toByteArray());
            servletResponse.getOutputStream().close();

            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            LOG.info("Built the shared input stream");

            Attributes attributes;

            ContentType contentType = ContentType.getOrDefault(entity);

            switch (contentType.getMimeType()) {
                case "application/dicom+json":
                    LOG.info("Processing application/dicom+json");
                    JsonParser parser = Json.createParser(is);
                    JSONReader jsonReader = new JSONReader(parser);
                    attributes = jsonReader.readDataset(null);
                    break;
                case "application/dicom+xml":
                    LOG.info("Processing application/dicom+xml");
                    try {
                        attributes = SAXReader.parse(is);
                    } catch (Exception e) {
                        throw new IllegalStateException("Unable to parse XML", e);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown Content-Type");
            }

            LOG.info("Starting to read the attributes");

            Sequence instanceSequence = attributes.getSequence(Tag.ReferencedSOPSequence);

            Set<String> sentSeries = new HashSet<>();
            for (Attributes instance: instanceSequence) {
                String instanceURL = instance.getString(Tag.RetrieveURL);

                // this is a total hack, get the study and series UIDs out of the returned URL
                int studiesIndex = instanceURL.indexOf("/studies/");
                int seriesIndex = instanceURL.indexOf("/series/");
                int instancesIndex = instanceURL.indexOf("/instances/");

                String studyUID = instanceURL.substring(studiesIndex + 9, seriesIndex);
                String seriesUID = instanceURL.substring(seriesIndex + 8, instancesIndex);
                String combinedUID = studyUID + "/" + seriesUID;

                if (!sentSeries.contains(combinedUID)) {
                    LOG.info("About to try to claim a series");

                    URI claimURI = UriBuilder.fromUri(authenticationServerURI).path("users/{user}/studies/{studyUID}/series/{seriesUID}").build(getKheopsUser(servletRequest), studyUID, seriesUID);
                    client.target(claimURI).request().header("Authorization", "Bearer " + getKheopsAccessToken(servletRequest)).put(Entity.text(""));

                    LOG.info("finished claiming the series");

                    sentSeries.add(combinedUID);
                }
            }
        } else {
            LOG.warning("The entity was null");
        }
        LOG.warning("finished copying the response");
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws ServletException, IOException {
        if (!servletRequest.getMethod().equals("POST")) {
            servletResponse.setStatus(405);
            LOG.warning("received request with method "+servletRequest.getMethod() + ", not processing it");
        } else {
            super.service(servletRequest, servletResponse);
        }
    }

    private String getKheopsAccessToken(HttpServletRequest servletRequest) {
        return (String) servletRequest.getAttribute(KHEOPS_ACCESS_TOKEN);
    }
    private String getKheopsUser(HttpServletRequest servletRequest) {
        return (String) servletRequest.getAttribute(KHEOPS_USER);
    }

    @SuppressWarnings("WeakerAccess")
    protected String getContextParam(String key) {
        return getServletContext().getInitParameter(key);
    }

    protected HttpResponse doExecute(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
                                     HttpRequest proxyRequest) throws IOException {
        HttpResponse httpResponse;
        try {
            httpResponse = super.doExecute(servletRequest, servletResponse, proxyRequest);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error!!",e);
            throw e;
        }
        return httpResponse;
    }
}


