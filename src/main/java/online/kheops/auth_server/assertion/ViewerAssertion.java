package online.kheops.auth_server.assertion;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.Optional;

final class ViewerAssertion implements Assertion {
    private static final Builder BUILDER = new Builder();

    private final JsonObject jwe;
    private final String sub;
    private final String email;
    private final Assertion assertion;
    static final class Builder {

        ViewerAssertion build(String assertionToken)
                throws BadAssertionException {

            try(JsonReader jsonReader = Json.createReader(new StringReader(assertionToken))) {
                JsonObject jwe = jsonReader.readObject();
                return new ViewerAssertion(jwe);
            }
        }
    }

    static Builder getBuilder() { return BUILDER; }

    private ViewerAssertion(JsonObject jwe)
            throws BadAssertionException {

        this.jwe = jwe;

        assertion = AssertionVerifier.createAssertion(jwe.getString("token"));
        this.sub = assertion.getSub();
        this.email = assertion.getEmail();
    }

    @Override
    public Optional<JsonObject> getViewer() { return Optional.of(jwe); }

    @Override
    public String getEmail() {
        return email;
    }

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
