package online.kheops.auth_server;

public abstract class PepAccessTokenBuilder {
    private static String secret = null;

    public static void setSecret(String secret) {
        PepAccessTokenBuilder.secret = secret;
    }

    public static PepAccessTokenBuilder newBuilder() {
        return new PepAccessTokenBuilderImpl(secret);
    }

    public abstract PepAccessTokenBuilder withExpiresIn(long seconds);

    public abstract PepAccessTokenBuilder withStudyUID(String studyUID);

    public abstract PepAccessTokenBuilder withSeriesUID(String seriesUID);
    public abstract PepAccessTokenBuilder withAllSeries();

    abstract public PepAccessTokenBuilder withSubject(String sub);

    public abstract PepAccessTokenBuilder withActingParty(String actingParty);

    public abstract String build();
}
