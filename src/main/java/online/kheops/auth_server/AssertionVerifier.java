package online.kheops.auth_server;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import online.kheops.auth_server.entity.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.*;

@SuppressWarnings("unused")
public class AssertionVerifier {
    private boolean verified = false;
    private String username = null;
    private String email = null;
    private String errorDescription = "Verification error";
    private boolean capabilityAssertion = false;

    private static final Logger LOG = LoggerFactory.getLogger(AssertionVerifier.class);

    private AssertionVerifier() {}

    public AssertionVerifier(final String assertion, final String grantType, final String superuserSecret) {

        if (grantType.equals("urn:ietf:params:oauth:grant-type:jwt-bearer")) {

            final String issuer;
            // find who this token is supposedly from
            try {
                DecodedJWT jwt = JWT.decode(assertion);
                issuer = jwt.getIssuer();
            } catch (JWTDecodeException exception) {
                return;
            }

            if (issuer == null) {
                return;
            }

            switch (issuer) {
                case "authorization.kheops.online":
                    try {
                        final Algorithm kheopsAlgorithmHMAC = Algorithm.HMAC256(superuserSecret);
                        JWTVerifier verifier = JWT.require(kheopsAlgorithmHMAC)
                                .withIssuer("authorization.kheops.online")
                                .build();
                        DecodedJWT jwt = verifier.verify(assertion);

                        if (jwt.getSubject() == null) {
                            throw new JWTVerificationException("No subject present in the token");
                        }
                        Claim emailClaim = jwt.getClaim("email");
                        if (emailClaim.isNull()) {
                            throw new JWTVerificationException("No email present in the token");
                        }
                        this.username = jwt.getSubject();
                        this.email = emailClaim.asString();
                        this.verified = true;
                    } catch (JWTVerificationException | UnsupportedEncodingException exception) {
                        return;
                    }
                    break;
                case "accounts.google.com":
                    try {
                        // FIXME: we should be getting the jwks_uri from https://accounts.google.com/.well-known/openid-configuration
                        JwkProvider jwkProvider = new UrlJwkProvider(new URL("https://www.googleapis.com/oauth2/v3/certs"));

                        RSAKeyProvider keyProvider = new RSAKeyProvider() {
                            @Override
                            public RSAPublicKey getPublicKeyById(String kid) {
                                try {
                                    return (RSAPublicKey) jwkProvider.get(kid).getPublicKey();
                                } catch (JwkException exception) {
                                    return null;
                                }
                            }
                            // implemented to get rid of warnings
                            @Override public RSAPrivateKey getPrivateKey() {return null;}
                            @Override public String getPrivateKeyId() {return null;}
                        };

                        Algorithm algorithmRSA = Algorithm.RSA256(keyProvider);
                        JWTVerifier verifier = JWT.require(algorithmRSA)
                                .withAudience("795653095144-nhfclj7mrb1h9n6tmdq2ugtj7ohkl3jq.apps.googleusercontent.com")
                                .withIssuer("accounts.google.com")
                                .build();
                        DecodedJWT jwt = verifier.verify(assertion);
                        if (jwt.getSubject() == null) {
                            throw new JWTVerificationException("No subject present in the token");
                        }
                        Claim emailClaim = jwt.getClaim("email");
                        if (emailClaim.isNull()) {
                            throw new JWTVerificationException("No email present in the token");
                        }
                        this.username = jwt.getSubject();
                        this.email = emailClaim.asString();
                        this.verified = true;
                    } catch (JWTVerificationException | MalformedURLException exception) {
                        return;
                    }
                    break;
                default:
                    this.errorDescription = "Unknown JWT issuer";
            }
        } else if (grantType.equals("urn:x-kheops:params:oauth:grant-type:capability")) {
            EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
            EntityManager em = factory.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                TypedQuery<Capability> query = em.createQuery("SELECT c FROM Capability c where c.secret = :secret", Capability.class);
                query.setParameter("secret", assertion);

                Capability capability;
                try {
                    capability = query.getSingleResult();
                } catch (NoResultException e) {
                    this.errorDescription = "Unknown capability";
                    return;
                }

                if (capability.isRevoked()) {
                    this.errorDescription = "Capability is revoked";
                    return;
                }

                if (ZonedDateTime.of(capability.getExpiration(), ZoneOffset.UTC).isBefore(ZonedDateTime.now())) {
                    this.errorDescription = "Capability is expired";
                    return;
                }

                this.username = capability.getUser().getGoogle_id();
                this.email = capability.getUser().getGoogle_email();
                this.verified = true;
                this.capabilityAssertion = true;

                tx.commit();
            } catch (Throwable t) {
                LOG.error("Error processing Capability", t);
            } finally {
                em.close();
                factory.close();
            }

        } else {
            this.errorDescription = "Unknown grant type";
        }
    }

    public boolean isVerfified() {
        return this.verified;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return email;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public boolean isCapabilityAssertion() {
        return capabilityAssertion;
    }
}

