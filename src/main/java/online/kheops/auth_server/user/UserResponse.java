package online.kheops.auth_server.user;

import javax.xml.bind.annotation.XmlElement;


public class UserResponse {

    private Response response;

    private static class Response {
        @XmlElement(name = "email")
        public String email;
        @XmlElement(name = "sub")
        public String sub;
    }

    public UserResponse(String email, String id) {
        response = new Response();
        response.email = email;
        response.sub = id;
    }

    public String getEmail() { return response.email; }

    public String getSub() { return response.sub; }

    public Response getResponse() { return response; }
}
