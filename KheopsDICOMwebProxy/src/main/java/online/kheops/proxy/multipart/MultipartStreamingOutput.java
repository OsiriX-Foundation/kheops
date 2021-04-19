package online.kheops.proxy.multipart;

import java.io.IOException;

@FunctionalInterface
public interface MultipartStreamingOutput {
    void write(MultipartWriter writer) throws IOException;
}
