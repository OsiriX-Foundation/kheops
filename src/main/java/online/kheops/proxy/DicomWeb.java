package online.kheops.proxy;

import org.mitre.dsmiley.httpproxy.ProxyServlet;

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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DicomWeb extends ProxyServlet
{

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
            authenticationServerURI = new URI(getConfigParam("online.kheops.auth_server.uri"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("online.kheops.auth_server.uri is not a valid URI", e);
        }

//        final URI dicomWebURI;
//        try {
//            dicomWebURI = new URI(context.getInitParameter("online.kheops.pacs.uri"));
//        } catch (URISyntaxException e) {
//            throw new IllegalStateException("online.kheops.pacs.uri is not a valid URI", e);
//        }

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

        return getTargetUri(servletRequest);
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

}


