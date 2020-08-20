package online.kheops.auth_server.util;

import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.jose4j.jwt.JwtClaims;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public abstract class JWTs {
    private JWTs() {}

    public static @NotNull Source decodeSource(@NotNull DecodedJWT jwt) throws JWTDecodeException {
        Objects.requireNonNull(jwt);

        final Claim isInboxClaim = jwt.getClaim("isInbox");
        final Claim sourceIdClaim = jwt.getClaim("sourceId");

        final boolean isInbox = !isInboxClaim.isNull() && isInboxClaim.asBoolean() != null && isInboxClaim.asBoolean();
        final String albumID;
        if (!sourceIdClaim.isNull()) {
            albumID = sourceIdClaim.asString();
        } else {
            albumID = null;
        }

        try {
            return Source.instance(isInbox, albumID);
        } catch (IllegalArgumentException e) {
            throw new JWTDecodeException("Unable to get source", e);
        }
    }

    public static void encodeSource(@NotNull JwtClaims claims, @NotNull Source source) {
        Objects.requireNonNull(claims);
        Objects.requireNonNull(source);

        if (source.isInbox()) {
            claims.setClaim("isInbox", true);
        } else {
            claims.setClaim("sourceId", source.getAlbumId());
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static @NotNull JWTCreator.Builder encodeSource(@NotNull JWTCreator.Builder builder, @NotNull Source source) {
        Objects.requireNonNull(builder);
        Objects.requireNonNull(source);

        if (source.isInbox()) {
            return builder.withClaim("isInbox", true);
        } else {
            return builder.withClaim("sourceId", source.getAlbumId());
        }
    }


}
