package online.kheops.auth_server.capability;

import online.kheops.auth_server.album.AlbumResponse;
import online.kheops.auth_server.entity.Capability;

import javax.xml.bind.annotation.XmlElement;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CapabilitiesResponse {

    private Response response = new Response();

    public static class AlbumScope {
        @XmlElement(name = "id")
        String id;
        @XmlElement(name = "name")
        String name;
    }

    public static class Response {
        @XmlElement(name = "id")
        String id;

        @XmlElement(name = "secret")
        String secret;
        @XmlElement(name = "title")
        String title;

        @XmlElement(name = "issued_at_time")
        String issuedAt;
        @XmlElement(name = "not_before_time")
        String notBeforeTime;
        @XmlElement(name = "expiration_time")
        String expirationTime;
        @XmlElement(name = "revoke_time")
        String revokeTime;

        @XmlElement(name = "revoked")
        boolean revoked;

        @XmlElement(name = "read_permission")
        Boolean readPermission;
        @XmlElement(name = "write_permission")
        Boolean writePermission;
        @XmlElement(name = "download_permission")
        Boolean downloadPermission;
        @XmlElement(name = "appropriate_permission")
        Boolean appropriatePermission;

        @XmlElement(name = "scope_type")
        String scopeType;
        @XmlElement(name = "album")
        AlbumScope albumScope;
        @XmlElement(name = "scope_series")
        String series;
        @XmlElement(name = "scope_study")
        String study;
    }

    public CapabilitiesResponse(Capability capability, boolean showSecret, boolean isIntrospect) {

        if(!isIntrospect) {
            response.id = capability.getId();
            if(showSecret) {
                response.secret = capability.getSecretBeforeHash();
            }
            response.title = capability.getTitle();
            response.issuedAt = ZonedDateTime.of(capability.getIssuedAtTime(), ZoneOffset.UTC).toString();

        }

        response.expirationTime = ZonedDateTime.of(capability.getExpirationTime(), ZoneOffset.UTC).toString();
        response.revoked = capability.isRevoked();
        if (capability.isActive()) {
            response.notBeforeTime = ZonedDateTime.of(capability.getNotBeforeTime(), ZoneOffset.UTC).toString();
        }
        if (capability.isRevoked()) {
            response.revokeTime = ZonedDateTime.of(capability.getRevokedTime(), ZoneOffset.UTC).toString();
        }
        response = ScopeType.valueOf(capability.getScopeType().toUpperCase()).setCapabilityResponse(response, capability);
    }

    public Response getResponse() {
        return response;
    }
}
