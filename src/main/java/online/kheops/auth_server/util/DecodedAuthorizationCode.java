package online.kheops.auth_server.util;

import java.util.Objects;
import java.util.Set;

public class DecodedAuthorizationCode {
    private final String subject;
    private final Set<String> studyInstanceUIDs;

    public static DecodedAuthorizationCode createDecodedAuthorizationCode(final String subject, final Set<String> studyInstanceUIDs) {
        return new DecodedAuthorizationCode(subject, studyInstanceUIDs);
    }

    private DecodedAuthorizationCode(final String subject, final Set<String> studyInstanceUIDs) {
        this.subject = Objects.requireNonNull(subject);
        this.studyInstanceUIDs = Objects.requireNonNull(studyInstanceUIDs);
    }

    public String getSubject() {
        return subject;
    }

    public Set<String> getStudyInstanceUIDs() {
        return studyInstanceUIDs;
    }
}
