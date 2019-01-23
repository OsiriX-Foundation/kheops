package online.kheops.auth_server.keycloak;

public class KeycloakException extends Exception{
    public KeycloakException(String message) {
        super(message);
    }
    public KeycloakException(String message, Throwable e) {
        super(message, e);
    }
}
