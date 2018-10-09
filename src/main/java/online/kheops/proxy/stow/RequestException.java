package online.kheops.proxy.stow;

import java.io.IOException;

public class RequestException extends IOException {
    public RequestException(String message) {
        super(message);
    }
}
