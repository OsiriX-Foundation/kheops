package online.kheops.auth_server;

public abstract class PACSAuthTokenBuilder {
    private static String secret = null;

    public static void setSecret(String secret) {
        PACSAuthTokenBuilder.secret = secret;
    }

    public static PACSAuthTokenBuilder newBuilder() {
        return new PACSAuthTokenBuilderImpl(secret);
    }

    public abstract PACSAuthTokenBuilder withStudyUID(String studyUID);

    public abstract PACSAuthTokenBuilder withSeriesUID(String seriesUID);
    public abstract PACSAuthTokenBuilder withAllSeries();

    abstract public PACSAuthTokenBuilder withSubject(String sub);

    public abstract String build();
}
