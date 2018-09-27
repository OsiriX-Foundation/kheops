package online.kheops.proxy;

public final class ContentLocation {
    private final String contentLocation;

    private ContentLocation(String contentLocation) {
        this.contentLocation = contentLocation;
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
        return contentLocation;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
