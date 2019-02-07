package online.kheops.auth_server.assertion;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import org.jose4j.json.internal.json_simple.JSONObject;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import java.util.logging.Level;

final class ViewerAssertion implements Assertion {
    private static final Builder BUILDER = new Builder();

    private final JsonObject jwe;
    private final String sub;
    private final String email;
    private final Assertion assertion;
    static final class Builder {

        ViewerAssertion build(String assertionToken) throws BadAssertionException {

            JsonReader jsonReader = Json.createReader(new StringReader(assertionToken));
            JsonObject jwe = jsonReader.readObject();

            return new ViewerAssertion(jwe);
        }
    }

    static Builder getBuilder() { return BUILDER; }

    private ViewerAssertion(JsonObject jwe) throws BadAssertionException {
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
}
