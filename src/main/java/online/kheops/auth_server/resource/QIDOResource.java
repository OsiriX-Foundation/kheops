package online.kheops.auth_server.resource;

import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.annotation.Secured;;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.entity.User;
import org.dcm4che3.data.Attributes;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Context;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;


@Path("/users")
public class QIDOResource {

    private static final Logger LOG = Logger.getLogger(TokenResource.class.getName());

    @Context
    private UriInfo uriInfo;

    @Context
    ServletContext context;

    @GET
    @Secured
    @Path("/{user}/studies")
    @Produces("application/dicom+json")
    public Response getStudies(@PathParam("user") String username,
                               @Context SecurityContext securityContext) {

        // for now don't use any parameters

        // get a list of all the studies
        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        // is the user sharing a series, or requesting access to a new series


        List<Attributes> attributesList = new ArrayList<>();


        final String user = context.getInitParameter("online.kheops.jdbc.user");
        final String password = context.getInitParameter("online.kheops.jdbc.password");
        final String url = context.getInitParameter("online.kheops.jdbc.url");

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
                // Connection is the only JDBC resource that we need
                // PreparedStatement and ResultSet are handled by jOOQ, internally

                long targetUserPk = User.findPkByUsernameJOOQ(username, conn);
                if (callingUserPk != targetUserPk) {
                    return Response.status(Response.Status.FORBIDDEN).entity("Access to study denied").build();
                }

                attributesList = Study.findAttributesByUserPKJOOQ(callingUserPk, uriInfo.getQueryParameters(), conn);
                LOG.info("QueryParameters : " + uriInfo.getQueryParameters().toString());
        } catch (Exception e) {/*empty*/}

        GenericEntity<List<Attributes>> genericAttributesList = new GenericEntity<List<Attributes>>(attributesList) {};
        return Response.ok(genericAttributesList).build();
    }
}
