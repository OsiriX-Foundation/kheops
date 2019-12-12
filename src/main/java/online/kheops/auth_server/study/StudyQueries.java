package online.kheops.auth_server.study;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.ErrorResponse;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class StudyQueries {

    private StudyQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static Study findStudyByStudyUID(String studyInstanceUID, EntityManager em)
            throws StudyNotFoundException {

        try {
            TypedQuery<Study> query = em.createQuery("select s from Study s where s.studyInstanceUID = :StudyInstanceUID", Study.class);
            query.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Study not found")
                    .detail("Study does not exist or you don't have access")
                    .build();
            throw new StudyNotFoundException(errorResponse);
        }
    }

    public static Study findStudyByStudyandUser(Study study, User user, EntityManager em)
            throws StudyNotFoundException {

        try {
            TypedQuery<Study> query = em.createQuery("select st from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s join s.study st where u=:user and st = :study", Study.class);
            query.setParameter("study", study);
            query.setParameter("user", user);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Study not found")
                    .detail("Study does not exist or you don't have access")
                    .build();
            throw new StudyNotFoundException(errorResponse);
        }
    }

    public static Study findStudyByStudyandUserInbox(Study study, User user, EntityManager em)
            throws StudyNotFoundException {

        try {
            TypedQuery<Study> query = em.createQuery("select st from User u join u.albumUser au join au.album a join a.albumSeries alS join alS.series s join s.study st where u=:user and st = :study and a = u.inbox", Study.class);
            query.setParameter("study", study);
            query.setParameter("user", user);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Study not found")
                    .detail("Study not found in the inbox")
                    .build();
            throw new StudyNotFoundException(errorResponse);
        }
    }

    public static Study findStudyByStudyandAlbum(Study study, Album album, EntityManager em)
            throws StudyNotFoundException {

        try {
            TypedQuery<Study> query = em.createQuery("select st from Album a join a.albumSeries alS join alS.series s join s.study st where a=:album and st = :study", Study.class);
            query.setParameter("study", study);
            query.setParameter("album", album);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Study not found")
                    .detail("Study not found in the album")
                    .build();
            throw new StudyNotFoundException(errorResponse);
        }
    }

    public static Study findStudyByStudyandAlbum(String studyUID, Album album, EntityManager em)
            throws StudyNotFoundException {

        try {
            TypedQuery<Study> query = em.createQuery("select st from Album a join a.albumSeries alS join alS.series s join s.study st where a=:album and st.studyInstanceUID = :studyUID", Study.class);
            query.setParameter("studyUID", studyUID);
            query.setParameter("album", album);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Study not found")
                    .detail("Study not found in the album")
                    .build();
            throw new StudyNotFoundException(errorResponse);
        }
    }
}
