package online.kheops.auth_server.series;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumSeries;
import online.kheops.auth_server.entity.Mutation;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.event.Events;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static online.kheops.auth_server.album.AlbumQueries.findAlbumSeriesByAlbumIDAndSeriesUID;
import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.AlbumsSeries.getAlbumSeries;
import static online.kheops.auth_server.series.SeriesQueries.*;

public class Series {

    private Series() {
        throw new IllegalStateException("Utility class");
    }

    public static void safeAttributeSetString(Attributes attributes, int tag, VR vr, String string) {
        if (string != null) {
            attributes.setString(tag, vr, string);
        }
    }

    public static online.kheops.auth_server.entity.Series getSeries(String studyInstanceUID, String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException{
        return findSeriesByStudyUIDandSeriesUID(studyInstanceUID,  seriesInstanceUID, em);
    }

    public static boolean seriesExist(String studyInstanceUID, String seriesInstanceUID, EntityManager em) {
        try {
            getSeries(studyInstanceUID,  seriesInstanceUID, em);
        } catch (SeriesNotFoundException e) {
            return false;
        }
        return true;
    }



    public static boolean canAccessSeries(User user, String studyInstanceUID, String seriesInstanceUID, EntityManager em) {
        try {
            findSeriesByStudyUIDandSeriesUID(user, studyInstanceUID,  seriesInstanceUID, em);
            return true;
        }catch (SeriesNotFoundException e) {
            return false;
        }
    }

    public static boolean canAccessSeries(Album album, String studyInstanceUID, String seriesInstanceUID, EntityManager em) {
        try {
            findSeriesByStudyUIDandSeriesUIDFromAlbum(album, studyInstanceUID,  seriesInstanceUID, em);
            return true;
        }catch (SeriesNotFoundException e) {
            return false;
        }
    }


    public static void editFavorites(User callingUser, String studyInstanceUID, String seriesInstanceUID, String fromAlbumId, boolean favorite)
            throws AlbumNotFoundException, SeriesNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Album album;
            if (fromAlbumId == null) {
                album = callingUser.getInbox();
            } else {
                album = getAlbum(fromAlbumId, em);
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

    public static boolean isFavorite(String seriesUID, User callingUser) {
        final EntityManager em = EntityManagerListener.createEntityManager();

        final boolean isFavorite;

        try {
            callingUser = em.merge(callingUser);
            isFavorite = isFavorite(seriesUID, callingUser.getInbox().getId());
        } finally {
            em.close();
        }
        return isFavorite;
    }

    public static boolean isFavorite(String seriesUID, String albumID) {
        final EntityManager em = EntityManagerListener.createEntityManager();

        final AlbumSeries albumSeries;
        try {
            albumSeries = findAlbumSeriesByAlbumIDAndSeriesUID(seriesUID, albumID, em);
        } catch (SeriesNotFoundException e) {
            throw new IllegalStateException("SeriesUID: "+seriesUID+"Not found inside the albumID: "+albumID);
        } finally {
            em.close();
        }
        return albumSeries.isFavorite();
    }

    public static boolean isSeriesInInbox(User callingUser, online.kheops.auth_server.entity.Series series, EntityManager em) {
        try {
            findSeriesBySeriesAndUserInbox(callingUser, series, em);
            return true;
        } catch (SeriesNotFoundException e) {
            return false;
        }
    }

}
