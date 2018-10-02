package online.kheops.auth_server.capability;

public class CapabilityBadRequest extends Exception {

    public CapabilityBadRequest(String message) {
        super(message);
    }

    public CapabilityBadRequest() {
        super("Bad request");
    }

    public CapabilityBadRequest(String message, Throwable e) {
        super(message, e);
    }

    public CapabilityBadRequest(Throwable e) { super("Bad request", e); }
}
