package online.kheops.auth_server.principal;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.accesstoken.AccessToken.*;
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
import static online.kheops.auth_server.series.SeriesQueries.*;
import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.study.Studies.getStudy;


public class CapabilityPrincipal implements KheopsPrincipal {
    private final Capability capability;
    private final User user;
    private final String originalToken;
    private final KheopsLogBuilder kheopsLogBuilder;

    private EntityManager em;

    public CapabilityPrincipal(Capability capability, User user,  String originalToken) {
        this.capability = capability;
        this.user = user;
        this.em = EntityManagerListener.createEntityManager();
        this.originalToken = originalToken;

        kheopsLogBuilder = new KheopsLogBuilder()
                .provenance(this)
                .user(user.getKeycloakId())
                .scope(capability.getScopeType())
                .tokenType(getTokenType());
    }

    @Override
    public String getName() {
        return user.getKeycloakId();
    }

    @Override
    public boolean hasSeriesReadAccess(String studyInstanceUID, String seriesInstanceUID) {
        this.em = EntityManagerListener.createEntityManager();
        try {
            if(getScope() == ScopeType.USER) {
                return canAccessSeries(user, studyInstanceUID, seriesInstanceUID, em);
            } else if (getScope() == ScopeType.ALBUM && capability.hasReadPermission()) {
                final AlbumUser albumUser = getAlbumUser(capability.getAlbum(), user, em);
                if (!albumUser.isAdmin()) { //the user who created the token is not longer an admin => normally the token should be removed
                    return false;
                }
                return canAccessSeries(capability.getAlbum(), studyInstanceUID, seriesInstanceUID, em);
            }
        } catch (UserNotMemberException e) {
            return false;
        } finally {
            em.close();
        }
        return false;
    }

    @Override
    public boolean hasStudyReadAccess(String studyInstanceUID) {
        this.em = EntityManagerListener.createEntityManager();
        try {
            final Study study = getStudy(studyInstanceUID, em);
            if(getScope() == ScopeType.USER) {
                return canAccessStudy(user, study, em);
            } else if (getScope() == ScopeType.ALBUM && capability.hasReadPermission()) {
                final AlbumUser albumUser = getAlbumUser(capability.getAlbum(), user, em);
                if (!albumUser.isAdmin()) {
                    return false;
                }
                return canAccessStudy(capability.getAlbum(), study, em);
            }
        } catch (StudyNotFoundException | UserNotMemberException e) {
            return false;
        } finally {
            em.close();
        }
        return false;
    }

    @Override
    public boolean hasUserAccess() { return getScope() == ScopeType.USER; }

    @Override
    public boolean hasSeriesWriteAccess(String studyInstanceUID, String seriesInstanceUID) {
        this.em = EntityManagerListener.createEntityManager();
        final User mergeUser = em.merge(user);
        if (getScope() == ScopeType.USER) {
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

        } else if (getScope() == ScopeType.ALBUM) {
            try {
                Capability mergeCapability = em.merge(capability);

                final AlbumUser albumUser = getAlbumUser(capability.getAlbum(), mergeUser, em);
                if (!albumUser.isAdmin()) {
                    return false;//if the creator of the token is no longer the admin of the album
                }
                final Series series;
                try {
                    series = getSeries(studyInstanceUID, seriesInstanceUID, em);
                } catch (SeriesNotFoundException e) {
                    //here the series not exist
                    return capability.hasWritePermission();
                }
                if (isOrphan(series, em)) {
                    return capability.hasWritePermission();
                }

                if(mergeCapability.getAlbum().containsSeries(series, em)) {
                    return capability.hasAppropriatePermission();
                }
            } catch (UserNotMemberException e) {
                return false;
            } finally {
                em.close();
            }
        }
        return false;
    }

    @Override
    public boolean hasStudyWriteAccess(String studyInstanceUID) {
        this.em = EntityManagerListener.createEntityManager();
        if (getScope() == ScopeType.USER) {
           return true;
        } else if (getScope() == ScopeType.ALBUM) {
            if (!canAccessStudy(capability.getAlbum(), studyInstanceUID)) {
                return capability.hasWritePermission();
            }
            return capability.hasAppropriatePermission();
        } else {
            return false;
        }
    }

    @Override
    public boolean hasAlbumPermission(AlbumUserPermissions usersPermission, String albumId) {

        if (!this.hasAlbumAccess(albumId)) {
            return false;
        }

        this.em = EntityManagerListener.createEntityManager();
        try {
            if(getScope() == ScopeType.ALBUM) {

                final Album album = em.merge(capability.getAlbum());

                if (!albumId.equals(album.getId())) {
                    return false;
                }

                return usersPermission.hasCapabilityPermission(capability);

            } else if (getScope() == ScopeType.USER) {

                final Album album = getAlbum(albumId, em);
                final AlbumUser albumUser = getAlbumUser(album, user, em);
                if(user.getInbox() == album) {
                    return false;
                }
                if (albumUser.isAdmin()) {
                    return true;
                }

                return usersPermission.hasUserPermission(album);
            }
        } catch (UserNotMemberException | AlbumNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
        return false;
    }

    @Override
    public boolean hasAlbumAccess(String albumId) {
        this.em = EntityManagerListener.createEntityManager();
        if (getScope() == ScopeType.ALBUM) {
            return albumId.equals(capability.getAlbum().getId());
        } else if (getScope() == ScopeType.USER) {
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
        } else {
            return false;
        }
    }

    @Override
    public boolean hasInboxAccess() {
        return getScope() == ScopeType.USER;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public ScopeType getScope() {
        return ScopeType.valueOf(capability.getScopeType().toUpperCase());
    }

    @Override
    public String getAlbumID() throws NotAlbumScopeTypeException {
        if(getScope() == ScopeType.ALBUM) {
            return capability.getAlbum().getId();
        } else {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Error")
                    .detail("this token is not an token with album scope")
                    .build();
            throw new NotAlbumScopeTypeException(errorResponse);
        }
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

    private boolean linkAuthorization;
    @Override
    public void setLink(boolean linkAuthorization) {
        this.linkAuthorization = linkAuthorization;
    }

    @Override
    public boolean isLink() { return linkAuthorization;  }

    @Override
    public String getOriginalToken() {
        return originalToken;
    }

    private TokenType getTokenType() {
        if (getScope() == ScopeType.ALBUM) {
            return TokenType.ALBUM_CAPABILITY_TOKEN;
        } else if (getScope() == ScopeType.USER) {
            return TokenType.USER_CAPABILITY_TOKEN;
        } else {
            throw new IllegalStateException("unknown scope type");
        }
    }
}
