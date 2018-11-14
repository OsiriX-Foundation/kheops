package online.kheops.zipper.bearertoken;

import java.util.Objects;

public final class BearerToken  {
    private final String token;

    private BearerToken(String token) {
        this.token = Objects.requireNonNull(token, "accesstoken");
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
