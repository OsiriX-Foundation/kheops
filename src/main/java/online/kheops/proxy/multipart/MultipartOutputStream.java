package online.kheops.proxy.multipart;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public interface MultipartOutputStream extends Flushable, Closeable {

    void writePart(final StreamingBodyPart bodyPart) throws IOException;

}
