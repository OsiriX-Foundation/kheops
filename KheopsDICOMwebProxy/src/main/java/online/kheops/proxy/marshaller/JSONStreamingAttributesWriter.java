package online.kheops.proxy.marshaller;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.io.OutputStream;

public class JSONStreamingAttributesWriter implements StreamingAttributesWriter {

    private final JsonGenerator generator;
    private final JSONWriter jsonWriter;

    JSONStreamingAttributesWriter(OutputStream outputStream) {
        this.generator = Json.createGenerator(outputStream);
        this.jsonWriter = new JSONWriter(generator);

    }

    public void writeStart() throws IOException {
        try {
            generator.writeStartArray();
        } catch (JsonException e) {
            throw new IOException("Could not serialize data", e);
        }
    }

    @Override
    public void write(Attributes attributes) {
        jsonWriter.write(attributes);
    }

    public void writeEnd() throws IOException {
        try {
            generator.writeEnd();
        } catch (JsonException e) {
            throw new IOException("Could not serialize data", e);
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            generator.flush();
        } catch (JsonException e) {
            throw new IOException("Could not flush", e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            generator.close();
        } catch (JsonException e) {
            throw new IOException("Could not close", e);
        }
    }
}
