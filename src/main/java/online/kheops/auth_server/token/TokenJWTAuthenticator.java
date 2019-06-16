package online.kheops.auth_server.token;

import com.auth0.jwk.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import online.kheops.auth_server.report_provider.ClientIdNotFoundException;
import online.kheops.auth_server.report_provider.ReportProviderUriNotValidException;
import online.kheops.auth_server.report_provider.ReportProviders;

import javax.servlet.ServletContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

import static online.kheops.auth_server.token.TokenRequestException.Error.INVALID_REQUEST;

public class TokenJWTAuthenticator {
    private static final String HOST_ROOT_PARAMETER = "online.kheops.root.uri";
    private static final String RS256 = "RS256";
    private static final Client CLIENT = ClientBuilder.newClient();

    final private ServletContext context;
    private String clientId;
    private String clientJWT;
    private DecodedJWT decodedJWT;

    private static class ConfigurationEntity {
        @XmlElement(name="jwks_uri")
        String jwksURI;
    }

    static TokenJWTAuthenticator newAuthenticator(final ServletContext context) {
        return new TokenJWTAuthenticator(context);
    }

    private TokenJWTAuthenticator(ServletContext context) {
        this.context = context;
    }

    TokenJWTAuthenticator clientId(final String clientId) {
        this.clientId = Objects.requireNonNull(clientId);
        return this;
    }

    TokenJWTAuthenticator clientJWT(final String clientJWT) {
        this.clientJWT = Objects.requireNonNull(clientJWT);

        try {
            decodedJWT = JWT.decode(clientJWT);
        } catch (JWTDecodeException e) {
            throw new TokenRequestException(INVALID_REQUEST, "Unable to verify the JWT. " + e.getMessage(), e);
        }

        return this;
    }

    TokenPrincipal authenticate() {
        Objects.requireNonNull(clientId);
        Objects.requireNonNull(clientJWT);
        Objects.requireNonNull(decodedJWT);
        basicValidation();

        RSAPublicKey publicKey = getPublicKey();
        final RSAKeyProvider keyProvider = new RSAKeyProvider() {
            @Override
            public RSAPublicKey getPublicKeyById(String kid) {
                return publicKey;
            }

            @Override
            public RSAPrivateKey getPrivateKey() {
                return null;
            }

            @Override
            public String getPrivateKeyId() {
                return null;
            }
        };

        try {
            JWT.require(Algorithm.RSA256(keyProvider))
                    .acceptLeeway(5)
                    .withIssuer(getConfigurationIssuer())
                    .withSubject(clientId)
                    .withAudience(getAudienceHost())
                    .build().verify(clientJWT);
        } catch (JWTVerificationException e) {
            throw new TokenRequestException(INVALID_REQUEST, "Unable to verify the JWT. " + e.getMessage() , e);
        }

        return new TokenPrincipal() {
            @Override
            public TokenClientKind getClientKind() {
                return TokenClientKind.REPORT_PROVIDER;
            }

            @Override
            public String getName() {
                return clientId;
            }
        };
    }

    private void basicValidation() {
        Objects.requireNonNull(decodedJWT);

        if (!decodedJWT.getAlgorithm().equals(RS256)) {
            throw new TokenRequestException(INVALID_REQUEST, "Unknown JWT signing algorithm: " + decodedJWT.getAlgorithm());
        }

        if (decodedJWT.getId() == null) {
            throw new TokenRequestException(INVALID_REQUEST, "Token id (jti) claim is required");
        }

        if (decodedJWT.getKeyId() == null) {
            throw new TokenRequestException(INVALID_REQUEST, "Token id (kid) header is required");
        }

        Date expDate = decodedJWT.getExpiresAt();
        if (expDate == null) {
            throw new TokenRequestException(INVALID_REQUEST, "Expiration date (exp) claim is required");
        }

        if (expDate.after(Date.from(Instant.now().plus(10, ChronoUnit.MINUTES)))) {
            throw new TokenRequestException(INVALID_REQUEST, "Expiration date (exp) claim is too far in the future");
        }
    }

    private String getKeyId() {
        Objects.requireNonNull(decodedJWT);
        return decodedJWT.getKeyId();
    }

    private RSAPublicKey getPublicKey() {
        try {
            JwkProvider provider = new UrlJwkProvider(getJWKSUri().toURL());
            return (RSAPublicKey) provider.get(getKeyId()).getPublicKey();
        } catch (JwkException e) {
            throw new TokenRequestException(INVALID_REQUEST, "Unable to get public key." + e.getMessage(), e);
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new TokenRequestException(INVALID_REQUEST, "Bad configuration URI", e);
        }
    }

    private String getConfigurationIssuer() {
        Objects.requireNonNull(clientId);

        try {
            return ReportProviders.getConfigIssuer(ReportProviders.getReportProvider(clientId));
        } catch (ClientIdNotFoundException e) {
            throw new TokenRequestException(INVALID_REQUEST, "Unknown clientID", e);
        } catch (ReportProviderUriNotValidException e) {
            throw new TokenRequestException(INVALID_REQUEST, "Configuration URI not valid", e);
        }
    }

    private String getAudienceHost() {
        return context.getInitParameter(HOST_ROOT_PARAMETER);
    }

    private URI getConfigurationURI() {
        Objects.requireNonNull(clientId);

        final URI configurationUri;
        try {
            configurationUri = new URI((ReportProviders.getReportProvider(clientId).getUrl()));
        } catch (ClientIdNotFoundException e) {
            throw new TokenRequestException(INVALID_REQUEST, "Unknown clientID", e);
        } catch (URISyntaxException e) {
            throw new TokenRequestException(INVALID_REQUEST, "Bad configuration URI Syntax", e);
        }

//        if (!configurationUri.getScheme().equals("https") && !configurationUri.getHost().equals("localhost")) {
//            throw new TokenRequestException(INVALID_REQUEST, "Non https jwks URIs are only allowed for localhost");
//        }

        return configurationUri;
    }

    private URI getJWKSUri() {
        final ConfigurationEntity configurationEntity;
        try {
            configurationEntity = CLIENT.target(getConfigurationURI()).request(MediaType.APPLICATION_JSON).get(ConfigurationEntity.class);
        } catch (WebApplicationException | ProcessingException e) {
            throw new TokenRequestException(INVALID_REQUEST, "Unable to obtain the jwks_uri", e);
        }

        final URI jwksUri;
        try {
            jwksUri = new URI(configurationEntity.jwksURI);
        } catch (URISyntaxException e) {
            throw new TokenRequestException(INVALID_REQUEST, "jwks_uri is not a valid URI", e);
        }

//        if (!jwksUri.getScheme().equals("https") && !jwksUri.getHost().equals("localhost")) {
//            throw new TokenRequestException(INVALID_REQUEST, "Non https jwks URIs are only allowed for localhost");
//        }

        return jwksUri;
    }
}
