package online.kheops.auth_server.assertion;

final class CapabilityAssertionBuilder implements AssertionBuilder {
    @Override
    public Assertion build(String assertionToken) throws BadAssertionException {
        return CapabilityAssertion.getBuilder().build(assertionToken);
    }
}
