package online.kheops.auth_server.accesstoken;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.capability.*;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.AlbumCapabilityPrincipal;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.principal.UserCapabilityPrincipal;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class CapabilityAccessToken implements AccessToken {
    private final String sub;
    private Capability capability;
    private final String token;


    static final class CapabilityAccessTokenBuilder implements AccessTokenBuilder {
        @Override
        public AccessToken build(String capabilityToken, boolean verifySignature) throws AccessTokenVerificationException {
            if (!CapabilityToken.isValidFormat(capabilityToken)) {
                throw new AccessTokenVerificationException("Bad capability token format");
            }

            final EntityManager em = EntityManagerListener.createEntityManager();
            final EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                final Capability capability = Capabilities.getCapability(capabilityToken, em);

                capability.isValid();

                final String sub = capability.getUser().getSub();

                capability.setLastUsed();

                tx.commit();

                return new CapabilityAccessToken(capability, sub, capabilityToken);
            } catch (CapabilityNotFoundException e) {
                throw new AccessTokenVerificationException("Unknown capability token");
            } catch (CapabilityNotValidException e) {
                throw new AccessTokenVerificationException(e.getMessage());
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        }
    }

    private CapabilityAccessToken(Capability capability, String sub, String token) {
        this.capability = Objects.requireNonNull(capability);
        this.sub = Objects.requireNonNull(sub);
        this.token = token;
    }

    @Override
    public String getSubject() {
        return sub;
    }

    @Override
    public Optional<String> getScope() {
        List<String> scopes = new ArrayList<>(4);

        if (capability.getScopeType().equals(ScopeType.ALBUM)) {
            if (capability.hasReadPermission()) {
                scopes.add("read");
            }
            if (capability.hasWritePermission()) {
                scopes.add("write");
            }
            if (capability.hasDownloadButtonPermission()) {
                scopes.add("downloadbutton");
            }
            if (capability.hasAppropriatePermission()) {
                scopes.add("send");
            }

            if (!scopes.isEmpty()) {
                return Optional.of(String.join(" ", scopes));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of("user read write downloadbutton send");
        }
    }

    @Override
    public TokenType getTokenType() {
        if (capability.getScopeType().equals(ScopeType.ALBUM)) {
            return TokenType.ALBUM_CAPABILITY_TOKEN;
        } else if (capability.getScopeType().equals(ScopeType.USER)) {
            return TokenType.USER_CAPABILITY_TOKEN;
        } else {
            throw new IllegalStateException("unknown scope type");
        }
    }

    @Override
    public KheopsPrincipal newPrincipal(ServletContext servletContext, User user) {
        if (capability.getScopeType().equals(ScopeType.ALBUM)) {
            return new AlbumCapabilityPrincipal(capability, user, token);
        } else {
            return new UserCapabilityPrincipal(capability, user, token);
        }
    }

    @Override
    public Optional<String> getCapabilityTokenId() {
        return Optional.of(capability.getId());
    }
}
