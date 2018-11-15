package online.kheops.auth_server.series;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumSeries;
import online.kheops.auth_server.entity.Mutation;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.event.Events;
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

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.AlbumsSeries.getAlbumSeries;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesByPk;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesByStudyUIDandSeriesUID;
import static online.kheops.auth_server.user.Users.getUser;

public class Series {

    private Series() {
        throw new IllegalStateException("Utility class");
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


    public static void editFavorites(Long callingUserPk, String studyInstanceUID, String seriesInstanceUID, Long fromAlbumPk, boolean favorite)
            throws UserNotFoundException, AlbumNotFoundException, SeriesNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album album;
            if (fromAlbumPk == null) {
                album = callingUser.getInbox();
            } else {
                album = getAlbum(fromAlbumPk, em);
            }
            final online.kheops.auth_server.entity.Series series = getSeries(studyInstanceUID, seriesInstanceUID, em);
            editSeriesFavorites(series, album, favorite, em);

            final Events.MutationType mutation;
            if (favorite) {
                mutation = Events.MutationType.ADD_FAV;
            } else {
                mutation = Events.MutationType.REMOVE_FAV;
            }
            final Mutation newAlbumMutation = Events.albumPostSeriesMutation(callingUser, album, mutation, series);
            em.persist(newAlbumMutation);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void editSeriesFavorites(online.kheops.auth_server.entity.Series series, Album album, boolean favorite, EntityManager em) {
        final AlbumSeries albumSeries = getAlbumSeries(album, series, em);
        albumSeries.setFavorite(favorite);
    }

}
