package online.kheops.zipper;

public  final class InstanceData {
    private final Instance instance;
    private final byte[] bytes;

    private InstanceData(Instance instance, byte[] bytes) {
        this.instance = instance;
        this.bytes = bytes;
    }

    public static InstanceData newInstance(Instance instance, byte[] bytes) {
        if (instance == null) {
            throw new IllegalArgumentException("instance can not be null");
        }
        if (bytes == null) {
            throw new IllegalArgumentException("bytes can not be null");
        }

        return new InstanceData(instance, bytes);
    }

    public Instance getInstance() {
        return instance;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
