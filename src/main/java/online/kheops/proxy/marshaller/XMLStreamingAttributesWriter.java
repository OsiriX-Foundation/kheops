package online.kheops.proxy.marshaller;

import online.kheops.proxy.multipart.MultipartWriter;
import online.kheops.proxy.multipart.StreamingBodyPart;
import org.dcm4che3.data.Attributes;

import java.io.IOException;

import static org.dcm4che3.ws.rs.MediaTypes.APPLICATION_DICOM_XML_TYPE;

public class XMLStreamingAttributesWriter implements StreamingAttributesWriter {

    private final MultipartWriter multipartWriter;

    public XMLStreamingAttributesWriter(final MultipartWriter multipartWriter) {
        this.multipartWriter = multipartWriter;
    }

    @Override
    public void write(Attributes attributes) throws IOException {
        multipartWriter.writePart(new StreamingBodyPart(attributes, APPLICATION_DICOM_XML_TYPE));
    }

    @Override
    public void flush() throws IOException {
        multipartWriter.flush();
    }

    @Override
    public void close() throws IOException {
        multipartWriter.close();
    }
}