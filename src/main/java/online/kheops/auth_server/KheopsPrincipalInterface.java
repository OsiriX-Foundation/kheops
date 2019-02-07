package online.kheops.auth_server;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;



public interface KheopsPrincipalInterface extends java.security.Principal{

    //for old version
    long getDBID();
    String getAlbumID() throws NotAlbumScopeTypeException, AlbumNotFoundException;

    boolean hasSeriesReadAccess(String study, String series) throws SeriesNotFoundException;
    boolean hasStudyReadAccess(String study);
    boolean hasUserAccess();
    boolean hasInboxAccess();

    boolean hasSeriesWriteAccess(String study, String series)throws SeriesNotFoundException;
    boolean hasStudyWriteAccess(String study);

    boolean hasAlbumPermission(UserPermissionEnum usersPermission, String albumId)throws AlbumNotFoundException;

    boolean hasAlbumAccess(String albumId) throws AlbumNotFoundException;

    User getUser();

    ScopeType getScope();
}
