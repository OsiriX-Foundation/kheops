package online.kheops.zipper;

import java.util.Objects;

public final class AccessToken {
    private final String token;
    private final AccessTokenType type;

    private AccessToken(String token, AccessTokenType type) {
        this.token = Objects.requireNonNull(token, "token must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public static AccessToken getInstance(String token, AccessTokenType type) {
        return new AccessToken(token, type);
    }

    public String getToken() {
        return token;
    }

    public AccessTokenType getType() {
        return type;
    }

    public String getTypeUrn() {
        return type.getUrn();
    }
}
