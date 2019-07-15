package online.kheops.auth_server;

import online.kheops.auth_server.token.TokenProvenance;

public abstract class PepAccessTokenBuilder {
    private static String secret = null;

    public static void setSecret(String secret) {
        PepAccessTokenBuilder.secret = secret;
    }

    public static PepAccessTokenBuilder newBuilder(TokenProvenance provenance) {
        return new PepAccessTokenBuilderImpl(secret, provenance);
    }

    public abstract PepAccessTokenBuilder withExpiresIn(long seconds);

    public abstract PepAccessTokenBuilder withStudyUID(String studyUID);

    public abstract PepAccessTokenBuilder withSeriesUID(String seriesUID);
    public abstract PepAccessTokenBuilder withAllSeries();

    public abstract  PepAccessTokenBuilder withSubject(String sub);

    public abstract String build();
}
