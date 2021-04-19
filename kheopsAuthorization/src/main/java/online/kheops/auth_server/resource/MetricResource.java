package online.kheops.auth_server.resource;


import online.kheops.auth_server.metric.MetricResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static javax.ws.rs.core.Response.Status.*;


@Path("/")
public class MetricResource {

    @GET
    @Path("metrics")
    public Response test() {

        final MetricResponse metricResponse = new MetricResponse();
        return Response.status(OK).entity(metricResponse).build();
    }
}
