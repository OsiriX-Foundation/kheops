package online.kheops.auth_server.event;

import online.kheops.auth_server.entity.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class EventQueries {

    private EventQueries() { throw new IllegalStateException("Utility class"); }

    public static List<Event> getEventsByAlbum(User user, Album album, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Event> query = em.createNamedQuery("Event.findAllByAlbum", Event.class);
        query.setParameter("album", album);
        query.setParameter("user", user);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static long getTotalEventsByAlbum(User user, Album album, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Event.countAllByAlbumAndUser", Long.class);
        query.setParameter("album", album);
        query.setParameter("user", user);
        return query.getSingleResult();
    }

    public static List<Comment> getCommentByAlbum(User user, Album album, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Comment> query = em.createNamedQuery("Comment.findAllByAlbum", Comment.class);
        query.setParameter("user", user);
        query.setParameter("album", album);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static long getTotalCommentsByAlbum(User user, Album album, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Comment.countAllByAlbumAndUser", Long.class);
        query.setParameter("album", album);
        query.setParameter("user", user);
        return query.getSingleResult();
    }

    public static List<Mutation> getMutationByAlbum(Album album, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Mutation> query = em.createNamedQuery("Mutation.findAllByAlbum", Mutation.class);
        query.setParameter("album", album);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static long getTotalMutationByAlbum(Album album, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Mutation.countAllByAlbum", Long.class);
        query.setParameter("album", album);
        return query.getSingleResult();
    }

    public static List<Comment> getCommentsByStudy(User user, String studyUID, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Comment> query = em.createNamedQuery("Comment.findAllByStudyUIDAndUser", Comment.class);
        query.setParameter("user", user);
        query.setParameter("studyUID", studyUID);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static long getTotalCommentsByStudy(User user, String studyUID, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Comment.countAllByStudyUIDAndUser", Long.class);
        query.setParameter("user", user);
        query.setParameter("studyUID", studyUID);
        return query.getSingleResult();
    }

    public static List<Comment> getPublicCommentsByStudy(String studyUID, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Comment> query = em.createNamedQuery("Comment.findAllPublicByStudyUID", Comment.class);
        query.setParameter("studyUID", studyUID);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static long getTotalPublicCommentsByStudy(String studyUID, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Comment.coundAllPublicByStudyUID", Long.class);
        query.setParameter("studyUID", studyUID);
        return query.getSingleResult();
    }
}
