package online.kheops.auth_server.token;

import java.util.Optional;

public interface TokenProvenance {
    default Optional<String> getAuthorizedParty() {
        return Optional.empty();
    }
    default Optional<String> getActingParty() {
        return Optional.empty();
    }
    default Optional<String> getCapabilityTokenId() {
        return Optional.empty();
    }
}
