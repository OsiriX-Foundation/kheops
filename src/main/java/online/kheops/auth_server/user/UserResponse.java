package online.kheops.auth_server.user;

import javax.xml.bind.annotation.XmlElement;


public class UserResponse {

    public static class Response {
        @XmlElement(name = "email")
        private String email;
        @XmlElement(name = "sub")
        private String sub;
    }

    private Response response;

    protected UserResponse(UserResponseBuilder userResponseBuilder) {
        response = new Response();
        response.email = userResponseBuilder.getEmail();
        response.sub = userResponseBuilder.getSub();
    }

    public String getEmail() { return response.email; }

    public String getSub() { return response.sub; }

    public Response getResponse() { return response; }
}


