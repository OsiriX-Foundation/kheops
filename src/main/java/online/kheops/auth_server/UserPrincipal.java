package online.kheops.auth_server;

import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.user.UsersPermission;

import javax.persistence.EntityManager;

public class UserPrincipal implements KheopsPrincipalInterface {

    private EntityManager em;
    private final User user;

    //old version
    Long dbid;
    public UserPrincipal(User user) {
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
