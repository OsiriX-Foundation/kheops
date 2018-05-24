package online.kheops.auth_server;

public interface AssertionBuilder {
    public Assertion build(String assertionToken) throws BadAssertionException;
}
