package online.kheops.proxy.id;

public final class ContentLocation {
    private final String location;

    private ContentLocation(String contentLocation) {
        this.location = contentLocation;
    }

    public static ContentLocation valueOf(String contentLocation) {
        return new ContentLocation(contentLocation);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ContentLocation && toString().equals(o.toString());
    }

    @Override
    public String toString() {
        return location;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
