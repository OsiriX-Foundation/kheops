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
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

    @SuppressWarnings("unused")
    static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
        @XmlElement(name = "token_type")
        String tokenType;
        @XmlElement(name = "expires_in")
        String expiresIn;
        @XmlElement(name = "user")
        String user;
    }

    //    private final static Pattern URI_PATTERN = Pattern.compile("/capabilities/([\\w]+)/proxy/studies(?:/)+?|(?:/([0-9.]+)(?:/)+?)+?");
    private final static Pattern URI_PATTERN = Pattern.compile("/capabilities/([\\w]+)/proxy/studies(?:/)??");

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
        Matcher m = URI_PATTERN.matcher(uriString);

        if (!m.matches()) {
            throw new IllegalStateException("can't parse the URL");
        }

        final String capabilitySecret = m.group(1);

        Client client = ClientBuilder.newClient();

        final URI authenticationServerURI;
        try {
            authenticationServerURI = new URI(getContextParam("online.kheops.auth_server.uri"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("online.kheops.auth_server.uri is not a valid URI", e);
        }

        Form form = new Form().param("assertion", capabilitySecret).param("grant_type", "urn:x-kheops:params:oauth:grant-type:capability");
        URI uri = UriBuilder.fromUri(authenticationServerURI).path("token").build();

        LOG.info("About to get a token");

        final TokenResponse tokenResponse;
        try {
            tokenResponse = client.target(uri).request("application/json").post(Entity.form(form), TokenResponse.class);
        } catch (ResponseProcessingException e) {
            LOG.log(Level.WARNING,"Unable to obtain a token for capability token", e);
            throw new IllegalStateException("Unable to get a request token for the capability URL", e);
        }

        servletRequest.setAttribute(KHEOPS_ACCESS_TOKEN, tokenResponse.accessToken);
        servletRequest.setAttribute(KHEOPS_USER, tokenResponse.user);

        return getTargetUri(servletRequest)+"/studies";
    }

    @Override
    protected void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse,
                                      HttpRequest proxyRequest, HttpServletRequest servletRequest)
                  throws IOException {

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

            Attributes attributes;

            ContentType contentType = ContentType.getOrDefault(entity);

            switch (contentType.getMimeType()) {
                case "application/dicom+json":
                    JsonParser parser = Json.createParser(is);
                    JSONReader jsonReader = new JSONReader(parser);
                    attributes = jsonReader.readDataset(null);
                    break;
                case "application/dicom+xml":
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
        }
    }


    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws ServletException, IOException {
        if (!servletRequest.getMethod().equals("POST")) {
            servletResponse.setStatus(405);
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

    protected String getContextParam(String key) {
        return getServletContext().getInitParameter(key);
    }

}


