package online.kheops.auth_server.services;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.Actor;
import online.kheops.auth_server.Series;

@Path("/")
public class TokenService
{
    static class TokenResponse {
       public TokenResponse() {};

        private String access_token;
        private String token_type;
        private Long expires_in;

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public Long getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Long expires_in) {
            this.expires_in = expires_in;
        }
    }

    static class ErrorResponse {
        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        private String error;
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
            tokenResponse.setAccess_token("eyJhbGciOiJIUzI1NiIsImtpZCI6IjEifQ.eyJzdWIiOiIxMDQzOTE0ODIzNDkxNzE4Mzc1NzYifQ.zkqemWjCKVUqoRpPtoxUrocAw8uo63Q49");
            tokenResponse.setToken_type("Bearer");
            tokenResponse.setExpires_in(new Long(3600));


            return Response.ok(tokenResponse).build();

//            Actor actor = new Actor();
//            actor.setId(new Long(1));
//            actor.setName("actor");
//
//             return Response.ok(actor).build();
// return actor;
//
//            return new Object () {
//            };
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("invalid_grant");

        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();

//        System.out.println("About to throw");
//
//        throw new BadRequestException();
    }

}

