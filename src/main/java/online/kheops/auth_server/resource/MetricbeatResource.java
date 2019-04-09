package online.kheops.auth_server.resource;


import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static javax.ws.rs.core.Response.Status.*;

@Path("/")
public class MetricbeatResource {
    
    @GET
    @Path("test")
    public Response test() {

        return Response.status(OK).entity("{\"boolean\": true,\"number\": 123,\"string\": \"Hello World\"}" ).build();
    }
}
