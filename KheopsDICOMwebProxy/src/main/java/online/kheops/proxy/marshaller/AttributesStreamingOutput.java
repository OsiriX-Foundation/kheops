package online.kheops.proxy.marshaller;

import java.io.IOException;

@FunctionalInterface
public interface AttributesStreamingOutput {
    void write(StreamingAttributesWriter output) throws IOException;
}
