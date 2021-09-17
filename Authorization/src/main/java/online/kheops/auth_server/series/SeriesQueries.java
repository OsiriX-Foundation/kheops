package online.kheops.auth_server.series;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.*;

import static online.kheops.auth_server.util.ErrorResponse.Message.SERIES_NOT_FOUND;
import static online.kheops.auth_server.util.ErrorResponse.Message.STUDY_NOT_FOUND;
import static online.kheops.auth_server.util.JPANamedQueryConstants.*;

public class SeriesQueries {

    private SeriesQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Series> findSeriesListByStudyUIDFromInbox(User callingUser, String studyInstanceUID, EntityManager em) {
        TypedQuery<Series> query = em.createNamedQuery("Series.findAllByStudyUIDFromInbox", Series.class);
        query.setParameter(STUDY_UID, studyInstanceUID);
        query.setParameter(USER, callingUser);
        return query.getResultList();
    }

    public static List<Series> findSeriesListByStudyUIDFromAlbum(Album album, String studyInstanceUID, EntityManager em) {
        TypedQuery<Series> query = em.createNamedQuery("Series.findAllByStudyUIDFromAlbum", Series.class);
        query.setParameter(STUDY_UID,studyInstanceUID);
        query.setParameter(ALBUM,album);
        return query.getResultList();
    }

    public static List<Series> findSeriesListByStudyUIDFromAlbumAndInbox(User callingUser, String studyInstanceUID, EntityManager em) {
        TypedQuery<Series> query = em.createNamedQuery("Series.findAllByStudyUIDFromInboxAndAlbum", Series.class);
        query.setParameter(STUDY_UID,studyInstanceUID);
        query.setParameter(USER,callingUser);
        return query.getResultList();
    }

    public static List<Series> findSeriesListByStudyUID(String studyInstanceUID, EntityManager em) {
        TypedQuery<Series> query = em.createNamedQuery("Series.findSeriesByStudyUID", Series.class);
        query.setParameter(STUDY_UID,studyInstanceUID);
        return query.getResultList();
    }

    public static Series findSeriesByStudyUIDandSeriesUIDFromInbox(User callingUser, String studyInstanceUID, String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createNamedQuery("Series.findByStudyUIDFromInbox", Series.class);
            query.setParameter(STUDY_UID, studyInstanceUID);
            query.setParameter(SERIES_UID, seriesInstanceUID);
            query.setParameter(USER, callingUser);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("The series does not exist in your inbox")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static Series findSeriesByStudyUIDandSeriesUIDFromAlbum(Album album, String studyInstanceUID, String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createNamedQuery("Series.findByStudyUIDFromAlbum", Series.class);
            query.setParameter(ALBUM, album);
            query.setParameter(SERIES_UID, seriesInstanceUID);
            query.setParameter(STUDY_UID, studyInstanceUID);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("The series does not exist in the album")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }
    public static Series findSeriesByStudyUIDandSeriesUID(User callingUser, String studyInstanceUID, String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createNamedQuery("Series.findBySeriesUIDAndStudyUIDAndUser", Series.class);
            query.setParameter(STUDY_UID, studyInstanceUID);
            query.setParameter(SERIES_UID, seriesInstanceUID);
            query.setParameter(USER, callingUser);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("The series does not exist or you don't have access")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static Series findSeriesByStudyUIDandSeriesUIDwithSharePermission(User callingUser, String studyInstanceUID, String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> seriesQuery = em.createNamedQuery("Series.findBySeriesUIDAndStudyUIDAndUserWithSharePermission", Series.class);
            seriesQuery.setParameter(STUDY_UID, studyInstanceUID);
            seriesQuery.setParameter(SERIES_UID, seriesInstanceUID);
            seriesQuery.setParameter(USER, callingUser);
            return seriesQuery.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("The series does not exist or you don't have the permission to share it")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static Series findSeriesByStudyUIDandSeriesUID(String studyInstanceUID, String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createNamedQuery("Series.findBySeriesUIDAndStudyUID", Series.class);
            query.setParameter(SERIES_UID, seriesInstanceUID);
            query.setParameter(STUDY_UID, studyInstanceUID);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("The series does not exist or you don't have access")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static Series findSeriesBySeriesUID(String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createNamedQuery("Series.findBySeriesUID", Series.class);
            query.setParameter(SERIES_UID, seriesInstanceUID);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("The series does not exist or you don't have access")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static Series findSeriesBySeriesAndAlbumWithSendPermission(User callingUser, Series series, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createNamedQuery("Series.findBySeriesAndUserWithSharePermission", Series.class);
            query.setParameter(SERIES, series);
            query.setParameter(USER, callingUser);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("The series does not exist in your albums or you don't hae the permission to share it")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static Series findSeriesBySeriesAndUserInbox(User callingUser, Series series, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createNamedQuery("Series.findBySeriesFromInbox", Series.class);
            query.setParameter(SERIES, series);
            query.setParameter(USER, callingUser);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("The series does not exist in your inbox")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static boolean isOrphan(Series series, EntityManager em) {
        try {
            TypedQuery<Series> query = em.createNamedQuery("Series.isOrphan", Series.class);
            query.setParameter(SERIES, series);
            query.getSingleResult();
            return false;
        } catch (NoResultException e) {
            return true;
        }
    }

    public static Map<String, Boolean> findAllSeriesInstanceUIDbyStudyUIDfromAlbum(User callingUser, Album album, String studyInstanceUID, EntityManager em)
            throws StudyNotFoundException {
        try {
            TypedQuery<SeriesUIDFavoritePair> query = em.createNamedQuery("Series.findAllUIDByStudyUIDFromAlbum", SeriesUIDFavoritePair.class);
            query.setParameter(ALBUM, album);
            query.setParameter(STUDY_UID, studyInstanceUID);
            query.setParameter(USER, callingUser);
            Set<SeriesUIDFavoritePair> seriesUIDFavoritePairSet = new HashSet<>(query.getResultList());
            final Map<String, Boolean> seriesUIDFavoritePairMap = new HashMap<>();
            for(SeriesUIDFavoritePair seriesUIDFavoritePair : seriesUIDFavoritePairSet) {
                seriesUIDFavoritePairMap.put(seriesUIDFavoritePair.getSeriesUID(), seriesUIDFavoritePair.favorite);
            }
            return seriesUIDFavoritePairMap;
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(STUDY_NOT_FOUND)
                    .detail("Study not found in the album")
                    .build();
            throw new StudyNotFoundException(errorResponse);
        }
    }

    public static Map<String, Boolean> findAllSeriesInstanceUIDbySeriesIUIDfromInbox(User callingUser, String studyInstanceUID, EntityManager em)
            throws StudyNotFoundException {

        try {
            TypedQuery<SeriesUIDFavoritePair> query = em.createNamedQuery("Series.findAllUIDByStudyUIDFromInbox", SeriesUIDFavoritePair.class);
            query.setParameter(STUDY_UID, studyInstanceUID);
            query.setParameter(USER, callingUser);
            Set<SeriesUIDFavoritePair> seriesUIDFavoritePairSet = new HashSet<>(query.getResultList());
            final Map<String, Boolean> seriesUIDFavoritePairMap = new HashMap<>();
            for(SeriesUIDFavoritePair seriesUIDFavoritePair : seriesUIDFavoritePairSet) {
                seriesUIDFavoritePairMap.put(seriesUIDFavoritePair.getSeriesUID(), seriesUIDFavoritePair.favorite);
            }
            return seriesUIDFavoritePairMap;
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(STUDY_NOT_FOUND)
                    .detail("Study not found in the inbox")
                    .build();
            throw new StudyNotFoundException(errorResponse);
        }
    }

    public static Map<String, Boolean> findAllSeriesInstanceUIDbySeriesIUIDfromAlbumandInbox(User callingUser, String studyInstanceUID, EntityManager em)
            throws StudyNotFoundException {

        try {
            TypedQuery<SeriesUIDFavoritePair> query = em.createNamedQuery("Series.findAllUIDByStudyUIDFromInboxAndAlbum", SeriesUIDFavoritePair.class);
            query.setParameter(STUDY_UID, studyInstanceUID);
            query.setParameter(USER, callingUser);
            Set<SeriesUIDFavoritePair> seriesUIDFavoritePairSet = new HashSet<>(query.getResultList());
            final Map<String, Boolean> seriesUIDFavoritePairMap = new HashMap<>();
            for(SeriesUIDFavoritePair seriesUIDFavoritePair : seriesUIDFavoritePairSet) {
                seriesUIDFavoritePairMap.put(seriesUIDFavoritePair.getSeriesUID(), seriesUIDFavoritePair.favorite);
            }
            return seriesUIDFavoritePairMap;
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(STUDY_NOT_FOUND)
                    .detail("Study does not exist or you don't have access")
                    .build();
            throw new StudyNotFoundException(errorResponse);
        }
    }

    public static void deleteSeriesList(final List<Series> seriesToDelete, final EntityManager em) {
        for (Series series : seriesToDelete) {
            em.remove(series);
        }
    }
}