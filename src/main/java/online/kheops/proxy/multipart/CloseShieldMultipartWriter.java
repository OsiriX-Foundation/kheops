package online.kheops.proxy.multipart;

import java.io.IOException;

public final class CloseShieldMultipartWriter implements MultipartWriter {

    private final MultipartWriter multipartWriter;
    private boolean closed;

    public CloseShieldMultipartWriter(final MultipartWriter multipartWriter) {
        this.multipartWriter = multipartWriter;
    }

    @Override
    public void writePart(StreamingBodyPart bodyPart) throws IOException {
        if (closed) {
            throw new IOException("writePart failed: multipartOutputStream is closed");
        } else {
            multipartWriter.writePart(bodyPart);
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            multipartWriter.flush();
        }
        closed = true;
    }

    @Override
    public void flush() throws IOException {
        if (closed) {
            throw new IOException("flush failed: multipartOutputStream is closed");
        } else {
            multipartWriter.flush();
        }
    }
}
