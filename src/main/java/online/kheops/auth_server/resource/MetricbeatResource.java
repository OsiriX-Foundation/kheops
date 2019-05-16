package online.kheops.auth_server.resource;


import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlElement;



import static javax.ws.rs.core.Response.Status.*;



@Path("/")
public class MetricbeatResource {


    static class MetricResponse {
        @XmlElement(name = "number_of_albums")
        public Long numberOfAlbums;
        @XmlElement(name = "number_of_users")
        public Long numberOfUsers;
    }


    @GET
    @Path("test")
    public Response test() {

        return Response.status(OK).entity("{\"boolean\": true,\"number\": 123,\"string\": \"Hello World\"}" ).build();
    }
}
