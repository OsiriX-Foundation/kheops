package online.kheops.auth_server.assertion;

interface AssertionBuilder {
    Assertion build(String assertionToken) throws BadAssertionException;
}
