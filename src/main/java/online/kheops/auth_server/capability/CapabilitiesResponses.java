package online.kheops.auth_server.capability;

import online.kheops.auth_server.entity.Capability;

import javax.xml.bind.annotation.XmlElement;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CapabilitiesResponses {

    private CapabilitiesResponses() { throw new IllegalStateException("Utility class"); }

    public static class CapabilityResponse {
        @XmlElement(name = "secret")
        String secret;
        @XmlElement(name = "description")
        String description;
        @XmlElement(name = "expiration")
        String expiration;
        @XmlElement(name = "revoked")
        boolean revoked;
    }

    public static CapabilityResponse CapabilityToCapabilitiesResponses(Capability capability) {

        final CapabilityResponse capabilityResponse = new CapabilityResponse();

        capabilityResponse.secret = capability.getSecret();
        capabilityResponse.description = capability.getTitle();
        capabilityResponse.expiration = ZonedDateTime.of(capability.getExpiration(), ZoneOffset.UTC).toString();
        capabilityResponse.revoked = capability.isRevoked();

        return capabilityResponse;
    }
}
