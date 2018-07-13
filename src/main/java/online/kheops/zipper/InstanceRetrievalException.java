package online.kheops.zipper;

import java.util.concurrent.ExecutionException;

@SuppressWarnings("WeakerAccess")
public final class InstanceRetrievalException extends ExecutionException {
    public InstanceRetrievalException(String message) {
        super(message);
    }
}
