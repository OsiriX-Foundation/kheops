package online.kheops.auth_server.principal;

import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.token.TokenProvenance;
import online.kheops.auth_server.user.AlbumUserPermissions;
import online.kheops.auth_server.util.KheopsLogBuilder;

import java.security.Principal;
import java.util.List;
import java.util.Optional;


public interface KheopsPrincipal extends Principal, TokenProvenance {

    long getDBID();
    String getAlbumID() throws NotAlbumScopeTypeException, AlbumNotFoundException;

    boolean hasSeriesReadAccess(String study, String series) throws SeriesNotFoundException;
    boolean hasStudyReadAccess(String study);
    boolean hasUserAccess();
    boolean hasInboxAccess();

    boolean hasSeriesWriteAccess(String study, String series) throws SeriesNotFoundException;

    boolean hasStudyWriteAccess(String study);

    boolean hasAlbumPermission(AlbumUserPermissions usersPermission, String albumId) throws AlbumNotFoundException;

    boolean hasAlbumAccess(String albumId) throws AlbumNotFoundException;

    KheopsLogBuilder getKheopsLogBuilder();

    default Optional<Capability> getCapability() {return Optional.empty();}

    default Optional<List<String>> getStudyList() {return Optional.empty();}

    default Optional<String> getClientId() {return Optional.empty();}

    boolean isLink();

    String getOriginalToken();

    User getUser();

    ScopeType getScope();
}
