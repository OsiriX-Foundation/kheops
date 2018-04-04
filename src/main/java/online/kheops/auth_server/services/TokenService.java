package online.kheops.auth_server.services;


import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import online.kheops.auth_server.User;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Path("/")
public class TokenService
{
    static class TokenResponse {
        @XmlElement(name = "access_token")
        public String accessToken;
        @XmlElement(name = "token_type")
        public String tokenType;
        @XmlElement(name = "expires_in")
        public Long expiresIn;
    }

    static class ErrorResponse {
        public String error;
        @XmlElement(name = "error_description")
        public String errorDescription;
    }

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(@FormParam("grant_type") String grant_type, @FormParam("assertion") String assertion, @FormParam("scope") String scope)
            throws MalformedURLException, UnsupportedEncodingException
    {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.error = "invalid_grant";

        // FIXME: we should be getting the jwks_uri from https://accounts.google.com/.well-known/openid-configuration
        JwkProvider jwkProvider = new UrlJwkProvider(new URL("https://www.googleapis.com/oauth2/v3/certs"));

        if (grant_type.equals("urn:ietf:params:oauth:grant-type:jwt-bearer")) {

            try {
                RSAKeyProvider keyProvider = new RSAKeyProvider() {
                    @Override
                    public RSAPublicKey getPublicKeyById(String kid) {
                        try {
                            return (RSAPublicKey) jwkProvider.get(kid).getPublicKey();
                        } catch (JwkException exception) {
                            System.err.println(exception);
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

                // test to see if the user already exists,
                // add the user if the user does not exist

                EntityManagerFactory factory = Persistence.createEntityManagerFactory("online.kheops");
                EntityManager em = factory.createEntityManager();
                EntityTransaction tx = em.getTransaction();

                tx.begin();

                User user = null;
                try {
                    TypedQuery<User> query = em.createQuery("select u from User u where u.username = :username", User.class);
                    user = query.setParameter("username", jwt.getSubject()).getSingleResult();
                } catch (Exception e) {
                    System.out.println("User not found");
                    System.out.println(e);
                }

                if (user == null) {
                    System.out.println("Creating a new User");
                    user = new User(jwt.getSubject());
                    em.persist(user);
                }

                tx.commit();
                em.close();
                factory.close();

                // try to generate a new token

                String HMAC256Secret = "P47dnfP28ptS/uzuuvEACmPYdMiOtFNLXiWTIwNNPgUjrvTgF/JCh3qZi47sIcpeZaUXw132mfmR4q5K/fwepA==";
                Algorithm algorithmHMAC = Algorithm.HMAC256(HMAC256Secret);
                String token = JWT.create()
                        .withIssuer("auth.kheops.online")
                        .withSubject(jwt.getSubject())
                        .withAudience("dicom.kheops.online")
                        .withExpiresAt(Date.from(Instant.now().plus( 1 , ChronoUnit.HOURS )))
                        .withNotBefore(new Date())
                        .sign(algorithmHMAC);

                TokenResponse tokenResponse = new TokenResponse();
                tokenResponse.accessToken = token;
                tokenResponse.tokenType = "Bearer";
                tokenResponse.expiresIn = new Long(3600);
                return Response.ok(tokenResponse).build();

            } catch (JWTVerificationException exception) {
                errorResponse.errorDescription = "Unable to validate JWT";
                return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
            }
        }

        errorResponse.errorDescription = "Unknown grant type";
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }

}

