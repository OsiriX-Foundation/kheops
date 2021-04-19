package online.kheops.proxy.stow;

import java.io.IOException;

public class GatewayException extends IOException {
    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
