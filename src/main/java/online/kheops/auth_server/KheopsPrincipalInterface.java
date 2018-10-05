package online.kheops.auth_server;

import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.user.UsersPermission;

public interface KheopsPrincipalInterface extends java.security.Principal{

    //for old version
    long getDBID();

    boolean hasSeriesReadAccess(String study, String series);
    boolean hasStudyReadAccess(String study);
    boolean hasUserReadAccess();

    boolean hasSeriesWriteAccess(String study, String series);
    boolean hasStudyWriteAccess(String study);
    boolean hasUserWriteAccess();

    boolean hasAlbumPermission(UsersPermission.UsersPermissionEnum usersPermission, Long albumId);

    boolean hasAlbumAccess(Long albumId);

    User getUser();

    ScopeType getScope();
}
