package online.kheops.auth_server.principal;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.getAlbumUser;
import static online.kheops.auth_server.album.Albums.isMemberOfAlbum;
import static online.kheops.auth_server.series.Series.canAccessSeries;

import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.series.SeriesQueries.*;
import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.study.Studies.getStudy;

public class UserPrincipal implements KheopsPrincipalInterface {

    private EntityManager em;
    private EntityTransaction tx;
    private final User user;

    //old version
    private final Long dbid;
    public UserPrincipal(User user) {
        this.dbid = user.getPk();
        this.user = user;
    }
    @Override
    public long getDBID() {
        return dbid;
    }
    //end old version

    @Override
    public String getName() { return Long.toString(dbid); }

    @Override
    public boolean hasSeriesReadAccess(String studyInstanceUID, String seriesInstanceUID) throws SeriesNotFoundException{
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();
            if (canAccessSeries(user, studyInstanceUID, seriesInstanceUID, em)) {
                return true;
            } else {
                throw new SeriesNotFoundException("seriesInstanceUID : " + seriesInstanceUID + " not found");
            }
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
    public boolean hasUserAccess() { return true; }

    @Override
    public boolean hasSeriesWriteAccess(String studyInstanceUID, String seriesInstanceUID) throws SeriesNotFoundException{
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();

            //find if the series exist
            final Series series;
            try {
                series = getSeries(studyInstanceUID, seriesInstanceUID, em);
            } catch (SeriesNotFoundException e) {
                return true;
            }

            // we need to check here if the series that was found is owned by the user
            try {
                findSeriesBySeriesAndUserInbox(user, series, em);
                return true;
            } catch (NoResultException ignored) {/*empty*/}

            try {
                findSeriesBySeriesAndAlbumWithSendPermission(user, series, em);
                return true;
            } catch (NoResultException ignored) {
                if (isOrphan(series, em)) {
                    return true;
                }
            }
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        throw new SeriesNotFoundException("SeriesUID : " + seriesInstanceUID + "from studyUID : " + studyInstanceUID + "not found");
    }

    @Override
    public boolean hasStudyWriteAccess(String study) { return true; }

    @Override
    public boolean hasAlbumPermission(UserPermissionEnum usersPermission, String albumId) throws AlbumNotFoundException {
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();

            final User userMerge = em.merge(user);
            final Album album = getAlbum(albumId, em);
            final AlbumUser albumUser = getAlbumUser(album, userMerge, em);

            if(userMerge.getInbox() == album) {
                throw new AlbumNotFoundException("Album id : " + albumId + " not found");
            }
            if (albumUser.isAdmin()) {
                return true;
            }

            return usersPermission.hasUserPermission(album);

        } catch (AlbumNotFoundException | UserNotMemberException e) {
            throw new AlbumNotFoundException("Album id : " + albumId + " not found");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Override
    public boolean hasAlbumAccess(String albumId){
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();
            final User userMerge = em.merge(user);
            final Album album = getAlbum(albumId, em);

            return isMemberOfAlbum(userMerge, album, em);
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
    public boolean hasInboxAccess() { return true; }

    @Override
    public User getUser() { return user; }

    @Override
    public ScopeType getScope() { return ScopeType.USER; }

    @Override
    public String getAlbumID() throws NotAlbumScopeTypeException { throw new NotAlbumScopeTypeException(""); }
}
