package online.kheops.auth_server.token;

import java.util.Optional;

public class TokenGrantResult {
    private final TokenResponseEntity tokenResponseEntity;
    private final String subject;

    private final String scope;
    private final String studyInstanceUID;
    private final String seriesInstanceUID;

    TokenGrantResult(TokenResponseEntity tokenResponseEntity, String subject) {
        this.tokenResponseEntity = tokenResponseEntity;
        this.subject = subject;
        this.scope = null;
        this.studyInstanceUID = null;
        this.seriesInstanceUID = null;
    }

    TokenGrantResult(TokenResponseEntity tokenResponseEntity, String subject, String scope, String studyInstanceUID, String seriesInstanceUID) {
        this.tokenResponseEntity = tokenResponseEntity;
        this.subject = subject;
        this.scope = scope;
        this.studyInstanceUID = studyInstanceUID;
        this.seriesInstanceUID = seriesInstanceUID;
    }

    public TokenResponseEntity getTokenResponseEntity() {
        return tokenResponseEntity;
    }

    public String getSubject() {
        return subject;
    }

    public Optional<String> getScope() {
        return Optional.ofNullable(scope);
    }

    public Optional<String> getStudyInstanceUID() {
        return Optional.ofNullable(studyInstanceUID);
    }

    public Optional<String> getSeriesInstanceUID() {
        return Optional.ofNullable(seriesInstanceUID);
    }
}
