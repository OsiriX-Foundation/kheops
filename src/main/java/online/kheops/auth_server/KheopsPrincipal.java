package online.kheops.auth_server;

import javax.security.auth.Subject;

public class KheopsPrincipal implements java.security.Principal {

    private final long dbid;

    public KheopsPrincipal(long dbid) {
        this.dbid = dbid;
    }

    public long getDBID() {
        return dbid;
    }

    @Override
    public String getName() {
        return Long.toString(dbid);
    }
}
