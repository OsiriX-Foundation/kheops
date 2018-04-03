package online.kheops.auth_server.services;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.Series;

@Path("/")
public class TokenService
{
    static class TokenResponse {
        @XmlAttribute(name="access_token")
        public String accessToken;
        @XmlAttribute(name="token_type")
        public String tokenType;
        @XmlAttribute(name="expires_in")
        public Long expiresIn;
    }

    static class ErrorResponse {
        public String error;
        @XmlAttribute(name="error_description")
        public String errorDescription;
    }

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(@FormParam("grant_type") String grant_type, @FormParam("assertion") String assertion, @FormParam("scope") String scope) {
        System.out.println("Processing a token request");
        if (grant_type.equals("urn:ietf:params:oauth:grant-type:jwt-bearer")) {
            try {
                DecodedJWT jwt = JWT.decode(assertion);
                String subject = jwt.getSubject();
                System.out.println("token from: " + subject);
            } catch (JWTDecodeException exception){
                //Invalid token
            }

            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.accessToken = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjEifQ.eyJzdWIiOiIxMDQzOTE0ODIzNDkxNzE4Mzc1NzYifQ.zkqemWjCKVUqoRpPtoxUrocAw8uo63Q49";
            tokenResponse.tokenType = "Bearer";
            tokenResponse.expiresIn = new Long(3600);

            return Response.ok(tokenResponse).build();
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.error = "invalid_grant";
        errorResponse.errorDescription = "Unknown grant type";

        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }

}

