package online.kheops.auth_server;

@SuppressWarnings("WeakerAccess")
public class KheopsPrincipal implements java.security.Principal {

    private final long dbid;

    public KheopsPrincipal(long DBID) {
        dbid = DBID;
    }

    public long getDBID() {
        return dbid;
    }

    @Override
    public String getName() {
        return Long.toString(dbid);
    }
}
