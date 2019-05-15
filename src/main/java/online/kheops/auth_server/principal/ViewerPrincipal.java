package online.kheops.auth_server.principal;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.AssertionVerifier;
import online.kheops.auth_server.assertion.BadAssertionException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;
import online.kheops.auth_server.util.Consts;

import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static online.kheops.auth_server.album.Albums.*;
import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesListByStudyUIDFromAlbum;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesListByStudyUIDFromInbox;
import static online.kheops.auth_server.user.Users.getOrCreateUser;

public class ViewerPrincipal implements KheopsPrincipalInterface {

    private EntityManager em;
    private EntityTransaction tx;
    private final JsonObject jwe;
    private final KheopsPrincipalInterface kheopsPrincipal;


    public ViewerPrincipal(JsonObject jwe) {

        final Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(jwe.getString(Consts.JWE.TOKEN));
        } catch (BadAssertionException e) {
            throw new IllegalStateException(e);
        }

        final User user;
        try {
            user = getOrCreateUser(assertion.getSub());
        } catch (UserNotFoundException e) {
            throw new IllegalStateException(e);
        }
        kheopsPrincipal = assertion.newPrincipal(user);

        this.jwe = jwe;
    }
    @Override
    public long getDBID() {
        return kheopsPrincipal.getDBID();
    }
    //end old version

    @Override
    public String getName() { return kheopsPrincipal.getName(); }

    @Override
    public boolean hasSeriesReadAccess(String studyInstanceUID, String seriesInstanceUID) throws SeriesNotFoundException{

        if(!kheopsPrincipal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
            return false;
        }

        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();

            final List<Series> seriesList;
            if(jwe.getBoolean(Consts.JWE.IS_INBOX)) {
                seriesList = findSeriesListByStudyUIDFromInbox(kheopsPrincipal.getUser(), studyInstanceUID, em);
            } else {
                final Album album = getAlbum(jwe.getString(Consts.JWE.SOURCE_ID), em);
                seriesList = findSeriesListByStudyUIDFromAlbum(kheopsPrincipal.getUser(), album, studyInstanceUID, em);
            }

            if (seriesList.contains(getSeries(studyInstanceUID, seriesInstanceUID, em))) {
                return true;
            } else {
                return false;
            }
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
    public boolean hasStudyReadAccess(String studyInstanceUID) {
        if(!kheopsPrincipal.hasStudyReadAccess(studyInstanceUID)) {
            return false;
        }

        if (studyInstanceUID.equals(jwe.getString(Consts.JWE.STUDY_INSTANCE_UID))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasUserAccess() { return false; }

    @Override
    public boolean hasSeriesWriteAccess(String studyInstanceUID, String seriesInstanceUID) { return false; }

    @Override
    public boolean hasStudyWriteAccess(String study) { return false; }

    @Override
    public boolean hasAlbumPermission(UserPermissionEnum usersPermission, String albumId)
            throws AlbumNotFoundException {

        if (!kheopsPrincipal.hasAlbumPermission(usersPermission, albumId)) {
            return false;
        } else {
            this.em = EntityManagerListener.createEntityManager();
            this.tx = em.getTransaction();
            try {
                tx.begin();

                final User userMerge = em.merge(kheopsPrincipal.getUser());
                final Album album = getAlbum(albumId, em);

                if(!isMemberOfAlbum(userMerge, album, em)) {
                    throw new AlbumNotFoundException("Album id : " + albumId + " not found");
                }

                if(userMerge.getInbox() == album) {
                    throw new AlbumNotFoundException("Album id : " + albumId + " not found");
                }

                return usersPermission.hasViewerPermission(album);
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        }
    }

    @Override
    public boolean hasAlbumAccess(String albumId){
        try {
            return kheopsPrincipal.hasAlbumAccess(albumId) && !jwe.getBoolean(Consts.JWE.IS_INBOX) && jwe.getString(Consts.JWE.SOURCE_ID).equals(albumId);
        } catch (AlbumNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean hasInboxAccess() {
        return jwe.getBoolean(Consts.JWE.IS_INBOX);
    }

    @Override
    public User getUser() { return kheopsPrincipal.getUser(); }

    @Override
    public ScopeType getScope() {
        if(!jwe.getBoolean(Consts.JWE.IS_INBOX) || kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            return ScopeType.ALBUM;
        } else {
            return ScopeType.USER;
        }

    }

    @Override
    public String getAlbumID() throws NotAlbumScopeTypeException, AlbumNotFoundException {
        final String albumID;
        if(!jwe.getBoolean(Consts.JWE.IS_INBOX)) {
            albumID = jwe.getString(Consts.JWE.SOURCE_ID);
        } else {
            throw new NotAlbumScopeTypeException("");
        }

        if(kheopsPrincipal.hasAlbumAccess(albumID)) {
            return albumID;
        } else {
            throw new AlbumNotFoundException("");
        }
    }

    @Override
    public String toString() {
        return "[ViewerPrincipal user:" + getUser() + " scope:" + getScope() + " hasUserAccess:" + hasUserAccess() + " hasInboxAccess:" + hasInboxAccess() + "]";
    }

    @Override
    public Optional<List<String>> getStudyList() {
        final List<String> studyList = new ArrayList();
        studyList.add(jwe.getString(Consts.JWE.STUDY_INSTANCE_UID));
        return Optional.of(studyList);
    }
}
