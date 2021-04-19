package online.kheops.proxy.marshaller;

import org.dcm4che3.data.Attributes;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public interface StreamingAttributesWriter extends Flushable, Closeable {
    void write(Attributes attributes) throws IOException;
}
