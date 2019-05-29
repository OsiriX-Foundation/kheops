package online.kheops.auth_server.util;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenErrorResponse {
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

        public String getErrorString() {
            return errorString;
        }
    }

    @XmlTransient
    private Error error;

    @XmlElement(name = "error_description")
    private String errorDescription;

    @XmlElement(name = "error")
    private String getErrorString() {
        return error.getErrorString();
    }

    @XmlTransient
    public Error getError() {
        return error;
    };

    @XmlTransient
    public String getErrorDescription() {
        return errorDescription;
    };

    public TokenErrorResponse() {
    }

    public TokenErrorResponse(Error error) {
        this.error = Objects.requireNonNull(error);
    }

    public TokenErrorResponse(Error error, String errorDescription) {
        this.error = Objects.requireNonNull(error);
        this.errorDescription = errorDescription;
    }

}
