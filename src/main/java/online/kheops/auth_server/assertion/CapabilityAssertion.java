package online.kheops.auth_server.assertion;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.capability.Capabilities;
import online.kheops.auth_server.capability.CapabilityNotFoundException;
import online.kheops.auth_server.capability.CapabilityNotValidException;
import online.kheops.auth_server.capability.CapabilityToken;
import online.kheops.auth_server.entity.Capability;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Objects;
import java.util.Optional;

final class CapabilityAssertion implements Assertion {
    private final String sub;
    private final String email;
    private static final Builder BUILDER = new Builder();
    private Capability capability;

    static final class Builder {
        CapabilityAssertion build(String capabilityToken) throws BadAssertionException {
            if (!CapabilityToken.isValidFormat(capabilityToken)) {
                throw new BadAssertionException("Bad capability token format");
            }

            final EntityManager em = EntityManagerListener.createEntityManager();
            final EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                final Capability capability = Capabilities.getCapability(capabilityToken, em);

                capability.isValid();

                final String sub = capability.getUser().getKeycloakId();
                final String email = capability.getUser().getEmail();

                capability.setLastUsed();

                tx.commit();

                return new CapabilityAssertion(capability, sub, email);
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

    private CapabilityAssertion(Capability capability, String sub, String email) {
        this.capability = Objects.requireNonNull(capability);
        this.sub = Objects.requireNonNull(sub);
        this.email = Objects.requireNonNull(email);
    }

    @Override
    public String getSub() {
        return sub;
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

    @Override
    public TokenType getTokenType() { return TokenType.CAPABILITY_TOKEN; }
}
