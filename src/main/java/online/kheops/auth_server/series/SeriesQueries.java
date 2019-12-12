package online.kheops.auth_server.series;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.ErrorResponse;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static online.kheops.auth_server.util.Consts.StudyInstanceUID;

public class SeriesQueries {

    private SeriesQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Series> findSeriesListByStudyUIDFromInbox(User callingUser, String studyInstanceUID, EntityManager em) {
        TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s where a = u.inbox and u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID", Series.class);
        query.setParameter(StudyInstanceUID, studyInstanceUID);
        query.setParameter("callingUser", callingUser);
        return query.getResultList();
    }

    public static List<Series> findSeriesListByStudyUIDFromAlbum(Album album, String studyInstanceUID, EntityManager em) {
        TypedQuery<Series> query = em.createQuery("select s from Album a join a.albumSeries alS join alS.series s where :album = a and s.study.studyInstanceUID = :StudyInstanceUID", Series.class);
        query.setParameter(StudyInstanceUID,studyInstanceUID);
        query.setParameter("album",album);
        return query.getResultList();
    }

    public static List<Series> findSeriesListByStudyUIDFromAlbumAndInbox(User callingUser, String studyInstanceUID, EntityManager em) {
        TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s where u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID", Series.class);
        query.setParameter(StudyInstanceUID,studyInstanceUID);
        query.setParameter("callingUser",callingUser);
        return query.getResultList();
    }

    public static Series findSeriesByStudyUIDandSeriesUIDFromInbox(User callingUser, String studyInstanceUID, String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s where a = u.inbox and u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID and s.seriesInstanceUID = :SeriesInstanceUID", Series.class);
            query.setParameter(StudyInstanceUID, studyInstanceUID);
            query.setParameter(Consts.SeriesInstanceUID, seriesInstanceUID);
            query.setParameter("callingUser", callingUser);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Series not found")
                    .detail("The series does not exist in your inbox")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static Series findSeriesByStudyUIDandSeriesUIDFromAlbum(Album album, String studyInstanceUID, String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createQuery("select s from Album a join a.albumSeries alS join alS.series s where :album = a and s.study.studyInstanceUID = :StudyInstanceUID and s.seriesInstanceUID = :SeriesInstanceUID", Series.class);
            query.setParameter("album", album);
            query.setParameter(Consts.SeriesInstanceUID, seriesInstanceUID);
            query.setParameter(StudyInstanceUID, studyInstanceUID);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Series not found")
                    .detail("The series does not exist in the album")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }
    public static Series findSeriesByStudyUIDandSeriesUID(User callingUser, String studyInstanceUID, String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s where u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID and s.seriesInstanceUID = :SeriesInstanceUID", Series.class);
            query.setParameter(StudyInstanceUID, studyInstanceUID);
            query.setParameter(Consts.SeriesInstanceUID, seriesInstanceUID);
            query.setParameter("callingUser", callingUser);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Series not found")
                    .detail("The series does not exist or you don't have access")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static Series findSeriesByStudyUIDandSeriesUIDwithSharePermission(User callingUser, String studyInstanceUID, String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> seriesQuery = em.createQuery("select s from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s where (a = u.inbox or au.admin = true or a.sendSeries = true)and u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID and s.seriesInstanceUID = :SeriesInstanceUID", Series.class);
            seriesQuery.setParameter(StudyInstanceUID, studyInstanceUID);
            seriesQuery.setParameter(Consts.SeriesInstanceUID, seriesInstanceUID);
            seriesQuery.setParameter("callingUser", callingUser);
            return seriesQuery.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Series not found")
                    .detail("The series does not exist or you don't have the èermission to share it")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static Series findSeriesByStudyUIDandSeriesUID(String studyInstanceUID, String seriesInstanceUID, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createQuery("select s from Series s join s.study st where s.seriesInstanceUID = :SeriesInstanceUID and st.studyInstanceUID = :StudyInstanceUID", Series.class);
            query.setParameter(Consts.SeriesInstanceUID, seriesInstanceUID);
            query.setParameter(StudyInstanceUID, studyInstanceUID);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Series not found")
                    .detail("The series does not exist or you don't have access")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static Series findSeriesBySeriesAndAlbumWithSendPermission(User callingUser, Series series, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s where u=:callingUser and s = :series and (au.admin = true or a.sendSeries = true)", Series.class);
            query.setParameter("series", series);
            query.setParameter("callingUser", callingUser);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Series not found")
                    .detail("The series does not exist in your album or you don't hae the permission to share it")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static Series findSeriesBySeriesAndUserInbox(User callingUser, Series series, EntityManager em)
            throws SeriesNotFoundException {

        try {
            TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s where u=:callingUser and s = :series and a = u.inbox", Series.class);
            query.setParameter("series", series);
            query.setParameter("callingUser", callingUser);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Series not found")
                    .detail("The series does not exist in your inbox")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
    }

    public static boolean isOrphan(Series series, EntityManager em) {
        try {
            TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s where s = :series", Series.class);
            query.setParameter("series", series);
            query.getSingleResult();
            return false;
        } catch (NoResultException e) {
            return true;
        }
    }

    public static Set<String> findAllSeriesInstanceUIDbyStudyUIDfromAlbum(User callingUser, Album album, String studyInstanceUID, EntityManager em)
            throws StudyNotFoundException {

        try {
            TypedQuery<String> query = em.createQuery("select s.seriesInstanceUID from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s where s.study.studyInstanceUID = :StudyInstanceUID and u.inbox <> a and :user = u and a = :album", String.class);
            query.setParameter("album", album);
            query.setParameter(StudyInstanceUID, studyInstanceUID);
            query.setParameter("user", callingUser);
            return new HashSet<>(query.getResultList());
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Study not found")
                    .detail("Study not found in the album")
                    .build();
            throw new StudyNotFoundException(errorResponse);
        }
    }

    public static Set<String> findAllSeriesInstanceUIDbySeriesIUIDfromInbox(User callingUser, String studyInstanceUID, EntityManager em)
            throws StudyNotFoundException {

        try {
            TypedQuery<String> query = em.createQuery("select s.seriesInstanceUID from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s where s.study.studyInstanceUID = :StudyInstanceUID and u.inbox = a and :user = u", String.class);
            query.setParameter(StudyInstanceUID, studyInstanceUID);
            query.setParameter("user", callingUser);
            return new HashSet<>(query.getResultList());
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Study not found")
                    .detail("Study not found in the inbox")
                    .build();
            throw new StudyNotFoundException(errorResponse);
        }
    }

    public static Set<String> findAllSeriesInstanceUIDbySeriesIUIDfromAlbumandInbox(User callingUser, String studyInstanceUID, EntityManager em)
            throws StudyNotFoundException {

        try {
            TypedQuery<String> query = em.createQuery("select s.seriesInstanceUID from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s where s.study.studyInstanceUID = :StudyInstanceUID and :user = u", String.class);
            query.setParameter(StudyInstanceUID, studyInstanceUID);
            query.setParameter("user", callingUser);
            return new HashSet<>(query.getResultList());
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Study not found")
                    .detail("Study does not exist or you don't have access")
                    .build();
            throw new StudyNotFoundException(errorResponse);
        }
    }
}
