package online.kheops.auth_server.capability;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.util.ErrorResponse;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
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

    public static List<Capability> findAllCapabilitiesByAlbum(String albumId, Integer limit, Integer offset, EntityManager em) {
        TypedQuery<Capability> query = em.createNamedQuery("Capability.findAllByAlbum", Capability.class);
        query.setParameter(ALBUM_ID, albumId);
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
    }

    public static List<Capability> findCapabilitiesByAlbumValidOnly(String albumId, Integer limit, Integer offset, EntityManager em) {
        TypedQuery<Capability> query = em.createNamedQuery("Capability.findAllValidByAlbum", Capability.class);
        query.setParameter(ALBUM_ID, albumId);
        query.setParameter(DATE_TIME_NOW, LocalDateTime.now());
        query.setFirstResult(offset).setMaxResults(limit);
        return query.getResultList();
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

    public static long countAllCapabilitiesByAlbum(String albumId, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Capability.countAllByAlbum", Long.class);
        query.setParameter(ALBUM_ID, albumId);
        return query.getSingleResult();
    }

    public static long countCapabilitiesByAlbumValidOnly(String albumId, EntityManager em) {
        TypedQuery<Long> query = em.createNamedQuery("Capability.countAllValidByAlbum", Long.class);
        query.setParameter(ALBUM_ID, albumId);
        query.setParameter(DATE_TIME_NOW, LocalDateTime.now());
        return query.getSingleResult();
    }

    public static void deleteAllCapabilitiesByAlbum (Album album, EntityManager em) {
        em.createNamedQuery("Capability.deleteAllByAlbum")
                .setParameter(ALBUM, album)
                .executeUpdate();

    }
}
