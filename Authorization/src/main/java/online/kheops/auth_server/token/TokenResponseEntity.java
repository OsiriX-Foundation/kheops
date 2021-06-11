package online.kheops.auth_server.token;

import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

class TokenResponseEntity {
    @XmlElement(name = "access_token")
    private final String accessToken;
    @XmlElement(name = "token_type")
    private static final String TOKEN_TYPE = "bearer";
    @XmlElement(name = "expires_in")
    private final Long expiresIn;

    @SuppressWarnings("unused")
    private TokenResponseEntity() {
        accessToken = null;
        expiresIn = null;
    }

    private TokenResponseEntity(final String accessToken, final long expiresIn) {
        this.accessToken = Objects.requireNonNull(accessToken);
        this.expiresIn = expiresIn;
    }

    static TokenResponseEntity createEntity(final String accessToken, final long expiresIn) {
        return new TokenResponseEntity(accessToken, expiresIn);
    }
}
