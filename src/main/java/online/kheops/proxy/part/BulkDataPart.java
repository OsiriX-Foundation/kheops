package online.kheops.proxy.part;

import online.kheops.proxy.id.ContentLocation;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public class BulkDataPart extends Part {
    private final InputStream inputStream;
    private final ContentLocation contentLocation;

    BulkDataPart(final InputStream inputStream, final MediaType mediaType, final ContentLocation contentLocation, final Path cacheFilePath) {
        super(mediaType, cacheFilePath);

        this.inputStream = inputStream;
        this.contentLocation = contentLocation;
    }

    @Override
    public Optional<ContentLocation> getContentLocation() {
        return Optional.of(contentLocation);
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
