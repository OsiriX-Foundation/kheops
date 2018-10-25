package online.kheops.auth_server;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UsersPermission;

import javax.persistence.NoResultException;

public interface KheopsPrincipalInterface extends java.security.Principal{

    //for old version
    long getDBID();
    long getAlbumID() throws NotAlbumScopeTypeException;

    boolean hasSeriesReadAccess(String study, String series) throws SeriesNotFoundException;
    boolean hasStudyReadAccess(String study)  throws StudyNotFoundException;
    boolean hasUserAccess();

    boolean hasSeriesWriteAccess(String study, String series)throws SeriesNotFoundException;
    boolean hasStudyWriteAccess(String study);

    boolean hasAlbumPermission(UsersPermission.UsersPermissionEnum usersPermission, Long albumId)throws AlbumNotFoundException;

    boolean hasAlbumAccess(Long albumId) throws AlbumNotFoundException;

    User getUser();

    ScopeType getScope();
}
