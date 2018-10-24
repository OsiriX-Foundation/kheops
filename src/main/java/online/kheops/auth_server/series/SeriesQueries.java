package online.kheops.auth_server.series;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.util.Consts;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeriesQueries {

    private SeriesQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Series> findSeriesListByStudyUIDFromInbox(User callingUser, String studyInstanceUID, EntityManager em) {
        TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.series s where a = u.inbox and u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID", Series.class);
        query.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
        query.setParameter("callingUser", callingUser);
        return query.getResultList();
    }

    public static Series findSeriesByStudyUIDandSeriesUIDFromInbox(User callingUser, String studyInstanceUID, String seriesInstanceUID, EntityManager em) throws NoResultException {
        TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.series s where a = u.inbox and u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID and s.seriesInstanceUID = :SeriesInstanceUID", Series.class);
        query.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
        query.setParameter(Consts.SeriesInstanceUID, seriesInstanceUID);
        query.setParameter("callingUser", callingUser);
        return query.getSingleResult();
    }

    public static List<Series> findSeriesListByStudyUIDFromAlbum(User callingUser, Album album, String studyInstanceUID, EntityManager em) {
        TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.series s where a <> u.inbox and :album = a and u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID", Series.class);
        query.setParameter(Consts.StudyInstanceUID,studyInstanceUID);
        query.setParameter("callingUser",callingUser);
        query.setParameter("album",album);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }

    public static List<Series> findSeriesListByStudyUIDFromAlbumAndInbox(User callingUser, String studyInstanceUID, EntityManager em) {
        TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.series s where u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID", Series.class);
        query.setParameter(Consts.StudyInstanceUID,studyInstanceUID);
        query.setParameter("callingUser",callingUser);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }

    public static Series findSeriesByStudyUIDandSeriesUIDFromAlbum(User callingUser, Album album, String studyInstanceUID, String seriesInstanceUID, EntityManager em) throws NoResultException {
        TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.series s where a <> u.inbox and :album = a and u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID and s.seriesInstanceUID = :SeriesInstanceUID", Series.class);
        query.setParameter("callingUser",callingUser);
        query.setParameter("album",album);
        query.setParameter(Consts.SeriesInstanceUID,seriesInstanceUID);
        query.setParameter(Consts.StudyInstanceUID,studyInstanceUID);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        return query.getSingleResult();
    }
    public static Series findSeriesByStudyUIDandSeriesUID(User callingUser, String studyInstanceUID, String seriesInstanceUID, EntityManager em) throws NoResultException {
        TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.series s where u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID and s.seriesInstanceUID = :SeriesInstanceUID", Series.class);
        query.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
        query.setParameter(Consts.SeriesInstanceUID, seriesInstanceUID);
        query.setParameter("callingUser", callingUser);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        return query.getSingleResult();
    }

    public static Series findSeriesByStudyUIDandSeriesUIDwithSharePermission(User callingUser, String studyInstanceUID, String seriesInstanceUID, EntityManager em) throws NoResultException {
        TypedQuery<Series> seriesQuery = em.createQuery("select s from User u join u.albumUser au join au.album a join a.series s where (a = u.inbox or au.admin = true or a.sendSeries = true)and u=:callingUser and s.study.studyInstanceUID = :StudyInstanceUID and s.seriesInstanceUID = :SeriesInstanceUID", Series.class);
        seriesQuery.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
        seriesQuery.setParameter(Consts.SeriesInstanceUID, seriesInstanceUID);
        seriesQuery.setParameter("callingUser", callingUser);
        seriesQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        return seriesQuery.getSingleResult();
    }

    public static Series findSeriesByStudyUIDandSeriesUID(String studyInstanceUID, String seriesInstanceUID, EntityManager em) throws NoResultException {
        TypedQuery<Series> query = em.createQuery("select s from Series s join s.study st where s.seriesInstanceUID = :SeriesInstanceUID and st.studyInstanceUID = :StudyInstanceUID", Series.class);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setParameter(Consts.SeriesInstanceUID, seriesInstanceUID);
        query.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
        return query.getSingleResult();
    }

    public static Series findSeriesByPk(Long seriesPk, EntityManager em) throws NoResultException {
        TypedQuery<Series> query = em.createQuery("select s from Series s join s.study st where s.pk = :seriesPk", Series.class);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setParameter("seriesPk", seriesPk);
        return query.getSingleResult();
    }


    public static Series findSeriesBySeriesAndAlbumWithSendPermission(User callingUser, Series series, EntityManager em) throws NoResultException {
        TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.series s where u=:callingUser and s = :series and (au.admin = true or a.sendSeries = true)", Series.class);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setParameter("series", series);
        query.setParameter("callingUser", callingUser);
        return query.getSingleResult();
    }

    public static Series findSeriesBySeriesAndUserInbox(User callingUser, Series series, EntityManager em) throws NoResultException {
        TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.series s where u=:callingUser and s = :series and a = u.inbox", Series.class);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setParameter("series", series);
        query.setParameter("callingUser", callingUser);
        return query.getSingleResult();
    }

    public static boolean isOrphan(Series series, EntityManager em) {
        try {
            TypedQuery<Series> query = em.createQuery("select s from User u join u.albumUser au join au.album a join a.series s where s = :series", Series.class);
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            query.setParameter("series", series);
            query.getSingleResult();
            return false;
        } catch (NoResultException e) {
            return true;
        }
    }

    public static Set<String> findAllSeriesInstanceUIDbySeriesIUIDfromAlbum(User callingUser, Album album, String studyInstanceUID, EntityManager em) throws NoResultException {
        TypedQuery<String> query = em.createQuery("select s.seriesInstanceUID from User u join u.albumUser au join au.album a join a.series s where s.study.studyInstanceUID = :StudyInstanceUID and u.inbox <> a and :user = u and a = :album", String.class);
        query.setParameter("album", album);
        query.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
        query.setParameter("user", callingUser);
        return new HashSet<>(query.getResultList());
    }

    public static Set<String> findAllSeriesInstanceUIDbySeriesIUIDfromInbox(User callingUser, String studyInstanceUID, EntityManager em) throws NoResultException {
        TypedQuery<String> query = em.createQuery("select s.seriesInstanceUID from User u join u.albumUser au join au.album a join a.series s where s.study.studyInstanceUID = :StudyInstanceUID and u.inbox = a and :user = u", String.class);
        query.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
        query.setParameter("user", callingUser);
        return new HashSet<>(query.getResultList());
    }

    public static Set<String> findAllSeriesInstanceUIDbySeriesIUIDfromAlbumandInbox(User callingUser, String studyInstanceUID, EntityManager em) throws NoResultException {
        TypedQuery<String> query = em.createQuery("select s.seriesInstanceUID from User u join u.albumUser au join au.album a join a.series s where s.study.studyInstanceUID = :StudyInstanceUID and :user = u", String.class);
        query.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
        query.setParameter("user", callingUser);
        return new HashSet<>(query.getResultList());
    }

    public static Series findSeriesByStudyUIDandSeriesUID(Album album, String studyInstanceUID, String seriesInstanceUID, EntityManager em) throws NoResultException {
        TypedQuery<Series> seriesQuery = em.createQuery("select s from Album a join a.series s where a=:album and s.study.studyInstanceUID = :StudyInstanceUID and s.seriesInstanceUID = :SeriesInstanceUID", Series.class);
        seriesQuery.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
        seriesQuery.setParameter(Consts.SeriesInstanceUID, seriesInstanceUID);
        seriesQuery.setParameter("album", album);
        seriesQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        return seriesQuery.getSingleResult();
    }


}
