package online.kheops.proxy.multipart;

import java.io.IOException;

public interface MultipartStreamingOutput {

    void write(MultipartOutputStream output) throws IOException;

}
