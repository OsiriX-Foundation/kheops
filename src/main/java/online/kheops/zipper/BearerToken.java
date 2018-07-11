package online.kheops.zipper;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public final class BearerToken  {
    private final String token;

    private BearerToken(String token) {
        this.token = Objects.requireNonNull(token, "token must not be null");
    }

    public static BearerToken newInstance(String token) {
        return new BearerToken(token);
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return token;
    }
}
