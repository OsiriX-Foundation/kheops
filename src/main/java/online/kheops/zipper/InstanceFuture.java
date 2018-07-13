package online.kheops.zipper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public final class InstanceFuture implements Future<byte[]> {

    private final Instance instance;
    private final byte[] bytes;
    private final ExecutionException executionException;

    private InstanceFuture(Instance instance, byte[] bytes) {
        this.instance = instance;
        this.bytes = bytes;
        this.executionException = null;
    }
    private InstanceFuture(Instance instance, ExecutionException executionException) {
        this.instance = instance;
        this.bytes = null;
        this.executionException = executionException;

    }

    public static InstanceFuture newInstance(Instance instance, byte[] bytes) {
        return new InstanceFuture(instance, bytes);
    }

    public static InstanceFuture newInstance(Instance instance, ExecutionException executionException) {
        return new InstanceFuture(instance, executionException);
    }

    public Instance getInstance() {
        return instance;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public byte[] get() throws ExecutionException {
        if (executionException != null) {
            throw executionException;
        } else {
            return bytes;
        }
    }

    @Override
    public byte[] get(long timeout, TimeUnit unit) throws ExecutionException {
        return get();
    }
}
