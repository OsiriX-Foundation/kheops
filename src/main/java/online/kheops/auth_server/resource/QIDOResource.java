package online.kheops.auth_server.resource;

import com.mchange.v2.c3p0.C3P0Registry;
import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.entity.Pair;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.entity.User;
import org.dcm4che3.data.Attributes;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Context;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Path("/users")
public class QIDOResource {

    private static final Logger LOG = Logger.getLogger(TokenResource.class.getName());

    @Context
    private UriInfo uriInfo;

    @Context
    ServletContext context;

    private static DataSource getDataSource() {
        Iterator iterator = C3P0Registry.getPooledDataSources().iterator();

        if (!iterator.hasNext()) {
            throw new RuntimeException("No C3P0 DataSource available");
        }
        DataSource dataSource = (DataSource) iterator.next();
        if (iterator.hasNext()) {
            LOG.log(Level.SEVERE, "More than one C3P0 Datasource present, picked the first one");
        }

        return dataSource;
    }

    @GET
    @Secured
    @Path("/{user}/studies")
    @Produces("application/dicom+json")
    public Response getStudies(@PathParam("user") String username,
                               @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        List<Attributes> attributesList;
        Integer studiesTotalCount;

        try (Connection connection = getDataSource().getConnection()) {
                long targetUserPk = User.findPkByUsernameJOOQ(username, connection);
                if (callingUserPk != targetUserPk) {
                    return Response.status(Response.Status.FORBIDDEN).entity("Access to study denied").build();
                }

                Pair pair = Study.findAttributesByUserPKJOOQ(callingUserPk, uriInfo.getQueryParameters(), connection);
                studiesTotalCount = pair.getStudiesTotalCount();
                attributesList = pair.getAttributesList();
                LOG.info("QueryParameters : " + uriInfo.getQueryParameters().toString());
        }
        catch (BadRequestException e) {
            LOG.log(Level.SEVERE, "Error 400 :", e);
            return Response.status(Response.Status.BAD_REQUEST).entity("The QIDO-RS Provider was unable to perform the query because the Service Provider cannot understand the query component.").build();
        }
        catch (Exception e) {
            LOG.log(Level.SEVERE, "Error while connecting to the database", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database Connection Error").build();
        }

        GenericEntity<List<Attributes>> genericAttributesList = new GenericEntity<List<Attributes>>(attributesList) {};
        return Response.ok(genericAttributesList).header("X-Total-Count", studiesTotalCount).build();
    }
}
