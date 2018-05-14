package online.kheops.auth_server.resource;

import online.kheops.auth_server.annotation.Secured;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/users")
public class CapabilitiesResource {

    @POST
    @Secured
    @Path("/{user}/capabilities")
    public Response putStudy(@PathParam("user") String username,
                             @Context SecurityContext securityContext) {



    }
}
