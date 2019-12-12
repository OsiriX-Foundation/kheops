package online.kheops.auth_server.capability;

import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.util.ErrorResponse;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public class CapabilitiesQueries {

    private CapabilitiesQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static Capability findCapabilityByCapabilityToken(String secret, EntityManager em)
            throws CapabilityNotFoundException {

        try {
            TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where c.secret = :secret", Capability.class);
            query.setParameter("secret", secret);
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
            TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c left join c.album a left join a.albumUser au where ((:user = au.user AND au.admin = true) OR (:user = c.user)) AND :capabilityId = c.id", Capability.class);
            query.setParameter("user", user);
            query.setParameter("capabilityId", capabilityId);
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
            TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :capabilityId = c.id", Capability.class);
            query.setParameter("capabilityId", capabilityId);
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
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :user = c.user order by c.issuedAtTime desc", Capability.class);
        query.setParameter("user", user);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static List<Capability> findCapabilitiesByUserValidOnly(User user, Integer limit, Integer offset, EntityManager em) {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :user = c.user and c.revokedTime = null and c.expirationTime > :dateTimeNow  order by c.issuedAtTime desc", Capability.class);
        query.setParameter("user", user);
        query.setParameter("dateTimeNow", LocalDateTime.now());
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static List<Capability> findAllCapabilitiesByAlbum(String albumId, Integer limit, Integer offset, EntityManager em) {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :albumId = c.album.id order by c.issuedAtTime desc", Capability.class);
        query.setParameter("albumId", albumId);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static List<Capability> findCapabilitiesByAlbumValidOnly(String albumId, Integer limit, Integer offset, EntityManager em) {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :albumId = c.album.id and c.revokedTime = null and c.expirationTime > :dateTimeNow order by c.issuedAtTime desc", Capability.class);
        query.setParameter("albumId", albumId);
        query.setParameter("dateTimeNow", LocalDateTime.now());
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static long countAllCapabilitiesByUser(User user, EntityManager em) {
        TypedQuery<Long> query = em.createQuery("SELECT count(c) from Capability c where :user = c.user", Long.class);
        query.setParameter("user", user);
        return query.getSingleResult();
    }

    public static long countCapabilitiesByUserValidOnly(User user, EntityManager em) {
        TypedQuery<Long> query = em.createQuery("SELECT count(c) from Capability c where :user = c.user and c.revokedTime = null and c.expirationTime > :dateTimeNow", Long.class);
        query.setParameter("user", user);
        query.setParameter("dateTimeNow", LocalDateTime.now());
        return query.getSingleResult();
    }

    public static long countAllCapabilitiesByAlbum(String albumId, EntityManager em) {
        TypedQuery<Long> query = em.createQuery("SELECT count(c) from Capability c where :albumId = c.album.id", Long.class);
        query.setParameter("albumId", albumId);
        return query.getSingleResult();
    }

    public static long countCapabilitiesByAlbumValidOnly(String albumId, EntityManager em) {
        TypedQuery<Long> query = em.createQuery("SELECT count(c) from Capability c where :albumId = c.album.id and c.revokedTime = null and c.expirationTime > :dateTimeNow", Long.class);
        query.setParameter("albumId", albumId);
        query.setParameter("dateTimeNow", LocalDateTime.now());
        return query.getSingleResult();
    }
}
