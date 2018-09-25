package online.kheops.auth_server.series;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static online.kheops.auth_server.series.SeriesQueries.findSeriesByPk;

public class Series {

    private Series() {
        throw new IllegalStateException("Utility class");
    }

    public static void checkValidUID(String uid, String name) {
        try {
            new Oid(uid);
        } catch (GSSException exception) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(name + " is not a valid UID").build());
        }
    }

    public static void safeAttributeSetString(Attributes attributes, int tag, VR vr, String string) {
        if (string != null) {
            attributes.setString(tag, vr, string);
        }
    }

    public static online.kheops.auth_server.entity.Series getSeries(Long seriesPk, EntityManager em) throws SeriesNotFoundException{
        try {
            return findSeriesByPk(seriesPk, em);
        } catch (NoResultException e) {
            throw new SeriesNotFoundException("StudyInstanceUID : "+seriesPk+" not found");
        }
    }

}
