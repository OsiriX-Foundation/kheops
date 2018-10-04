package online.kheops.auth_server;

import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.user.UsersPermission;

public interface KheopsPrincipalInterface extends java.security.Principal{

    //for old version
    public long getDBID();

    public boolean hasSeriesReadAccess(String study, String series);
    public boolean hasStudyReadAccess(String study);
    public boolean hasUserReadAccess();

    //public boolean hasSeriesWriteAccess(String study, String series);
    //public boolean hasStudyWriteAccess(String study);
    public boolean hasUserWriteAccess();

    public boolean hasAlbumPermission(UsersPermission.UsersPermissionEnum usersPermission, Long albumId);

    public boolean hasAlbumAccess(Long albumId);

    public User getUser();

    public ScopeType getScope();
}
