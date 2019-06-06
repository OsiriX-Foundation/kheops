package online.kheops.auth_server.accesstoken;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletContext;
import java.io.StringReader;
import java.util.Optional;

final class ViewerAccessToken implements AccessToken {

    private final JsonObject jwe;
    private final String sub;

    static final class Builder {
        private final ServletContext servletContext;

        private Builder(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        ViewerAccessToken build(String assertionToken)
                throws AccessTokenVerificationException {

            try(JsonReader jsonReader = Json.createReader(new StringReader(assertionToken))) {
                JsonObject jwe = jsonReader.readObject();
                return new ViewerAccessToken(servletContext, jwe);
            }
        }
    }

    static Builder getBuilder(ServletContext servletContext) { return new Builder(servletContext); }

    private ViewerAccessToken(ServletContext servletContext, JsonObject jwe)
            throws AccessTokenVerificationException {

        this.jwe = jwe;

        AccessToken accessToken = AccessTokenVerifier.authenticateAccessToken(servletContext, jwe.getString("token"));
        this.sub = accessToken.getSub();
    }

    @Override
    public Optional<JsonObject> getViewer() { return Optional.of(jwe); }

    @Override
    public boolean hasCapabilityAccess() {
        return false;
    }

    @Override
    public String getSub() {
        return sub;
    }

    @Override
    public TokenType getTokenType() { return TokenType.VIEWER_TOKEN; }
}
