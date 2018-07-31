package online.kheops.auth_server.assertion.assertion;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.entity.Capability;

import javax.persistence.*;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CapabilityAssertion implements Assertion {
    private String username;
    private String email;

    public void setCapabilityToken(String capabilityToken) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            final TypedQuery<Capability> query = em.createQuery("SELECT c FROM Capability c where c.secret = :secret", Capability.class);
            query.setParameter("secret", capabilityToken);
            final Capability capability = query.getSingleResult();

            if (capability.isRevoked()) {
                throw new ForbiddenException("Capability token is revoked");
            }

            if (ZonedDateTime.of(capability.getExpiration(), ZoneOffset.UTC).isBefore(ZonedDateTime.now())) {
                throw new ForbiddenException("Capability token is expired");
            }

            username = capability.getUser().getGoogleId();
            email = capability.getUser().getGoogleEmail();

            tx.commit();
        } catch (NoResultException e) {
            throw new NotAuthorizedException("Unknown capability token");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public boolean isCapabilityAssertion() {
        return true;
    }
}
