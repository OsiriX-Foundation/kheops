package online.kheops.auth_server.util;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

public class ErrorResponse implements Serializable {

    public class Message {

        public static final String BAD_QUERY_PARAMETER = "Bad Query Parameter";
        public static final String BAD_FORM_PARAMETER = "Bad Form Parameter";
        public static final String USER_NOT_FOUND = "User Not Found";
        public static final String ALBUM_NOT_FOUND = "Album Not Found";
        public static final String WEBHOOK_NOT_FOUND = "Webhook Not Found";
        public static final String SERIES_NOT_FOUND = "Series Not Found";
        public static final String STUDY_NOT_FOUND = "Study Not Found";
        public static final String REPORT_PROVIDER_NOT_FOUND = "Report Provider Not Found";
        public static final String AUTHORIZATION_ERROR = "Authorization error";

    }

    public static class ErrorResponseBuilder {

        private String message;
        private String detail;
        private String help;

        public ErrorResponseBuilder() {
            this.help = "https://github.com/OsiriX-Foundation/KheopsAuthorization/wiki";
        }

        public ErrorResponseBuilder message (String message) {
            this.message = message;
            return this;
        }
        private String getMessage() { return message; }

        public ErrorResponseBuilder detail (String detail) {
            this.detail = detail;
            return this;
        }
        private String getDetail() { return detail; }

        private String getHelp() { return help; }

        public ErrorResponse build () {
            return new ErrorResponse(this);
        }

    }


    @XmlElement(name = "message")
    private String message;
    @XmlElement(name = "detail")
    private String detail;
    @XmlElement(name = "help")
    private String help;

    private ErrorResponse() {/*empty*/}

    private ErrorResponse(ErrorResponseBuilder errorResponseBuilder) {
        this.message = errorResponseBuilder.getMessage();
        this.detail = errorResponseBuilder.getDetail();
        this.help = errorResponseBuilder.getHelp();
    }

    @Override
    public String toString() {
        return "message='" + message + "', detail='" + detail + "'";
    }
}
