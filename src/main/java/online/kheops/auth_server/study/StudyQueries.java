package online.kheops.auth_server.study;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.util.Consts;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class StudyQueries {

    private StudyQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static Study findStudyByStudyUID(String studyInstanceUID, EntityManager em) throws NoResultException {
        TypedQuery<Study> query = em.createQuery("select s from Study s where s.studyInstanceUID = :StudyInstanceUID", Study.class);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
        return query.getSingleResult();
    }

    public static Study findStudyByStudyUIDandUser(String studyInstanceUID, User user, EntityManager em) throws NoResultException {
        TypedQuery<Study> query = em.createQuery("select st from User u join u.albumUser au join au.album a join a.series s  join s.study st where u=:user and a = u.inbox and st.studyInstanceUID = :StudyInstanceUID", Study.class);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
        query.setParameter("user", user);
        return query.getSingleResult();
    }

    public static Study findStudyByStudyandUser(Study study, User user, EntityManager em) throws NoResultException {
        TypedQuery<Study> query = em.createQuery("select st from User u join u.albumUser au join au.album a join a.series s join s.study st where u=:user and st = :study", Study.class);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setParameter("study", study);
        query.setParameter("user", user);
        return query.getSingleResult();
    }

    public static Study findStudyByStudyandAlbum(Study study, Album album, EntityManager em) throws NoResultException {
        TypedQuery<Study> query = em.createQuery("select st from Album a join a.series s join s.study st where a=:album and st = :study", Study.class);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setParameter("study", study);
        query.setParameter("album", album);
        return query.getSingleResult();
    }

}
