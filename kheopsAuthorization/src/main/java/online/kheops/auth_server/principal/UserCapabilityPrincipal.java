package online.kheops.auth_server.principal;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.accesstoken.AccessToken.TokenType;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.AlbumUserPermissions;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.persistence.EntityManager;
import java.util.Optional;

import static online.kheops.auth_server.album.Albums.*;
import static online.kheops.auth_server.series.Series.*;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesBySeriesAndAlbumWithSendPermission;
import static online.kheops.auth_server.series.SeriesQueries.isOrphan;
import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.study.Studies.getStudy;


public class UserCapabilityPrincipal implements KheopsPrincipal, CapabilityPrincipal {
    private final Capability capability;
    private final User user;
    private final String originalToken;
    private final KheopsLogBuilder kheopsLogBuilder;


    public UserCapabilityPrincipal(Capability capability, User user, String originalToken) {
        this.capability = capability;
        this.user = user;
        this.originalToken = originalToken;

        kheopsLogBuilder = new KheopsLogBuilder()
                .provenance(this)
                .user(user.getSub())
                .tokenType(getTokenType());
    }

    @Override
    public String getName() {
        return user.getSub();
    }

    @Override
    public boolean hasUserAccess() { return true; }

    @Override
    public boolean hasSeriesViewAccess(String studyInstanceUID, String seriesInstanceUID) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            return canAccessSeries(user, studyInstanceUID, seriesInstanceUID, em);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean hasStudyViewAccess(String studyInstanceUID) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            final Study study = getStudy(studyInstanceUID, em);
            return canAccessStudy(user, study, em);
        } catch (StudyNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean hasSeriesDeleteAccess(String studyInstanceUID, String seriesInstanceUID) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            return canAccessSeries(user, studyInstanceUID, seriesInstanceUID, em);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean hasStudyDeleteAccess(String studyInstanceUID) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            final Study study = getStudy(studyInstanceUID, em);
            return canAccessStudy(user, study, em);
        } catch (StudyNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean hasSeriesShareAccess(String studyInstanceUID, String seriesInstanceUID) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            return canAccessSeries(user, studyInstanceUID, seriesInstanceUID, em);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean hasStudyShareAccess(String studyInstanceUID) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            final Study study = getStudy(studyInstanceUID, em);
            return canAccessStudy(user, study, em);
        } catch (StudyNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean hasSeriesAddAccess(String studyInstanceUID, String seriesInstanceUID) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final User mergeUser = em.merge(user);

        try {
            final Series series;
            try {
                series = getSeries(studyInstanceUID, seriesInstanceUID, em);
            } catch (SeriesNotFoundException e) {
                //if the series not exist
                return true;
            }

            // we need to check here if the series that was found is owned by the user
            if(isSeriesInInbox(mergeUser, series, em)) {
                return true;
            }

            try {
                findSeriesBySeriesAndAlbumWithSendPermission(mergeUser, series, em);
                return true;
            } catch (SeriesNotFoundException ignored) {
                if (isOrphan(series, em)) {
                    return true;
                }
            }
        } finally {
            em.close();
        }
        return false;
    }

    @Override
    public boolean hasAlbumPermission(AlbumUserPermissions usersPermission, String albumId) {

        if (!this.hasAlbumAccess(albumId)) {
            return false;
        }

        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            final Album album = getAlbum(albumId, em);
            final AlbumUser albumUser = getAlbumUser(album, user, em);
            if(user.getInbox() == album) {
                return false;
            }
            if (albumUser.isAdmin()) {
                return true;
            }
            return usersPermission.hasUserPermission(album);

        } catch (UserNotMemberException | AlbumNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean hasAlbumAccess(String albumId) {
        final EntityManager em = EntityManagerListener.createEntityManager();

        if (albumId == null) {
            return false;
        }
        try {
            final Album album = getAlbum(albumId, em);
            return isMemberOfAlbum(user, album, em);
        } catch (AlbumNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean hasInboxAccess() { return true; }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public ScopeType getScope() {
        return ScopeType.USER;
    }

    @Override
    public String getAlbumID() throws NotAlbumScopeTypeException {

        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .message("Error")
                .detail("this token is not an token with album scope")
                .build();
        throw new NotAlbumScopeTypeException(errorResponse);
    }

    @Override
    public Optional<String> getCapabilityTokenId() {
        return Optional.of(capability.getId());
    }

    @Override
    public KheopsLogBuilder getKheopsLogBuilder() {
        return kheopsLogBuilder;
    }

    @Override
    public String toString() {
        return "[CapabilityPrincipal user:" + getUser() + " scope:" + getScope() + " hasUserAccess:" + hasUserAccess() + " hasInboxAccess:" + hasInboxAccess() + "]";
    }

    @Override
    public Optional<Capability> getCapability() { return Optional.ofNullable(capability); }

    @Override
    public String getOriginalToken() { return originalToken; }

    private TokenType getTokenType() { return TokenType.USER_CAPABILITY_TOKEN; }
}
