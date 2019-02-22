package online.kheops.auth_server.principal;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.AssertionVerifier;
import online.kheops.auth_server.assertion.BadAssertionException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;

import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import java.util.List;

import static online.kheops.auth_server.album.Albums.*;
import static online.kheops.auth_server.series.Series.canAccessSeries;
import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.series.SeriesQueries.*;
import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.user.Users.getOrCreateUser;

public class ViewerPrincipal implements KheopsPrincipalInterface {

    private EntityManager em;
    private EntityTransaction tx;
    private final JsonObject jwe;
    private final KheopsPrincipalInterface kheopsPrincipal;


    public ViewerPrincipal(JsonObject jwe) {

        Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(jwe.getString("token"));
        } catch (BadAssertionException e) {
            assertion = null;
        }

        User user;
        try {
            user = getOrCreateUser(assertion.getSub());
        } catch (UserNotFoundException e) {
            user = null;
        }
        if(assertion.getCapability().isPresent()) {
            kheopsPrincipal = new CapabilityPrincipal(assertion.getCapability().get(), user);
        } else if(assertion.getViewer().isPresent()) {
            kheopsPrincipal = new ViewerPrincipal(assertion.getViewer().get());
        } else {
            kheopsPrincipal = new UserPrincipal(user);
        }

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

            List<Series> seriesList;
            if(jwe.getBoolean("isInbox")) {
                seriesList = findSeriesListByStudyUIDFromInbox(kheopsPrincipal.getUser(), studyInstanceUID, em);
            } else {
                final Album album = getAlbum(jwe.getString("sourceId"), em);
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

        if (studyInstanceUID.compareTo(jwe.getString("studyInstanceUID")) == 0) {
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
    public boolean hasAlbumPermission(UserPermissionEnum usersPermission, String albumId) throws AlbumNotFoundException {
        if (!kheopsPrincipal.hasAlbumPermission(usersPermission, albumId)) {
            return false;
        } else {
            this.em = EntityManagerListener.createEntityManager();
            this.tx = em.getTransaction();
            try {
                tx.begin();

                final User userMerge = em.merge(kheopsPrincipal.getUser());
                final Album album = getAlbum(albumId, em);
                final AlbumUser albumUser = getAlbumUser(album, userMerge, em);

                if(userMerge.getInbox() == album) {
                    throw new AlbumNotFoundException("Album id : " + albumId + " not found");
                }
                if (albumUser.isAdmin()) {
                    return true;
                }

                return usersPermission.hasViewerPermission(album);

            } catch (AlbumNotFoundException | UserNotMemberException e) {
                throw new AlbumNotFoundException("Album id : " + albumId + " not found");
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
            return kheopsPrincipal.hasAlbumAccess(albumId) && !jwe.getBoolean("isInbox") && jwe.getString("sourceId").compareTo(albumId) == 0;
        } catch (AlbumNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean hasInboxAccess() {
        return jwe.getBoolean("isInbox");
    }

    @Override
    public User getUser() { return kheopsPrincipal.getUser(); }

    @Override
    public ScopeType getScope() {
        if(!jwe.getBoolean("isInbox") || kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            return ScopeType.ALBUM;
        } else {
            return ScopeType.USER;
        }

    }

    @Override
    public String getAlbumID() throws NotAlbumScopeTypeException, AlbumNotFoundException {
        final String albumID;
        if(!jwe.getBoolean("isInbox")) {
            albumID = jwe.getString("sourceId");
        } else {
            throw new NotAlbumScopeTypeException("");
        }

        if(kheopsPrincipal.hasAlbumAccess(albumID)) {
            return albumID;
        } else {
            throw new AlbumNotFoundException("");
        }

    }
}
