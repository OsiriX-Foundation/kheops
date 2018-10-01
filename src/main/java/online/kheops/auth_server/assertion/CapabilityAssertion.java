package online.kheops.auth_server.assertion;

import online.kheops.auth_server.capability.Capabilities;
import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.Capability;

import javax.persistence.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

final class CapabilityAssertion implements Assertion {
    private final String username;
    private final String email;
    private static final Builder BUILDER = new Builder();

    static final class Builder {
        CapabilityAssertion build(String capabilityToken) throws BadAssertionException {
            if (!Capabilities.isValidFormat(capabilityToken)) {
                throw new BadAssertionException("Bad capability format");
            }

            final EntityManager em = EntityManagerListener.createEntityManager();
            final EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                final TypedQuery<Capability> query = em.createQuery("SELECT c FROM Capability c where c.secret = :secret", Capability.class);
                query.setParameter("secret", capabilityToken);
                final Capability capability = query.getSingleResult();

                if (capability.isRevoked()) {
                    throw new BadAssertionException("Capability token is revoked");
                }

                if (ZonedDateTime.of(capability.getStartTime(), ZoneOffset.UTC).isAfter(ZonedDateTime.now())) {
                    throw new BadAssertionException("Capability token is not yet valid");
                }

                if (ZonedDateTime.of(capability.getExpiration(), ZoneOffset.UTC).isBefore(ZonedDateTime.now())) {
                    throw new BadAssertionException("Capability token is expired");
                }

                final String username = capability.getUser().getGoogleId();
                final String email = capability.getUser().getGoogleEmail();

                tx.commit();

                return new CapabilityAssertion(username, email);
            } catch (NoResultException e) {
                throw new BadAssertionException("Unknown capability token");
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        }
    }

    static Builder getBuilder() {
        return BUILDER;
    }

    private CapabilityAssertion(String username, String email) {
        this.username = Objects.requireNonNull(username);
        this.email = Objects.requireNonNull(email);
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
    public boolean hasCapabilityAccess() {
        return false;
    }
}
