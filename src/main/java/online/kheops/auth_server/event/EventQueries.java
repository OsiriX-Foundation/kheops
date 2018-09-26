package online.kheops.auth_server.event;

import online.kheops.auth_server.entity.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class EventQueries {

    private EventQueries() { throw new IllegalStateException("Utility class"); }

    public static List<Event> getEventsByAlbum(User user, Album album, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Event> query = em.createQuery("SELECT e from Event e where :album = e.album and (e.privateTargetUser = null or e.privateTargetUser = :user or e.user = :user) order by e.eventTime desc", Event.class);
        query.setParameter("album", album);
        query.setParameter("user", user);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static List<Comment> getCommentByAlbum(User user, Album album, EntityManager em, Integer offset, Integer limit) {
        TypedQuery<Comment> query = em.createQuery("SELECT c from Comment c where :album = c.album and (c.privateTargetUser = null or c.privateTargetUser = :user or c.user = :user) order by c.eventTime desc", Comment.class);
        query.setParameter("user", user);
        query.setParameter("album", album);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    /*public static long getNumbreOfComments(Album album, User user, EntityManager em) {
        Query query = em.createQuery("SELECT count(c) from Comment c where :album = c.album and c.privateTargetUser = null or c.user = :user or c.privateTargetUser = :user");
        query.setParameter("album", album);
        query.setParameter("user", user);
        return (long) query.getSingleResult();
    }*/

    public static List<Mutation> getMutationByAlbum(Album album, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Mutation> query = em.createQuery("SELECT m from Mutation m where :album = m.album order by m.eventTime desc", Mutation.class);
        query.setParameter("album", album);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static List<Comment> getCommentsByStudy(User user, String studyUID, EntityManager em, Integer offset, Integer limit) {
        TypedQuery<Comment> query = em.createQuery("SELECT c from Comment c where c.study =  :studyUID and (c.privateTargetUser = null or c.privateTargetUser = :user or c.user = :user) order by c.eventTime desc", Comment.class);
        query.setParameter("user", user);
        query.setParameter("studyUID", studyUID);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }
}
