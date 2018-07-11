package online.kheops.zipper;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public  final class InstanceData {
    private final Instance instance;
    private final byte[] bytes;

    private InstanceData(Instance instance, byte[] bytes) {
        this.instance = Objects.requireNonNull(instance, "instance must not be null");
        this.bytes = bytes;
    }

    public static InstanceData newInstance(Instance instance, byte[] bytes) {
        return new InstanceData(instance, bytes);
    }

    public Instance getInstance() {
        return instance;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
