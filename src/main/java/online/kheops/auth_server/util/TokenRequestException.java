package online.kheops.auth_server.util;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class TokenRequestException extends BadRequestException {

    public enum Error {
        INVALID_REQUEST("invalid_request"),
        INVALID_CLIENT("invalid_client"),
        INVALID_GRANT("invalid_grant"),
        UNAUTHORIZED_CLIENT("unauthorized_client"),
        UNSUPPORTED_GRANT_TYPE("unsupported_grant_type"),
        INVALID_SCOPE("invalid_scope");

        private String errorString;

        Error(String errorString) {
            this.errorString = errorString;
        }

        public String toString() {
            return errorString;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class TokenErrorResponse {

        @XmlElement(name = "error")
        private String error;

        @XmlElement(name = "error_description")
        private String errorDescription;

        public TokenErrorResponse() {}

        private TokenErrorResponse(String error, String errorDescription) {
            this.error = error;
            this.errorDescription = errorDescription;
        }

    }

    public TokenRequestException(Error error) {
        super(Response.status(BAD_REQUEST).entity(new TokenErrorResponse(error.toString(), null)).build());
    }

    public TokenRequestException(Error error, Throwable throwable) {
        super(Response.status(BAD_REQUEST).entity(new TokenErrorResponse(error.toString(), null)).build(), throwable);
    }

    public TokenRequestException(Error error, String errorMessage) {
        super(Response.status(BAD_REQUEST).entity(new TokenErrorResponse(error.toString(), errorMessage)).build());
    }

    public TokenRequestException(Error error, String errorMessage, Throwable throwable) {
        super(Response.status(BAD_REQUEST).entity(new TokenErrorResponse(error.toString(), errorMessage)).build(), throwable);
    }
}
