package online.kheops.proxy.part;

import online.kheops.proxy.id.ContentLocation;
import online.kheops.proxy.id.InstanceID;
import online.kheops.proxy.id.SeriesID;
import online.kheops.proxy.stream.TeeInputStream;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.ws.rs.MediaTypes;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public abstract class Part implements AutoCloseable {
    private final MediaType mediaType;
    private final Path cacheFilePath;

    Part(MediaType mediaType, Path cacheFilePath) {
        this.mediaType = mediaType;
        this.cacheFilePath = cacheFilePath;
    }

    public static Part getInstance(MultipartInputStream multipartInputStream) throws IOException {
        final Map<String, List<String>> headerParams = multipartInputStream.readHeaderParams();
        final ContentLocation contentLocation = ContentLocation.valueOf(getHeaderParamValue(headerParams, "content-location"));
        final MediaType mediaType = MediaType.valueOf(getHeaderParamValue(headerParams, "content-type"));

        final Path cacheFilePath = Files.createTempFile("PartCache", null);
        final OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(cacheFilePath));
        final TeeInputStream teeInputStream = new TeeInputStream(multipartInputStream, outputStream);

        final Part newPart;
        if (MediaTypes.equalsIgnoreParameters(mediaType, MediaTypes.APPLICATION_DICOM_TYPE)) {
            newPart = new DICOMPart(teeInputStream, mediaType, cacheFilePath);
        } else if (MediaTypes.equalsIgnoreParameters(mediaType, MediaTypes.APPLICATION_DICOM_XML_TYPE) || MediaTypes.equalsIgnoreParameters(mediaType, MediaTypes.APPLICATION_DICOM_JSON_TYPE)) {
            newPart = new DICOMMetadataPart(teeInputStream, mediaType, cacheFilePath);
        } else {
            newPart = new BulkDataPart(teeInputStream, mediaType, contentLocation, cacheFilePath);
        }

        outputStream.close();
        return newPart;
    }

    public Set<ContentLocation> getBulkDataLocations() {
        return Collections.emptySet();
    }

    public Optional<ContentLocation> getContentLocation() {
        return Optional.empty();
    }

    /**
     * @throws MissingAttributeException Overriding classes may throw this exception
     */
    public Optional<SeriesID> getSeriesID() throws MissingAttributeException {
        return Optional.empty();
    }

    /**
     * @throws MissingAttributeException Overriding classes may throw this exception
     */
    public Optional<InstanceID> getInstanceID() throws MissingAttributeException {
        return Optional.empty();
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    private static String getHeaderParamValue(Map<String, List<String>> headerParams, String key) {
        List<String> list = headerParams.get(key);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public void close() throws IOException {
        Files.delete(cacheFilePath);
    }

    public Path getCacheFilePath() {
        return cacheFilePath;
    }

    @Override
    public String toString() {
        final InstanceID instanceID;
        try {
            instanceID = getInstanceID().orElse(null);
        } catch (MissingAttributeException e) {
            return "Part with missing attribute";
        }
        if (instanceID == null) {
            return "Part with unknown Instance ID";
        } else {
            return "Part: " + instanceID;
        }
    }
}
