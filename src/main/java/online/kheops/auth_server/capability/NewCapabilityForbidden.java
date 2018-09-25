package online.kheops.auth_server.capability;

public class NewCapabilityForbidden extends Exception {

    public NewCapabilityForbidden(String message) {
        super(message);
    }

    public NewCapabilityForbidden(String message, Throwable e) {
        super(message, e);
    }

}
