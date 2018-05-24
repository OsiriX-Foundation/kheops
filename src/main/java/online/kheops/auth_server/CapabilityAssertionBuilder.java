package online.kheops.auth_server;

import online.kheops.auth_server.entity.Capability;

public class CapabilityAssertionBuilder implements AssertionBuilder {
    @Override
    public Assertion build(String assertionToken) throws BadAssertionException {
        CapabilityAssertion capabilityAssertion = new CapabilityAssertion();
        capabilityAssertion.setCapabilityToken(assertionToken);
        return capabilityAssertion;
    }
}
