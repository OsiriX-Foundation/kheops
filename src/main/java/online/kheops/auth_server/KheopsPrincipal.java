package online.kheops.auth_server;

@SuppressWarnings("WeakerAccess")
public class KheopsPrincipal implements java.security.Principal {

    private final long DBID;

    public KheopsPrincipal(long DBID) {
        this.DBID = DBID;
    }

    public long getDBID() {
        return DBID;
    }

    @Override
    public String getName() {
        return Long.toString(DBID);
    }
}
