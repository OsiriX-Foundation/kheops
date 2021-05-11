package online.kheops.auth_server.event;

import online.kheops.auth_server.entity.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static online.kheops.auth_server.util.JPANamedQueryConstants.*;


public class EventQueries {

    private EventQueries() { throw new IllegalStateException("Utility class"); }

    public static List<Event> getEventsByAlbum(User user, Album album, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Event> query = em.createNamedQuery("Event.findAllByAlbum", Event.class);
        query.setParameter(ALBUM, album);
        query.setParameter(USER, user);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static long getTotalEventsByAlbum(User user, Album album, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Event.countAllByAlbumAndUser", Long.class);
        query.setParameter(ALBUM, album);
        query.setParameter(USER, user);
        return query.getSingleResult();
    }

    public static List<Comment> getCommentByAlbum(User user, Album album, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Comment> query = em.createNamedQuery("Comment.findAllByAlbum", Comment.class);
        query.setParameter(USER, user);
        query.setParameter(ALBUM, album);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static long getTotalCommentsByAlbum(User user, Album album, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Comment.countAllByAlbumAndUser", Long.class);
        query.setParameter(ALBUM, album);
        query.setParameter(USER, user);
        return query.getSingleResult();
    }

    public static List<Mutation> getMutationByAlbum(Album album, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Mutation> query = em.createNamedQuery("Mutation.findAllByAlbum", Mutation.class);
        query.setParameter(ALBUM, album);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static long getTotalMutationByAlbum(Album album, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Mutation.countAllByAlbum", Long.class);
        query.setParameter(ALBUM, album);
        return query.getSingleResult();
    }

    public static long getTotalMutationByAlbum(String albumId, MutationQueryParams mutationQueryParams, EntityManager em) {

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Long> c = cb.createQuery(Long.class);
        final Root<Mutation> mutation = c.from(Mutation.class);
        c.select(cb.countDistinct(mutation));

        final List<Predicate> filters = new ArrayList<>();

        filters.add(cb.equal(mutation.get(Mutation_.album).get(Album_.id), albumId));

        mutationQueryParams.getReportProviders().ifPresent(lst -> filters.add(cb.or(mutation.join(Mutation_.reportProvider, JoinType.LEFT).get(ReportProvider_.clientId).in(lst))));
        mutationQueryParams.getSeries().ifPresent(lst -> filters.add(cb.or(mutation.join(Mutation_.series, JoinType.LEFT).get(Series_.seriesInstanceUID).in(lst))));
        mutationQueryParams.getStudies().ifPresent(lst -> filters.add(cb.or(mutation.join(Mutation_.study, JoinType.LEFT).get(Study_.studyInstanceUID).in(lst))));
        mutationQueryParams.getTypes().ifPresent(lst -> filters.add(cb.or(mutation.get(Mutation_.mutationType).in(lst))));
        mutationQueryParams.getCapabilityTokens().ifPresent(lst -> filters.add(cb.or(mutation.join(Mutation_.capability, JoinType.LEFT).get(Capability_.id).in(lst))));
        mutationQueryParams.getUsers().ifPresent(lst -> filters.add(cb.or(cb.or(mutation.join(Mutation_.user, JoinType.LEFT).get(User_.sub).in(lst)), cb.or(mutation.join(Mutation_.toUser, JoinType.LEFT).get(User_.sub).in(lst)))));

        mutationQueryParams.getStartDate().ifPresent(date -> filters.add(cb.greaterThanOrEqualTo(mutation.get(Mutation_.eventTime), date)));
        mutationQueryParams.getEndDate().ifPresent(date -> filters.add(cb.lessThanOrEqualTo(mutation.get(Mutation_.eventTime), date)));

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

        filters.add(cb.equal(mutation.get(Mutation_.album).get(Album_.id), albumId));

        mutationQueryParams.getReportProviders().ifPresent(lst -> filters.add(cb.or(mutation.join(Mutation_.reportProvider, JoinType.LEFT).get(ReportProvider_.clientId).in(lst))));
        mutationQueryParams.getSeries().ifPresent(lst -> filters.add(cb.or(mutation.join(Mutation_.series, JoinType.LEFT).get(Series_.seriesInstanceUID).in(lst))));
        mutationQueryParams.getStudies().ifPresent(lst -> filters.add(cb.or(mutation.join(Mutation_.study, JoinType.LEFT).get(Study_.studyInstanceUID).in(lst))));
        mutationQueryParams.getTypes().ifPresent(lst -> filters.add(cb.or(mutation.get(Mutation_.mutationType).in(lst))));
        mutationQueryParams.getCapabilityTokens().ifPresent(lst -> filters.add(cb.or(mutation.join(Mutation_.capability, JoinType.LEFT).get(Capability_.id).in(lst))));
        mutationQueryParams.getUsers().ifPresent(lst -> filters.add(cb.or(cb.or(mutation.join(Mutation_.user, JoinType.LEFT).get(User_.sub).in(lst)), cb.or(mutation.join(Mutation_.toUser, JoinType.LEFT).get(User_.sub).in(lst)))));

        mutationQueryParams.getStartDate().ifPresent(date -> filters.add(cb.greaterThanOrEqualTo(mutation.get(Mutation_.eventTime), date)));
        mutationQueryParams.getEndDate().ifPresent(date -> filters.add(cb.lessThanOrEqualTo(mutation.get(Mutation_.eventTime), date)));

        c.where(cb.and(filters.toArray(new Predicate[0])));
        c.orderBy(cb.desc(mutation.get(Mutation_.eventTime)));

        TypedQuery<Mutation> q = em.createQuery(c);
        q.setMaxResults(limit).setFirstResult(offset);
        return q.getResultList();
    }

    public static List<Comment> getCommentsByStudy(User user, String studyUID, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Comment> query = em.createNamedQuery("Comment.findAllByStudyUIDAndUser", Comment.class);
        query.setParameter(USER, user);
        query.setParameter(STUDY_UID, studyUID);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static long getTotalCommentsByStudy(User user, String studyUID, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Comment.countAllByStudyUIDAndUser", Long.class);
        query.setParameter(USER, user);
        query.setParameter(STUDY_UID, studyUID);
        return query.getSingleResult();
    }

    public static List<Comment> getPublicCommentsByStudy(String studyUID, Integer offset, Integer limit, EntityManager em) {
        TypedQuery<Comment> query = em.createNamedQuery("Comment.findAllPublicByStudyUID", Comment.class);
        query.setParameter(STUDY_UID, studyUID);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static long getTotalPublicCommentsByStudy(String studyUID, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Comment.coundAllPublicByStudyUID", Long.class);
        query.setParameter(STUDY_UID, studyUID);
        return query.getSingleResult();
    }

    public static void deleteAllEventsByAlbum (Album album, EntityManager em) {
        em.createNamedQuery("Event.deleteAllByAlbum")
                .setParameter(ALBUM, album)
                .executeUpdate();

    }
}
