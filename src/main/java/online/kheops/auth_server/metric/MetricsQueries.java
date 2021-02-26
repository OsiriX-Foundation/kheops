package online.kheops.auth_server.metric;

import online.kheops.auth_server.capability.ScopeType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

class MetricsQueries {

    private MetricsQueries() { /*empty*/ }

    static Long getNumberOfAlbumsIncludeInbox(EntityManager em) {
        return em.createQuery("SELECT COUNT(a) FROM Album a", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfAlbumsFav(EntityManager em) {
        return em.createQuery("SELECT COUNT(au) FROM AlbumUser au WHERE au.favorite = true", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfUsers(EntityManager em) {
        return em.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfReportProviders(EntityManager em) {
        return em.createQuery("SELECT COUNT(rp) FROM ReportProvider rp", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfStudies(EntityManager em) {
        return em.createQuery("SELECT COUNT(s) FROM Study s", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfOrphanSeries(EntityManager em) {
        return em.createQuery("SELECT COUNT(s.pk) FROM Series s LEFT JOIN s.albumsSeries als WHERE als.album = null", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfUnpopulatedSeries (EntityManager em) {
        return em.createQuery("SELECT COUNT(s) FROM Series s WHERE s.populated = false", Long.class)
                .getSingleResult();
    }
    static Long getNumberOfUnpopulatedStudies (EntityManager em) {
        return em.createQuery("SELECT COUNT(s) FROM Study s WHERE s.populated = false", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfSeries(EntityManager em) {
        return em.createQuery("SELECT COUNT(s) FROM Series s", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfInstances(EntityManager em) {
        return em.createQuery("SELECT COALESCE(SUM(s.numberOfSeriesRelatedInstances), 0) FROM Series s", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfSeriesFav(EntityManager em) {
        return em.createQuery("SELECT COUNT(als) FROM AlbumSeries als WHERE als.favorite = true", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfComments(EntityManager em) {
        return em.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.album = NULL", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfCommentsPublic(EntityManager em) {
        return em.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.album = NULL AND c.privateTargetUser = NULL", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfCommentsPrivate(EntityManager em) {
        return em.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.album = NULL AND c.privateTargetUser <> NULL", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfToken(EntityManager em) {
        return em.createQuery("SELECT COUNT(c) FROM Capability c", Long.class)
                .getSingleResult();
    }

    static Long getNumberOfToken(ScopeType scopeType, EntityManager em) {
        return em.createQuery("SELECT COUNT(c) FROM Capability c WHERE c.scopeType = :scopeType", Long.class)
                .setParameter("scopeType", scopeType)
                .getSingleResult();
    }

    static Long getNumberOfActiveToken(ScopeType scopeType, EntityManager em) {
        return em.createQuery("SELECT COUNT(c) FROM Capability c WHERE c.scopeType = :scopeType AND c.revokedTime = NULL AND c.expirationTime > :now AND c.notBeforeTime < :now", Long.class)
                .setParameter("now", LocalDateTime.now(ZoneOffset.UTC))
                .setParameter("scopeType", scopeType)
                .getSingleResult();
    }

    static Long getNumberOfUnactiveToken(ScopeType scopeType, EntityManager em) {
        return em.createQuery("SELECT COUNT(c) FROM Capability c WHERE c.scopeType = :scopeType AND (c.revokedTime <> NULL OR c.expirationTime < :now OR c.notBeforeTime > :now)", Long.class)
                .setParameter("now", LocalDateTime.now(ZoneOffset.UTC))
                .setParameter("scopeType", scopeType)
                .getSingleResult();
    }

}
