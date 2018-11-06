package online.kheops.zipper.BearerToken;

import java.util.Objects;

public final class BearerToken  {
    private final String token;

    private BearerToken(String token) {
        this.token = Objects.requireNonNull(token, "token");
    }

    static BearerToken newInstance(String token) {
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
