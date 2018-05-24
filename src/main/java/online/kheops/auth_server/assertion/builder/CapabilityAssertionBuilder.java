package online.kheops.auth_server.assertion.builder;

import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.assertion.CapabilityAssertion;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;

public class CapabilityAssertionBuilder implements AssertionBuilder {
    @Override
    public Assertion build(String assertionToken) throws BadAssertionException {
        CapabilityAssertion capabilityAssertion = new CapabilityAssertion();
        capabilityAssertion.setCapabilityToken(assertionToken);
        return capabilityAssertion;
    }
}
