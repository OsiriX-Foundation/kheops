package online.kheops.auth_server.principal;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.accesstoken.AccessToken.*;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.report_provider.ClientIdNotFoundException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.user.AlbumUserPermissions;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.ForbiddenException;

import java.util.List;
import java.util.Optional;

import static online.kheops.auth_server.report_provider.ReportProviders.getReportProvider;
import static online.kheops.auth_server.series.Series.*;
import static online.kheops.auth_server.study.Studies.canAccessStudy;

public class ReportProviderPrincipal implements KheopsPrincipal {

    private EntityManager em;
    private EntityTransaction tx;
    private final User user;
    private final boolean hasReadAccess;
    private final boolean hasWriteAccess;
    private final String actingParty;
    private final String capabilityTokenId;

    private List<String> studyUids;
    private String clientId;
    private Album album;

    //old version
    private final Long dbid;
    public ReportProviderPrincipal(User user, String actingParty, String capabilityTokenId, List<String> studyUids,
                                   String clientId, boolean hasReadAccessAccess, boolean hasWriteAccess) {
        try {
            album = getReportProvider(clientId).getAlbum();
        } catch (ClientIdNotFoundException e) {
            throw new ForbiddenException("Client id not found");
        }
        this.clientId = clientId;
        this.studyUids = studyUids;
        this.dbid = user.getPk();
        this.user = user;
        this.actingParty = actingParty;
        this.capabilityTokenId = capabilityTokenId;
        this.hasReadAccess = hasReadAccessAccess;
        this.hasWriteAccess = hasWriteAccess;
    }
    @Override
    public long getDBID() {
        return dbid;
    }
    //end old version

    @Override
    public String getName() { return Long.toString(dbid); }

    @Override
    public boolean hasSeriesReadAccess(String studyInstanceUID, String seriesInstanceUID) {

        if (!hasReadAccess) {
            return false;
        }

        if (!studyUids.contains(studyInstanceUID)) {
            return false;
        }

        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();
            album = em.merge(album);

            return canAccessSeries(album, studyInstanceUID, seriesInstanceUID, em);

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Override
    public boolean hasStudyReadAccess(String studyInstanceUID) {
        return hasStudyAccess(studyInstanceUID);
    }

    @Override
    public boolean hasUserAccess() { return false; }

    @Override
    public boolean hasSeriesWriteAccess(String studyInstanceUID, String seriesInstanceUID)
            throws SeriesNotFoundException {

        if (!hasWriteAccess) {
            return false;
        }

        if (!studyUids.contains(studyInstanceUID)) {
            return false;
        }

        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();

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
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        throw new SeriesNotFoundException("SeriesUID : " + seriesInstanceUID + "from studyUID : " + studyInstanceUID + "not found");
    }

    @Override
    public boolean hasStudyWriteAccess(String studyInstanceUID) {
        return hasStudyAccess(studyInstanceUID);
    }

    @Override
    public boolean hasAlbumPermission(AlbumUserPermissions usersPermission, String albumId) {
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();

             album = em.merge(album);

             if(!album.getId().equals(albumId))  {
                 return false;
             }

            return usersPermission.hasProviderPermission(album);

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    @Override
    public boolean hasAlbumAccess(String albumId){
        return albumId.equals(album.getId());
    }

    @Override
    public boolean hasInboxAccess() { return false; }

    @Override
    public User getUser() { return user; }

    @Override
    public ScopeType getScope() { return ScopeType.ALBUM; }

    @Override
    public String getAlbumID() { return album.getId(); }

    @Override
    public KheopsLogBuilder getKheopsLogBuilder() {
        return new KheopsLogBuilder()
                .provenance(this)
                .user(getUser().getKeycloakId())
                .clientID(clientId)
                .tokenType(TokenType.REPORT_PROVIDER_TOKEN);
    }

    @Override
    public String toString() {
        return "[ReportProviderPrincipal user:" + getUser() + " dbid:" + getDBID() + " albumId:" + album.getId() + " clientID:" + clientId + "]";
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

    private boolean hasStudyAccess(String studyInstanceUID) {
        if (!studyUids.contains(studyInstanceUID)) {
            return false;
        }

        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();
            album = em.merge(album);
            tx.commit();

            return canAccessStudy(album, studyInstanceUID);

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}
