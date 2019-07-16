package online.kheops.auth_server.resource;


import online.kheops.auth_server.annotation.MetricSecured;
import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.metric.MetricResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;


@Path("/")
public class MetricResource {

    private static final Logger LOG = Logger.getLogger(MetricResource.class.getName());

    @GET
    @MetricSecured
    @Path("metrics")
    public Response test() {

        LOG.log(Level.INFO, "request metrics");

        final MetricResponse metricResponse = new MetricResponse();
        return Response.status(OK).entity(metricResponse).build();
    }
}
