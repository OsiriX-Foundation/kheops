package online.kheops.auth_server.capability;

import online.kheops.auth_server.entity.Capability;

import javax.xml.bind.annotation.XmlElement;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CapabilitiesResponses {

    private CapabilitiesResponses() { throw new IllegalStateException("Utility class"); }

    public static class CapabilityResponse {
        @XmlElement(name = "id")
        long id;

        @XmlElement(name = "secret")
        String secret;
        @XmlElement(name = "description")
        String description;

        @XmlElement(name = "creation_date")
        String creation;
        @XmlElement(name = "start_time")
        String start;
        @XmlElement(name = "expiration_date")
        String expiration;
        @XmlElement(name = "revoke_date")
        String revoke;

        @XmlElement(name = "revoked")
        boolean revoked;

        @XmlElement(name = "read_permission")
        boolean readPermission;
        @XmlElement(name = "write_permission")
        boolean writePermission;

        @XmlElement(name = "scope_type")
        String scopeType;
        @XmlElement(name = "scope_album")
        Long albumId;
        @XmlElement(name = "scope_series")
        String series;
        @XmlElement(name = "scope_study")
        String study;
    }

    public static CapabilityResponse CapabilityToCapabilitiesResponses(Capability capability) {

        final CapabilityResponse capabilityResponse = new CapabilityResponse();

        capabilityResponse.id = capability.getPk();

        capabilityResponse.secret = capability.getSecret();//TODO MUST BE REMOVE USE FOR DEBUG ONLY !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        capabilityResponse.description = capability.getTitle();
        capabilityResponse.expiration = ZonedDateTime.of(capability.getExpiration(), ZoneOffset.UTC).toString();
        capabilityResponse.revoked = capability.isRevoked();
        if (capability.isRevoked()) {
            capabilityResponse.revoke = ZonedDateTime.of(capability.getRevokedTime(), ZoneOffset.UTC).toString();
        }
        capabilityResponse.creation = ZonedDateTime.of(capability.getCreatedTime(), ZoneOffset.UTC).toString();
        if (capability.isActive()) {
            capabilityResponse.start = ZonedDateTime.of(capability.getStartTime(), ZoneOffset.UTC).toString();
        }

        capabilityResponse.readPermission = capability.isReadPermission();
        capabilityResponse.writePermission = capability.isWritePermission();

        capabilityResponse.scopeType = capability.getScopeType();
        switch (capability.getScopeType()){
            case "album":
                capabilityResponse.albumId = capability.getAlbum().getPk();
                break;
            case "series":
                capabilityResponse.series = capability.getSeries().getSeriesInstanceUID();
                break;
            case "study":
                capabilityResponse.study = capability.getStudy().getStudyInstanceUID();
                break;
            case "user":
                break;
        }

        return capabilityResponse;
    }
}
