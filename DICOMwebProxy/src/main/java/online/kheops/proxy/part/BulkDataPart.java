package online.kheops.proxy.part;

import online.kheops.proxy.id.ContentLocation;
import online.kheops.proxy.id.InstanceID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

class BulkDataPart extends Part {
    private final InputStream inputStream;
    private final ContentLocation contentLocation;

    BulkDataPart(final Providers providers, final InputStream inputStream, final MediaType mediaType, final ContentLocation contentLocation, final Path cacheFilePath) {
        super(providers, mediaType, cacheFilePath);

        this.inputStream = inputStream;
        this.contentLocation = contentLocation;
    }

    @Override
    public Optional<ContentLocation> getContentLocation() {
        return Optional.of(contentLocation);
    }

    @Override
    public InputStream newInputStreamForInstance(Set<InstanceID> instanceIDs) {
        if (instanceIDs.isEmpty()) {
            return inputStream;
        } else {
            throw new IllegalArgumentException("Asking for input stream from BulkDataPart for specific instanceIDs");
        }
    }
}
