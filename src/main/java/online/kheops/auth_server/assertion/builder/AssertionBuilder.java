package online.kheops.auth_server.assertion.builder;

import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;

public interface AssertionBuilder {
    Assertion build(String assertionToken) throws BadAssertionException;
}
