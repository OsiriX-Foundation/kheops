package online.kheops.auth_server.util;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponseEntity {
    @XmlElement(name = "access_token")
    String accessToken;
    @XmlElement(name = "token_type")
    final String tokenType = "Bearer";
    @XmlElement(name = "expires_in")
    Long expiresIn;

    public TokenResponseEntity() {}

    private TokenResponseEntity(final String accessToken, final long expiresIn) {
        this.accessToken = Objects.requireNonNull(accessToken);
        this.expiresIn = expiresIn;
    }

    public static TokenResponseEntity createEntity(final String accessToken, final long expiresIn) {
        return new TokenResponseEntity(accessToken, expiresIn);
    }
}


