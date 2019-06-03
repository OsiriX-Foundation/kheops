package online.kheops.auth_server.assertion;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletContext;
import java.io.StringReader;
import java.util.Optional;

final class ViewerAssertion implements Assertion {

    private final JsonObject jwe;
    private final String sub;

    static final class Builder {
        private final ServletContext servletContext;

        private Builder(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        ViewerAssertion build(String assertionToken)
                throws BadAssertionException {

            try(JsonReader jsonReader = Json.createReader(new StringReader(assertionToken))) {
                JsonObject jwe = jsonReader.readObject();
                return new ViewerAssertion(servletContext, jwe);
            }
        }
    }

    static Builder getBuilder(ServletContext servletContext) { return new Builder(servletContext); }

    private ViewerAssertion(ServletContext servletContext, JsonObject jwe)
            throws BadAssertionException {

        this.jwe = jwe;

        Assertion assertion = AssertionVerifier.createAssertion(servletContext, jwe.getString("token"));
        this.sub = assertion.getSub();
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
