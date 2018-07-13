package online.kheops.zipper;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public final class AccessToken {
    private final String token;
    private final String user;
    private final AccessTokenType type;

    private AccessToken(String token, String user, AccessTokenType type) {
        this.token = Objects.requireNonNull(token, "token must not be null");
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public static AccessToken getInstance(String token, String user, AccessTokenType type) {
        return new AccessToken(token, user, type);
    }

    public String getToken() {
        return token;
    }

    public String getUser() {
        return user;
    }

    @SuppressWarnings("unused")
    public AccessTokenType getType() {
        return type;
    }

    public String getTypeUrn() {
        return type.getUrn();
    }
}
