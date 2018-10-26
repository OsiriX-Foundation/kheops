package online.kheops.proxy.part;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class DICOMPart extends DICOMMetadataPart {
    private final Attributes fileMetaInformation;
    private final File bulkDataDirectory;

    private static class ParsedData {
        private final Attributes dataset;
        private final Attributes fileMetaInformation;
        private final File bulkDataDirectory;

        ParsedData(Attributes dataset, Attributes fileMetaInformation, File bulkDataDirectory) {
            this.dataset = dataset;
            this.fileMetaInformation = fileMetaInformation;
            this.bulkDataDirectory = bulkDataDirectory;
        }

        Attributes getDataset() {
            return dataset;
        }

        Attributes getFileMetaInformation() {
            return fileMetaInformation;
        }

        File getBulkDataDirectory() {
            return bulkDataDirectory;
        }
    }

    private DICOMPart(final ParsedData parsedData, final MediaType mediaType, final Path cacheFilePath) throws IOException {
        super(parsedData.getDataset(), mediaType, cacheFilePath);

        this.fileMetaInformation = parsedData.getFileMetaInformation();
        this.bulkDataDirectory = parsedData.getBulkDataDirectory();
    }

    DICOMPart(InputStream inputStream, MediaType mediaType, final Path cacheFilePath) throws IOException {
        this(parseInputStream(inputStream), mediaType, cacheFilePath);
    }

    private static ParsedData parseInputStream(InputStream inputStream) throws IOException{
        final DicomInputStream dicomInputStream = new DicomInputStream(inputStream);
        dicomInputStream.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);

        return new ParsedData(dicomInputStream.readDataset(-1, -1),
                              dicomInputStream.getFileMetaInformation(),
                              dicomInputStream.getBulkDataDirectory());
    }

    public Attributes getFileMetaInformation() {
        return fileMetaInformation;
    }

    public File getBulkDataDirectory() {
        return bulkDataDirectory;
    }

    @Override
    public String getTransferSyntax() {
        return fileMetaInformation.getString(Tag.TransferSyntaxUID);
    }

    @Override
    public void close() throws IOException
    {
        // TODO erase the bulk file cache
        super.close();
    }

}
