package online.kheops.auth_server.capability;

public class CapabilityBadRequestException extends Exception {

    public CapabilityBadRequestException(String message) {
        super(message);
    }

    public CapabilityBadRequestException() {
        super("Bad request");
    }

    public CapabilityBadRequestException(String message, Throwable e) {
        super(message, e);
    }

    public CapabilityBadRequestException(Throwable e) { super("Bad request", e); }
}
