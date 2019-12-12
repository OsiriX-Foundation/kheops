package online.kheops.auth_server.util;

import javax.xml.bind.annotation.XmlElement;

public class ErrorResponse {

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
