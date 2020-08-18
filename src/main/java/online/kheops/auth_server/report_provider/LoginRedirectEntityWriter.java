package online.kheops.auth_server.report_provider;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Provider
@Produces(MediaType.TEXT_HTML)
public class LoginRedirectEntityWriter implements MessageBodyWriter<LoginRedirectEntity> {

    private static final String ISS_PARAMETER = "iss";
    private static final String LOGIN_HINT_PARAMETER = "login_hint";
    private static final String TARGET_LINK_URI_PARAMETER = "target_link_uri";

    private static final String DEFAULT_CLIENT_NAME = "Report Provider";

    private static final String REPORT_PROVIDER_NAME = "$report_provider_name";
    private static final String REPORT_PROVIDER_URL = "$report_provider_url";

    private static final String HTML_TEMPLATE =
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "  <head>\n" +
            "    <title>" + REPORT_PROVIDER_NAME + "</title>\n" +
            "    <meta http-equiv=\"refresh\" content=\"0;URL='" + REPORT_PROVIDER_URL + "'\" />\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <p>Please wait</p> \n" +
            "    <p>Loading the report provider at <a href=\"" + REPORT_PROVIDER_URL + "\">\n" + "</a>.</p> \n" +
            "  </body>\n" +
            "</html>";

    private static String getHtml(String reportProviderName, URI reportProviderURI) {
        return HTML_TEMPLATE
                .replace(REPORT_PROVIDER_NAME, reportProviderName)
                .replace(REPORT_PROVIDER_URL, reportProviderURI.toString());
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return LoginRedirectEntity.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(LoginRedirectEntity loginRedirectEntity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        final UriBuilder uriBuilder = UriBuilder.fromUri(loginRedirectEntity.getInitiateLoginUri());

        uriBuilder.queryParam(ISS_PARAMETER, loginRedirectEntity.getIssuer());
        loginRedirectEntity.getLoginHint().ifPresent(loginHint -> uriBuilder.queryParam(LOGIN_HINT_PARAMETER, loginHint));
        loginRedirectEntity.getTargetLinkUri().ifPresent(targetLinkUri -> uriBuilder.queryParam(TARGET_LINK_URI_PARAMETER, targetLinkUri));

        entityStream.write(getHtml(loginRedirectEntity.clientName().orElse(DEFAULT_CLIENT_NAME),uriBuilder.build()).getBytes(StandardCharsets.UTF_8));
    }
}
