package online.kheops.zipper.token;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public final class AccessToken {
    private final String token;
    private final AccessTokenType type;

    private AccessToken(String token, String user, AccessTokenType type) {
        this.token = Objects.requireNonNull(token, "token");
        this.type = Objects.requireNonNull(type, "type");
    }

    public static AccessToken getInstance(String token, String user, AccessTokenType type) {
        return new AccessToken(token, user, type);
    }

    public String getToken() {
        return token;
    }

    @SuppressWarnings("unused")
    public AccessTokenType getType() {
        return type;
    }

    public String getTypeUrn() {
        return type.getUrn();
    }
}
