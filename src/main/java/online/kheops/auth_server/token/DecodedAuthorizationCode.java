package online.kheops.auth_server.token;

import java.util.Objects;
import java.util.Set;

class DecodedAuthorizationCode {
    private final String subject;
    private final Set<String> studyInstanceUIDs;

    static DecodedAuthorizationCode createDecodedAuthorizationCode(final String subject, final Set<String> studyInstanceUIDs) {
        return new DecodedAuthorizationCode(subject, studyInstanceUIDs);
    }

    private DecodedAuthorizationCode(final String subject, final Set<String> studyInstanceUIDs) {
        this.subject = Objects.requireNonNull(subject);
        this.studyInstanceUIDs = Objects.requireNonNull(studyInstanceUIDs);
    }

    String getSubject() {
        return subject;
    }

    Set<String> getStudyInstanceUIDs() {
        return studyInstanceUIDs;
    }
}
