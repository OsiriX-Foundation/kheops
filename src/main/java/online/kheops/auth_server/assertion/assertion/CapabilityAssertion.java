package online.kheops.auth_server.assertion.assertion;

import online.kheops.auth_server.PersistenceUtils;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;
import online.kheops.auth_server.entity.Capability;

import javax.persistence.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CapabilityAssertion implements Assertion {
    private String username;
    private String email;

    public void setCapabilityToken(String capabilityToken) throws BadAssertionException {
        final EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
        final EntityManager em = factory.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            final TypedQuery<Capability> query = em.createQuery("SELECT c FROM Capability c where c.secret = :secret", Capability.class);
            query.setParameter("secret", capabilityToken);
            final Capability capability = query.getSingleResult();

            if (capability.isRevoked()) {
                throw new BadAssertionException("Capability token is revoked");
            }

            if (ZonedDateTime.of(capability.getExpiration(), ZoneOffset.UTC).isBefore(ZonedDateTime.now())) {
                throw new BadAssertionException("Capability token is expired");

            }

            username = capability.getUser().getGoogleId();
            email = capability.getUser().getGoogleEmail();

            tx.commit();
        } catch (NoResultException e) {
            throw new BadAssertionException("Unknown capability token", e);
        } finally {
            em.close();
            factory.close();
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
