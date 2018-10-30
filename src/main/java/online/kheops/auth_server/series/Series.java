package online.kheops.auth_server.series;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.user.UserNotFoundException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static online.kheops.auth_server.series.SeriesQueries.findSeriesByPk;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesByStudyUIDandSeriesUID;
import static online.kheops.auth_server.user.Users.getUser;

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
            throw new SeriesNotFoundException("Series PK : "+seriesPk+" not found");
        }
    }

    public static online.kheops.auth_server.entity.Series getSeries(String studyInstanceUID, String seriesInstanceUID, EntityManager em) throws SeriesNotFoundException{
        try {
            return findSeriesByStudyUIDandSeriesUID(studyInstanceUID,  seriesInstanceUID, em);
        } catch (NoResultException e) {
            throw new SeriesNotFoundException("seriesInstanceUID : "+seriesInstanceUID+" not found");
        }
    }

    public static boolean canAccessSeries(User user, String studyInstanceUID, String seriesInstanceUID, EntityManager em) {
        try {
            findSeriesByStudyUIDandSeriesUID(user, studyInstanceUID,  seriesInstanceUID, em);
            return true;
        }catch (NoResultException e) {
            return false;
        }
    }

    public static boolean canAccessSeries(Album album, String studyInstanceUID, String seriesInstanceUID, EntityManager em) {
        try {
            findSeriesByStudyUIDandSeriesUID(album, studyInstanceUID,  seriesInstanceUID, em);
            return true;
        }catch (NoResultException e) {
            return false;
        }
    }


    public static void addToFavorites(Long callingUserPk, String studyInstanceUID, String seriesInstanceUID)
    throws UserNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album album = callingUser.getInbox();







            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void removeFromFavorites(Long callingUserPk, String studyInstanceUID, String seriesInstanceUID) {

    }

}
