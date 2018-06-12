package online.kheops.auth_server;

public abstract class PACSAuthTokenBuilder {
    static private String secret = null;

    public static void setSecret(String secret) {
        PACSAuthTokenBuilder.secret = secret;
    }

    public static PACSAuthTokenBuilder newBuilder() {
        return new PACSAuthTokenBuilderImpl(secret);
    }

    abstract public PACSAuthTokenBuilder withStudyUID(String studyUID);

    abstract public PACSAuthTokenBuilder withSeriesUID(String seriesUID);
    abstract public PACSAuthTokenBuilder withAllSeries();

//    abstract public PACSAuthTokenBuilder withSubject(String sub);

    abstract public String build();
}
