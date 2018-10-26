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

    private static class ParsedData {
        private final Attributes dataset;
        private final Attributes fileMetaInformation;

        ParsedData(Attributes dataset, Attributes fileMetaInformation) {
            this.dataset = dataset;
            this.fileMetaInformation = fileMetaInformation;
        }

        Attributes getDataset() {
            return dataset;
        }

        Attributes getFileMetaInformation() {
            return fileMetaInformation;
        }
    }

    private DICOMPart(final ParsedData parsedData, final MediaType mediaType, final Path cacheFilePath) throws IOException {
        super(parsedData.getDataset(), mediaType, cacheFilePath);

        this.fileMetaInformation = parsedData.getFileMetaInformation();
    }

    DICOMPart(InputStream inputStream, MediaType mediaType, final Path cacheFilePath) throws IOException {
        this(parseInputStream(inputStream), mediaType, cacheFilePath);
    }

    private static ParsedData parseInputStream(InputStream inputStream) throws IOException{
        final DicomInputStream dicomInputStream = new DicomInputStream(inputStream);
        dicomInputStream.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);

        return new ParsedData(dicomInputStream.readDataset(-1, -1),
                              dicomInputStream.getFileMetaInformation());
    }

    public Attributes getFileMetaInformation() {
        return fileMetaInformation;
    }

    @Override
    public String getTransferSyntax() {
        return fileMetaInformation.getString(Tag.TransferSyntaxUID);
    }
}
