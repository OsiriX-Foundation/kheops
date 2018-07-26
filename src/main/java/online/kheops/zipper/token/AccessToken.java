package online.kheops.zipper.token;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public final class AccessToken {
    private final String token;
    private final String user;
    private final AccessTokenType type;

    private AccessToken(String token, String user, AccessTokenType type) {
        this.token = Objects.requireNonNull(token, "token");
        this.user = Objects.requireNonNull(user, "user");
        this.type = Objects.requireNonNull(type, "type");
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
