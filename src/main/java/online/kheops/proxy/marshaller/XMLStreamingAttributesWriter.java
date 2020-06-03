package online.kheops.proxy.marshaller;

import online.kheops.proxy.multipart.MultipartOutputStream;
import online.kheops.proxy.multipart.StreamingBodyPart;
import org.dcm4che3.data.Attributes;

import java.io.IOException;

import static org.dcm4che3.ws.rs.MediaTypes.APPLICATION_DICOM_XML_TYPE;

public class XMLStreamingAttributesWriter implements StreamingAttributesWriter {

    private final MultipartOutputStream multipartOutputStream;

    public XMLStreamingAttributesWriter(final MultipartOutputStream multipartOutputStream) {
        this.multipartOutputStream = multipartOutputStream;
    }

    @Override
    public void write(Attributes attributes) throws IOException {
        multipartOutputStream.writePart(new StreamingBodyPart(attributes, APPLICATION_DICOM_XML_TYPE));
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close()
    {
    }
}