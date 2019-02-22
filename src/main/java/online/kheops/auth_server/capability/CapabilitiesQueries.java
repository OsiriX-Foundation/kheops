package online.kheops.auth_server.capability;

import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;

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
            throws NoResultException {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where c.secret = :secret", Capability.class);
        query.setParameter("secret", secret);
        return query.getSingleResult();
    }

    public static Capability findCapabilityByCapabilityTokenandUser(User user, String capabilityId, EntityManager em)
            throws NoResultException {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :user = c.user AND :capabilityId = c.id", Capability.class);
        query.setParameter("user", user);
        query.setParameter("capabilityId", capabilityId);
        return query.getSingleResult();
    }

    public static Capability findCapabilityByCapabilityID(String capabilityId, EntityManager em)
            throws NoResultException {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :capabilityId = c.id", Capability.class);
        query.setParameter("capabilityId", capabilityId);
        return query.getSingleResult();
    }


    public static  List<Capability> findAllCapabilitiesByUser(User user, EntityManager em) {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :user = c.user order by c.issuedAtTime desc", Capability.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public static  List<Capability> findCapabilitiesByUserValidOnly(User user, EntityManager em) {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :user = c.user and c.revokedTime = null and c.expirationTime > :dateTimeNow  order by c.issuedAtTime desc", Capability.class);
        query.setParameter("user", user);
        query.setParameter("dateTimeNow", LocalDateTime.now());
        return query.getResultList();
    }


    public static  List<Capability> findAllCapabilitiesByAlbum(String albumId, EntityManager em) {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :albumId = c.album.id order by c.issuedAtTime desc", Capability.class);
        query.setParameter("albumId", albumId);
        return query.getResultList();
    }

    public static  List<Capability> findCapabilitiesByAlbumValidOnly(String albumId, EntityManager em) {
        TypedQuery<Capability> query = em.createQuery("SELECT c from Capability c where :albumId = c.album.id and c.revokedTime = null and c.expirationTime > :dateTimeNow  order by c.issuedAtTime desc", Capability.class);
        query.setParameter("albumId", albumId);
        query.setParameter("dateTimeNow", LocalDateTime.now());
        return query.getResultList();
    }
}
