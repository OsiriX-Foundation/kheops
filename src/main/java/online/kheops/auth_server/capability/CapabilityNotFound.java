package online.kheops.auth_server.capability;

public class CapabilityNotFound extends Exception {

    public CapabilityNotFound(String message) {
        super(message);
    }

    public CapabilityNotFound() {
        super("Unknown capability");
    }

    public CapabilityNotFound(String message, Throwable e) {
        super(message, e);
    }

    public CapabilityNotFound(Throwable e) { super("Unknown capability", e); }
}
