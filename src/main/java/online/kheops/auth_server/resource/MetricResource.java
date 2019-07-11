package online.kheops.auth_server.resource;


import online.kheops.auth_server.metric.MetricResponse;
import org.glassfish.jersey.process.internal.RequestContext;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import static javax.ws.rs.core.Response.Status.*;


@Path("/")
public class MetricResource {

    @Context
    private SecurityContext securityContext;

    @GET
    @Path("metrics")
    public Response test() {



        MetricResponse metricResponse = new MetricResponse();

        return Response.status(OK).entity(metricResponse).build();
    }
}
