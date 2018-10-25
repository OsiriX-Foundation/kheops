package online.kheops.auth_server.capability;

public class CapabilityNotFoundException extends Exception {

    public CapabilityNotFoundException(String message) {
        super(message);
    }

    public CapabilityNotFoundException() {
        super("Unknown capability");
    }

    public CapabilityNotFoundException(String message, Throwable e) {
        super(message, e);
    }

    public CapabilityNotFoundException(Throwable e) { super("Unknown capability", e); }
}
