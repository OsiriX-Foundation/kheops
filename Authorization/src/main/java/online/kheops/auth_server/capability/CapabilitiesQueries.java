package online.kheops.auth_server.capability;

import online.kheops.auth_server.album.AlbumResponseBuilder;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.util.ErrorResponse;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static online.kheops.auth_server.util.JPANamedQueryConstants.*;

public class CapabilitiesQueries {

    private CapabilitiesQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static Capability findCapabilityByCapabilityToken(String secret, EntityManager em)
            throws CapabilityNotFoundException {

        try {
            TypedQuery<Capability> query = em.createNamedQuery("Capability.findBySecret", Capability.class);
            query.setParameter(SECRET, secret);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Not Found")
                    .detail("Capability token not found")
                    .build();
            throw new CapabilityNotFoundException(errorResponse);
        }
    }

    public static Capability findCapabilityByIdandUser(User user, String capabilityId, EntityManager em)
            throws CapabilityNotFoundException {

        try {
            TypedQuery<Capability> query = em.createNamedQuery("Capability.findByIdAndUser", Capability.class);
            query.setParameter(USER, user);
            query.setParameter(CAPABILITY_ID, capabilityId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Not Found")
                    .detail("Capability token not found")
                    .build();
            throw new CapabilityNotFoundException(errorResponse);
        }
    }

    public static Capability findCapabilityByCapabilityID(String capabilityId, EntityManager em)
            throws CapabilityNotFoundException {

        try {
            TypedQuery<Capability> query = em.createNamedQuery("Capability.findById", Capability.class);
            query.setParameter(CAPABILITY_ID, capabilityId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Not Found")
                    .detail("Capability token not found")
                    .build();
            throw new CapabilityNotFoundException(errorResponse);
        }
    }
    public static Capability findCapabilityByCapabilityIDAndAlbumId(String capabilityId, String albumId, EntityManager em)
            throws CapabilityNotFoundException {

        try {
            TypedQuery<Capability> query = em.createNamedQuery("Capability.findByIdAndAlbumId", Capability.class);
            query.setParameter(CAPABILITY_ID, capabilityId);
            query.setParameter(ALBUM_ID, albumId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Not Found")
                    .detail("Capability token not found")
                    .build();
            throw new CapabilityNotFoundException(errorResponse);
        }
    }


    public static List<Capability> findAllCapabilitiesByUser(User user, Integer limit, Integer offset, EntityManager em) {
        TypedQuery<Capability> query = em.createNamedQuery("Capability.findAllByUser", Capability.class);
        query.setParameter(USER, user);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static List<Capability> findCapabilitiesByUserValidOnly(User user, Integer limit, Integer offset, EntityManager em) {
        TypedQuery<Capability> query = em.createNamedQuery("Capability.findAllValidByUser", Capability.class);
        query.setParameter(USER, user);
        query.setParameter(DATE_TIME_NOW, LocalDateTime.now());
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static List<Capability> findAllCapabilitiesByAlbum(String albumId, User user, Integer limit, Integer offset, boolean valid, EntityManager em) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Capability> c = cb.createQuery(Capability.class);
        final Root<Capability> rootCapability = c.from(Capability.class);
        final Join<Capability, Album> al = rootCapability.join(Capability_.album);

        c.select(rootCapability);
        final List<Predicate> criteria = new ArrayList<>();
        criteria.add(cb.equal(al.get(Album_.id), albumId));
        if (user != null) {
            criteria.add(cb.equal(rootCapability.get(Capability_.user), user));
        }

        if (valid) {
            criteria.add(cb.isNull(rootCapability.get(Capability_.revokedTime)));
            criteria.add(cb.greaterThan(rootCapability.get(Capability_.expirationTime), LocalDateTime.now()));
        }

        if (criteria.size() == 1) {
            c.where(cb.and(criteria.get(0)));
        } else if (criteria.size() > 1) {
            c.where(cb.and(criteria.toArray(new Predicate[0])));
        }

        final TypedQuery<Capability> q = em.createQuery(c);
        q.setFirstResult(offset);
        q.setMaxResults(limit);

        return q.getResultList();
    }

    public static long countAllCapabilitiesByUser(User user, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Capability.countAllByUser", Long.class);
        query.setParameter(USER, user);
        return query.getSingleResult();
    }

    public static long countCapabilitiesByUserValidOnly(User user, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Capability.countAllValidByUser", Long.class);
        query.setParameter(USER, user);
        query.setParameter(DATE_TIME_NOW, LocalDateTime.now());
        return query.getSingleResult();
    }

    public static long countCapabilitiesByAlbum(String albumId, User user, boolean valid, EntityManager em) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Long> c = cb.createQuery(long.class);
        final Root<Capability> rootCapability = c.from(Capability.class);
        final Join<Capability, Album> al = rootCapability.join(Capability_.album);

        c.select(cb.countDistinct(rootCapability.get(Capability_.pk)));
        final List<Predicate> criteria = new ArrayList<>();
        criteria.add(cb.equal(al.get(Album_.id), albumId));
        if (user != null) {
            criteria.add(cb.equal(rootCapability.get(Capability_.user), user));
        }

        if (valid) {
            criteria.add(cb.isNull(rootCapability.get(Capability_.revokedTime)));
            criteria.add(cb.greaterThan(rootCapability.get(Capability_.expirationTime), LocalDateTime.now()));
        }

        if (criteria.size() == 1) {
            c.where(cb.and(criteria.get(0)));
        } else if (criteria.size() > 1) {
            c.where(cb.and(criteria.toArray(new Predicate[0])));
        }

        final TypedQuery<Long> q = em.createQuery(c);


        return q.getSingleResult();
    }



        public static void deleteAllCapabilitiesByAlbum (Album album, EntityManager em) {
        em.createNamedQuery("Capability.deleteAllByAlbum")
                .setParameter(ALBUM, album)
                .executeUpdate();

    }
}
