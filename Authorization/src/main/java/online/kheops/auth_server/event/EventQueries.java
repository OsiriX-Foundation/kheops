package online.kheops.auth_server.event;

import online.kheops.auth_server.entity.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
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

    public static long getTotalMutationByAlbum(String albumId, MutationQueryParams mutationQueryParams, EntityManager em) {

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Long> c = cb.createQuery(Long.class);
        final Root<Mutation> mutation = c.from(Mutation.class);
        c.select(cb.countDistinct(mutation));

        final List<Predicate> filters = new ArrayList<>();

        filters.add(cb.equal(mutation.get("album").get("id"), albumId));

        mutationQueryParams.getReportProviders().ifPresent(lst -> filters.add(cb.or(mutation.join("reportProvider", JoinType.LEFT).get("clientId").in(lst))));
        mutationQueryParams.getSeries().ifPresent(lst -> filters.add(cb.or(mutation.join("series", JoinType.LEFT).get("seriesInstanceUID").in(lst))));
        mutationQueryParams.getStudies().ifPresent(lst -> filters.add(cb.or(mutation.join("study", JoinType.LEFT).get("studyInstanceUID").in(lst))));
        mutationQueryParams.getTypes().ifPresent(lst -> filters.add(cb.or(mutation.get("mutationType").in(lst))));
        mutationQueryParams.getCapabilityTokens().ifPresent(lst -> filters.add(cb.or(mutation.join("capability", JoinType.LEFT).get("id").in(lst))));
        mutationQueryParams.getUsers().ifPresent(lst -> filters.add(cb.or(cb.or(mutation.join("user", JoinType.LEFT).get("sub").in(lst)), cb.or(mutation.join("toUser", JoinType.LEFT).get("sub").in(lst)))));

        mutationQueryParams.getStartDate().ifPresent(date -> filters.add(cb.greaterThanOrEqualTo(mutation.get("eventTime"), date)));
        mutationQueryParams.getEndDate().ifPresent(date -> filters.add(cb.lessThanOrEqualTo(mutation.get("eventTime"), date)));

        c.where(cb.and(filters.toArray(new Predicate[0])));

        TypedQuery<Long> q = em.createQuery(c);
        return q.getSingleResult();
    }

    public static List<Mutation> getMutationByAlbum(String albumId, MutationQueryParams mutationQueryParams, Integer offset, Integer limit, EntityManager em) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Mutation> c = cb.createQuery(Mutation.class);
        final Root<Mutation> mutation = c.from(Mutation.class);
        c.select(mutation);
        c.distinct(true);

        final List<Predicate> filters = new ArrayList<>();

        filters.add(cb.equal(mutation.get("album").get("id"), albumId));

        mutationQueryParams.getReportProviders().ifPresent(lst -> filters.add(cb.or(mutation.join("reportProvider", JoinType.LEFT).get("clientId").in(lst))));
        mutationQueryParams.getSeries().ifPresent(lst -> filters.add(cb.or(mutation.join("series", JoinType.LEFT).get("seriesInstanceUID").in(lst))));
        mutationQueryParams.getStudies().ifPresent(lst -> filters.add(cb.or(mutation.join("study", JoinType.LEFT).get("studyInstanceUID").in(lst))));
        mutationQueryParams.getTypes().ifPresent(lst -> filters.add(cb.or(mutation.get("mutationType").in(lst))));
        mutationQueryParams.getCapabilityTokens().ifPresent(lst -> filters.add(cb.or(mutation.join("capability", JoinType.LEFT).get("id").in(lst))));
        mutationQueryParams.getUsers().ifPresent(lst -> filters.add(cb.or(cb.or(mutation.join("user", JoinType.LEFT).get("sub").in(lst)), cb.or(mutation.join("toUser", JoinType.LEFT).get("sub").in(lst)))));

        mutationQueryParams.getStartDate().ifPresent(date -> filters.add(cb.greaterThanOrEqualTo(mutation.get("eventTime"), date)));
        mutationQueryParams.getEndDate().ifPresent(date -> filters.add(cb.lessThanOrEqualTo(mutation.get("eventTime"), date)));

        c.where(cb.and(filters.toArray(new Predicate[0])));
        c.orderBy(cb.desc(mutation.get("eventTime")));

        TypedQuery<Mutation> q = em.createQuery(c);
        q.setMaxResults(limit).setFirstResult(offset);
        return q.getResultList();
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
