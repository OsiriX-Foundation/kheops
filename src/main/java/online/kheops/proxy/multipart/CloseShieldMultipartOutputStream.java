package online.kheops.proxy.multipart;

import java.io.IOException;

public final class CloseShieldMultipartOutputStream implements MultipartOutputStream {

    private final MultipartOutputStream multipartOutputStream;
    private boolean closed;

    public CloseShieldMultipartOutputStream(final MultipartOutputStream multipartOutputStream) {
        this.multipartOutputStream = multipartOutputStream;
    }

    @Override
    public void writePart(StreamingBodyPart bodyPart) throws IOException {
        if (closed) {
            throw new IOException("writePart failed: multipartOutputStream is closed");
        } else {
            multipartOutputStream.writePart(bodyPart);
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            multipartOutputStream.flush();
        }
        closed = true;
    }

    @Override
    public void flush() throws IOException {
        if (closed) {
            throw new IOException("flush failed: multipartOutputStream is closed");
        } else {
            multipartOutputStream.flush();
        }
    }
}
