package online.kheops.auth_server;

import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.user.UsersPermission;

import javax.persistence.EntityManager;

public class CapabilityPrincipal implements KheopsPrincipalInterface {

    private final Capability capability;
    private final User user;

    private EntityManager em;

    /*public CapabilityPrincipal(ScopeType scopeType, boolean readPermission, boolean writePermission) {
        this.scopeType = scopeType;
        this.readPermission = readPermission;
        this.writePermission = writePermission;
    }*/

    //old version
    Long dbid;
    public CapabilityPrincipal(Capability capability, User user) {
        this.capability = capability;
        this.dbid=user.getPk();
        this.user = user;
        this.em = EntityManagerListener.createEntityManager();
    }
    @Override
    public long getDBID() {
        return dbid;
    }
    @Override
    public String getName() {
        return Long.toString(dbid);
    }
    //end old version

    @Override
    public boolean hasSeriesReadAccess(String study, String series) {
        return false;
    }

    @Override
    public boolean hasStudyReadAccess(String study) {
        return false;
    }

    @Override
    public boolean hasUserReadAccess() {
        return false;
    }

    @Override
    public boolean hasSeriesWriteAccess(String study, String series) {
        return false;
    }

    @Override
    public boolean hasStudyWriteAccess(String study) {
        return false;
    }

    @Override
    public boolean hasUserWriteAccess() {
        return false;
    }

    @Override
    public boolean hasAlbumPermission(UsersPermission usersPermission, Long albumId) {
        return false;
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public ScopeType getScope() {
        return null;
    }
}
