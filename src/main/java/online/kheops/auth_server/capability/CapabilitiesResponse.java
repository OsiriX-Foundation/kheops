package online.kheops.auth_server.capability;

import online.kheops.auth_server.album.AlbumResponse;
import online.kheops.auth_server.entity.Capability;

import javax.xml.bind.annotation.XmlElement;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CapabilitiesResponse {

    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "secret") // secret is deprecated
    private String secret;
    @XmlElement(name = "access_token") // prefer this to secret
    private String getAccessToken() {return secret;}
    @XmlElement(name = "token_type")
    private String tokenType = "Bearer";
    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "issued_at_time")
    private String issuedAt;
    @XmlElement(name = "not_before_time")
    private String notBeforeTime;
    @XmlElement(name = "last_used")
    private String lastUsed;
    @XmlElement(name = "expiration_time")
    private String expirationTime;
    @XmlElement(name = "revoke_time")
    private String revokeTime;

    @XmlElement(name = "revoked")
    private boolean revoked;

    @XmlElement(name = "read_permission")
    private Boolean readPermission;
    @XmlElement(name = "write_permission")
    private Boolean writePermission;
    @XmlElement(name = "download_permission")
    private Boolean downloadPermission;
    @XmlElement(name = "appropriate_permission")
    private Boolean appropriatePermission;

    @XmlElement(name = "scope_type")
    private String scopeType;
    @XmlElement(name = "album")
    private AlbumResponse albumResponse;
    @XmlElement(name = "scope_series")
    private String series;
    @XmlElement(name = "scope_study")
    private String study;
    @XmlElement(name = "created_by")
    private String createdBy;

    private CapabilitiesResponse() { /*empty*/ }

    public CapabilitiesResponse(Capability capability, boolean showSecret, boolean isIntrospect) {

        if (!isIntrospect) {
            id = capability.getId();
            if (showSecret) {
                secret = capability.getSecretBeforeHash();
            }
            title = capability.getTitle();
            issuedAt = ZonedDateTime.of(capability.getIssuedAtTime(), ZoneOffset.UTC).toString();
            if (capability.getLastUsed() != null) {
                lastUsed = ZonedDateTime.of(capability.getLastUsed(), ZoneOffset.UTC).toString();
            }
            createdBy = capability.getUser().getEmail();
        }

        expirationTime = ZonedDateTime.of(capability.getExpirationTime(), ZoneOffset.UTC).toString();
        revoked = capability.isRevoked();

        if (capability.isActive()) {
            notBeforeTime = ZonedDateTime.of(capability.getNotBeforeTime(), ZoneOffset.UTC).toString();
        }

        if (capability.isRevoked()) {
            revokeTime = ZonedDateTime.of(capability.getRevokedTime(), ZoneOffset.UTC).toString();
        }

        ScopeType.valueOf(capability.getScopeType().toUpperCase()).setCapabilityResponse(this, capability);
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }
    public void setReadPermission(Boolean readPermission) { this.readPermission = readPermission; }
    public void setWritePermission(Boolean writePermission) { this.writePermission = writePermission; }
    public void setDownloadPermission(Boolean downloadPermission) { this.downloadPermission = downloadPermission; }
    public void setAppropriatePermission(Boolean appropriatePermission) { this.appropriatePermission = appropriatePermission; }
    public void setAlbumResponse(AlbumResponse albumResponse) { this.albumResponse = albumResponse; }
    public void setSeries(String series) { this.series = series; }
    public void setStudy(String study) { this.study = study; }
}
