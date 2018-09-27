package online.kheops.proxy;


import online.kheops.proxy.part.BulkDataPart;
import online.kheops.proxy.part.DICOMMetadataPart;
import online.kheops.proxy.part.DICOMPart;
import online.kheops.proxy.part.Part;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.SAXReader;
import org.weasis.dicom.web.StowRS;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public final class STOWService {
    private final StowRS stowRS;

    STOWService(StowRS stowRS) {
        this.stowRS = stowRS;
    }

    public void write(Part part) throws STOWGatewayException {
        if (part instanceof DICOMPart) {
            writeDICOM((DICOMPart) part);
        } else if (part instanceof DICOMMetadataPart) {
            writeMetadata((DICOMMetadataPart) part);
        } else if (part instanceof BulkDataPart) {
            writeBulkData((BulkDataPart) part);
        } else {
            throw new ClassCastException("Unable to cast the part to a known Part class");
        }
    }


    public void writeBulkData(BulkDataPart bulkDataPart) {

    }
    public void writeMetadata(DICOMMetadataPart bulkDataPart) {

    }

    public void writeDICOM(DICOMPart dicomPart) throws STOWGatewayException {
        try {
            stowRS.uploadDicom(dicomPart.getDataset(), dicomPart.getTransferSyntax());
        } catch (IOException e) {
            throw new STOWGatewayException("Failed to store DICOMPart", e);
        }
    }


    public Attributes getResponse() throws IOException {
        final InputStream inputStream = stowRS.writeEndMarkersGetInputStream();
        if (inputStream == null) {
            return null;
        } else {
            try {
                return SAXReader.parse(inputStream);
            } catch (ParserConfigurationException | SAXException e) {
                throw new IOException("Error parsing response", e);
            }
        }
    }

}
