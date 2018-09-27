package online.kheops.proxy.part;

import online.kheops.proxy.ContentLocation;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.Optional;

public class BulkDataPart extends Part {
    private final InputStream inputStream;
    private final ContentLocation contentLocation;

    BulkDataPart(InputStream inputStream, MediaType mediaType, ContentLocation contentLocation) {
        super(mediaType);

        this.inputStream = inputStream;
        this.contentLocation = contentLocation;
    }

    public Optional<ContentLocation> getContentLocation() {
        return Optional.of(contentLocation);
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
