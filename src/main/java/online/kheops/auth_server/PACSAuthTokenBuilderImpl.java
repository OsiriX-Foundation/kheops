package online.kheops.auth_server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PACSAuthTokenBuilderImpl extends PACSAuthTokenBuilder {

    private static final String STUDY_UID = "study_uid";
    private static final String SERIES_UID = "series_uid";
    private static final String SERIES_ALL_ACCESS = "all_access";
    private static final String SUBJECT = "sub";

    private Map<String, String> claims;
    private Algorithm algorithm;

    PACSAuthTokenBuilderImpl(String secret) {
        claims = new HashMap<>();
        try {
            algorithm = Algorithm.HMAC256(secret);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Bad secret", e);
        }
    }

    @Override
    public PACSAuthTokenBuilder withStudyUID(String studyUID) {
        validateUID(studyUID);
        claims.put(STUDY_UID, studyUID);
        return this;
    }

    @Override
    public PACSAuthTokenBuilder withSeriesUID(String seriesUID) {
        validateUID(seriesUID);
        claims.put(SERIES_UID, seriesUID);
        return this;
    }

    @Override
    public PACSAuthTokenBuilder withAllSeries() {
        claims.put(SERIES_UID, SERIES_ALL_ACCESS);
        return this;
    }

    @Override
    public PACSAuthTokenBuilder withSubject(String subject){
        claims.put(SUBJECT, subject);
        return this;
    }

    @Override
    public String build() {
        if (!claims.containsKey(STUDY_UID)) {
            throw new IllegalStateException("Missing StudyUID");
        }
        if (!claims.containsKey(SERIES_UID)) {
            throw new IllegalStateException("Missing seriesUID");
        }
        if (!claims.containsKey(SUBJECT)) {
            throw new IllegalStateException("Missing Subject");
        }

        return JWT.create().withIssuer("auth.kheops.online")
                .withAudience("dicom.kheops.online")
                .withClaim(STUDY_UID, claims.get(STUDY_UID))
                .withClaim(SERIES_UID, claims.get(SERIES_UID))
                .withSubject(claims.get(SUBJECT))
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .withNotBefore(new Date()).sign(algorithm);
    }

    private void validateUID(String uid) {
        try {
            new Oid(uid);
        } catch (GSSException exception) {
            throw new IllegalArgumentException(uid + " is not a valid UID");
        }
    }
}
