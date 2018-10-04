package online.kheops.auth_server;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UsersPermission;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.getAlbumUser;
import static online.kheops.auth_server.series.Series.canAccessSeries;

import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.study.Studies.getStudy;

public class UserPrincipal implements KheopsPrincipalInterface {

    private EntityManager em;
    private EntityTransaction tx;
    private final User user;

    //old version
    Long dbid;
    public UserPrincipal(User user) {
        this.dbid=user.getPk();
        this.user = user;
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
    public boolean hasSeriesReadAccess(String studyInstanceUID, String seriesInstanceUID) {

        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();
            return canAccessSeries(user, studyInstanceUID, seriesInstanceUID, em);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Override
    public boolean hasStudyReadAccess(String studyInstanceUID) {
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();
            final Study study = getStudy(studyInstanceUID, em);
            return canAccessStudy(user, study, em);
        } catch (StudyNotFoundException e) {
            return false;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Override
    public boolean hasUserReadAccess() { return true; }

    /*@Override
    public boolean hasSeriesWriteAccess(String study, String series) {
        return false;
    }

    @Override
    public boolean hasStudyWriteAccess(String study) {
        return false;
    }*/

    @Override
    public boolean hasUserWriteAccess() {
        return true;
    }

    @Override
    public boolean hasAlbumPermission(UsersPermission.UsersPermissionEnum usersPermission, Long albumId) {
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();
            final Album album = getAlbum(albumId, em);
            final AlbumUser albumUser = getAlbumUser(album, user, em);
            if (albumUser.isAdmin()) {
                return true;
            }
            if (usersPermission == UsersPermission.UsersPermissionEnum.ADD_SERIES && album.isAddSeries()){
                return true;
            }
            if (usersPermission == UsersPermission.UsersPermissionEnum.ADD_USER && album.isAddUser()){
                return true;
            }
            if (usersPermission == UsersPermission.UsersPermissionEnum.DELETE_SERIES && album.isDeleteSeries()){
                return true;
            }
            if (usersPermission == UsersPermission.UsersPermissionEnum.DOWNLOAD_SERIES && album.isDownloadSeries()){
                return true;
            }
            if (usersPermission == UsersPermission.UsersPermissionEnum.SEND_SERIES && album.isSendSeries()){
                return true;
            }
            if (usersPermission == UsersPermission.UsersPermissionEnum.WRITE_COMMENT && album.isWriteComments()){
                return true;
            }
            if (usersPermission == UsersPermission.UsersPermissionEnum.LIST_USERS){
                return true;
            }
            if (usersPermission == UsersPermission.UsersPermissionEnum.EDIT_ALBUM){
                return true;
            }

            return false;
        } catch (AlbumNotFoundException e) {
            return false;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Override
    public boolean hasAlbumAccess(Long albumId) {
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();
            final Album album = getAlbum(albumId, em);
            final AlbumUser albumUser = getAlbumUser(album, user, em);
            return true;
        } catch (AlbumNotFoundException e) {
            return false;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public ScopeType getScope() {
        return ScopeType.USER;
    }
}
