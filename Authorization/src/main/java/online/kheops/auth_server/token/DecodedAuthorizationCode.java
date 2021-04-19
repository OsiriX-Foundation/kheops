package online.kheops.auth_server.token;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

class DecodedAuthorizationCode {
    private final String subject;
    private final String actingParty;
    private final String capabilityTokenId;
    private final Set<String> studyInstanceUIDs;

    static DecodedAuthorizationCode createDecodedAuthorizationCode(final String subject, final String actingParty, final String capabilityTokenId, final Set<String> studyInstanceUIDs) {
        return new DecodedAuthorizationCode(subject, actingParty, capabilityTokenId, studyInstanceUIDs);
    }

    private DecodedAuthorizationCode(final String subject, final String actingParty, final String capabilityTokenId, final Set<String> studyInstanceUIDs) {
        this.subject = Objects.requireNonNull(subject);
        this.actingParty = actingParty;
        this.capabilityTokenId = capabilityTokenId;
        this.studyInstanceUIDs = Objects.requireNonNull(studyInstanceUIDs);
    }

    String getSubject() {
        return subject;
    }

    public Optional<String> getActingParty() {
        return Optional.ofNullable(actingParty);
    }

    public Optional<String> getCapabilityTokenId() {
        return Optional.ofNullable(capabilityTokenId);
    }

    Set<String> getStudyInstanceUIDs() {
        return studyInstanceUIDs;
    }
}
