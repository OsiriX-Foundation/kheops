package online.kheops.proxy.part;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import java.io.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

class DICOMPart extends DICOMMetadataPart {

    DICOMPart(final Providers providers, InputStream inputStream, MediaType mediaType, final Path cacheFilePath) throws IOException {
        super(providers, Collections.singleton(parseInputStream(inputStream)), mediaType, cacheFilePath);
    }

    private static Attributes parseInputStream(InputStream inputStream) throws IOException{
        final DicomInputStream dicomInputStream = new DicomInputStream(inputStream);
//        dicomInputStream.setBulkDataDescriptor(new BulkDataDescriptor() {
//            @Override
//            public boolean isBulkData(List<ItemPointer> itemPointer, String privateCreator, int tag, VR vr, int length) {
//                switch (tag) {
//                    case Tag.StudyInstanceUID:
//                    case Tag.SeriesInstanceUID:
//                    case Tag.SOPInstanceUID:
//                    case Tag.SOPClassUID:
//                        return false;
//                    default:
//                        return true;
//                }
//            }
//        });
        dicomInputStream.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);

        return dicomInputStream.readDataset(-1, -1);
    }
}
