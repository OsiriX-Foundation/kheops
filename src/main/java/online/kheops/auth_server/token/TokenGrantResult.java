package online.kheops.auth_server.token;

public class TokenGrantResult {
    private TokenResponseEntity tokenResponseEntity;
    private String subject;

    TokenGrantResult(TokenResponseEntity tokenResponseEntity, String subject) {
        this.tokenResponseEntity = tokenResponseEntity;
        this.subject = subject;
    }

    public TokenResponseEntity getTokenResponseEntity() {
        return tokenResponseEntity;
    }

    public String getSubject() {
        return subject;
    }
}
