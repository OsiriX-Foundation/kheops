package online.kheops.auth_server.principal;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.accesstoken.AccessToken.*;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.report_provider.*;
import online.kheops.auth_server.report_provider.ReportProvider;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.token.TokenAuthenticationContext;
import online.kheops.auth_server.user.AlbumUserPermissions;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.Source;

import javax.persistence.EntityManager;
import javax.ws.rs.ForbiddenException;

import java.util.List;
import java.util.Optional;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.series.Series.*;
import static online.kheops.auth_server.study.Studies.canAccessStudy;

public class ReportProviderPrincipal implements KheopsPrincipal, CapabilityPrincipal {

    private final User user;
    private final boolean hasReadAccess;
    private final boolean hasWriteAccess;
    private final String actingParty;
    private final String capabilityTokenId;
    private final KheopsLogBuilder kheopsLogBuilder;

    private final List<String> studyUids;
    private final Source source;
    private final String clientId;
    private final String originalToken;
    private final ReportProvider reportProvider;

    public ReportProviderPrincipal(TokenAuthenticationContext tokenAuthenticationContext, User user, String actingParty, String capabilityTokenId, List<String> studyUids, Source source,
                                   String clientId, boolean hasReadAccessAccess, boolean hasWriteAccess, String originalToken) {
        try {
            reportProvider = tokenAuthenticationContext.getReportProviderCatalogue().getReportProvider(clientId);
        } catch (ReportProviderNotFoundException e) {
            throw new ForbiddenException("Client id not found");
        }

        this.clientId = clientId;
        this.studyUids = studyUids;
        this.source = source;
        this.user = user;
        this.actingParty = actingParty;
        this.capabilityTokenId = capabilityTokenId;
        this.hasReadAccess = hasReadAccessAccess;
        this.hasWriteAccess = hasWriteAccess;
        this.originalToken = originalToken;

        kheopsLogBuilder = new KheopsLogBuilder()
                .provenance(this)
                .user(getUser().getSub())
                .clientID(clientId)
                .tokenType(TokenType.REPORT_PROVIDER_TOKEN);
    }

    @Override
    public String getName() { return user.getSub(); }

    @Override
    public boolean hasUserAccess() { return false; }

    @Override
    public boolean hasAlbumPermission(AlbumUserPermissions usersPermission, String albumId) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            if (source.isInbox() && !user.getInbox().getId().equals(albumId)) {
                return false;
            }
            if (!source.isInbox() && !source.getAlbumId().equals(albumId)) {
                return false;
            }

            final String albumRestriction = reportProvider.getAlbumIdRestriction().orElse(null);
            if (albumRestriction != null && !albumRestriction.equals(albumId)) {
                return false;
            }

            return usersPermission.hasProviderPermission(getAlbum(albumId, em));
        } catch (AlbumNotFoundException ignored) {
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean hasSeriesViewAccess(String studyInstanceUID, String seriesInstanceUID) {
        if (!hasReadAccess) {
            return false;
        }

        if (!studyUids.contains(studyInstanceUID)) {
            return false;
        }

        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            Album album;
            if (source.isInbox()) {
                album = user.getInbox();
            } else {
                album = getAlbum(source.getAlbumId(), em);
            }

            album = em.merge(album);
            return canAccessSeries(album, studyInstanceUID, seriesInstanceUID, em);
        } catch (AlbumNotFoundException ignored) {
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean hasStudyViewAccess(String studyInstanceUID) {
        if (!studyUids.contains(studyInstanceUID)) {
            return false;
        }

        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            Album album;
            if (source.isInbox()) {
                album = user.getInbox();
            } else {
                album = getAlbum(source.getAlbumId(), em);
            }

            album = em.merge(album);
            return canAccessStudy(album, studyInstanceUID);
        } catch (AlbumNotFoundException ignored) {
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean hasSeriesAddAccess(String studyInstanceUID, String seriesInstanceUID) {
        if (!hasWriteAccess) {
            return false;
        }

        if (!studyUids.contains(studyInstanceUID)) {
            return false;
        }

        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            Album album;
            if (source.isInbox()) {
                album = user.getInbox();
            } else {
                album = getAlbum(source.getAlbumId(), em);
            }

            if (!canAccessStudy(album, studyInstanceUID)) {
                return false;
            }

            //find if the series exist
            try {
                getSeries(studyInstanceUID, seriesInstanceUID, em);
            } catch (SeriesNotFoundException e) {
                return true;
            }

            // we need to check here if the series that was found is in the right album
            if(canAccessSeries(album, studyInstanceUID, seriesInstanceUID, em)) {
                return true;
            }
        } catch (AlbumNotFoundException ignored) {
            return false;
        } finally {
            em.close();
        }
        return false;
    }

    @Override
    public boolean hasAlbumAccess(String albumId) {
        return source.equals(Source.album(albumId));
    }

    @Override
    public boolean hasInboxAccess() {
        return source.isInbox();
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public ScopeType getScope() {
        return source.isInbox() ? ScopeType.USER : ScopeType.ALBUM;
    }

    @Override
    public String getAlbumID() {
        return source.getAlbumIdOrElse(null);
    }

    @Override
    public KheopsLogBuilder getKheopsLogBuilder() { return kheopsLogBuilder; }

    @Override
    public String toString() {
        return "[ReportProviderPrincipal user:" + getUser() + " source:" + source + " clientID:" + clientId + "]";
    }

    @Override
    public Optional<String> getClientId() {
        return Optional.of(clientId);
    }

    @Override
    public Optional<List<String>> getStudyList() {
        return Optional.of(studyUids);
    }

    @Override
    public Optional<String> getActingParty() {
        return Optional.ofNullable(actingParty);
    }

    @Override
    public Optional<String> getAuthorizedParty() {
        return Optional.of(clientId);
    }

    @Override
    public Optional<String> getCapabilityTokenId() {
        return Optional.ofNullable(capabilityTokenId);
    }

    @Override
    public String getOriginalToken() {
        return originalToken;
    }

}
