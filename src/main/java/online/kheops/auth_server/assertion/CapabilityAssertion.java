package online.kheops.auth_server.assertion;

import online.kheops.auth_server.capability.Capabilities;
import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.capability.CapabilityNotValidException;
import online.kheops.auth_server.capability.CapabilityNotFoundException;
import online.kheops.auth_server.entity.Capability;

import javax.persistence.*;
import java.util.Objects;
import java.util.Optional;

final class CapabilityAssertion implements Assertion {
    private final String username;
    private final String email;
    private static final Builder BUILDER = new Builder();
    private Capability capability;

    static final class Builder {
        CapabilityAssertion build(String capabilityToken) throws BadAssertionException {
            if (!Capabilities.isValidFormat(capabilityToken)) {
                throw new BadAssertionException("Bad capability format");
            }

            final EntityManager em = EntityManagerListener.createEntityManager();
            final EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                final Capability capability = Capabilities.getCapability(capabilityToken, em);

                capability.isValid();

                final String username = capability.getUser().getGoogleId();
                final String email = capability.getUser().getGoogleEmail();

                tx.commit();

                return new CapabilityAssertion(capability, username, email);
            } catch (CapabilityNotFoundException e) {
                throw new BadAssertionException("Unknown capability token");
            } catch (CapabilityNotValidException e) {
                throw new BadAssertionException(e.getMessage());
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

    private CapabilityAssertion(Capability capability, String username, String email) {
        this.capability = Objects.requireNonNull(capability);
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

    @Override
    public Optional<Capability> getCapability() {
        return Optional.of(capability);
    }
}
