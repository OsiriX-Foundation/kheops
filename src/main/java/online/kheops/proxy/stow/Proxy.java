package online.kheops.proxy.stow;

import online.kheops.proxy.multipart.MultipartOutputStream;
import online.kheops.proxy.multipart.StreamingBodyPart;
import online.kheops.proxy.part.Part;
import online.kheops.proxy.stow.authorization.AuthorizationManager;
import online.kheops.proxy.stow.authorization.AuthorizationManagerException;
import online.kheops.proxy.stow.resource.Resource;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;
import org.dcm4che3.ws.rs.MediaTypes;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Proxy {
    private static final Logger LOG = Logger.getLogger(Resource.class.getName());

    private final InputStream inputStream;
    private final MediaType contentType;

    private final MultipartOutputStream multipartOutputStream;
    private final AuthorizationManager authorizationManager;

    public Proxy(final MediaType contentType, final InputStream inputStream, final MultipartOutputStream multipartOutputStream, final AuthorizationManager authorizationManager)
                    throws GatewayException, RequestException {
        this.contentType = contentType;
        this.inputStream = inputStream;
        this.authorizationManager = authorizationManager;
        this.multipartOutputStream = multipartOutputStream;

        processMultipart();
    }

    private void processMultipart() throws RequestException, GatewayException {
        final MultipartParser multipartParser = new MultipartParser(getBoundary());
        try {
            multipartParser.parse(inputStream, this::processPart);
        } catch (RequestException | GatewayException e) {
            throw e;
        } catch (IOException e) {
            throw new GatewayException("Error parsing input", e);
        }
    }

    private void processPart(final int partNumber, final MultipartInputStream multipartInputStream)
            throws RequestException, GatewayException {

        String partString = "Unknown part";
        try (Part part = Part.getInstance(multipartInputStream)) {
            partString = part.toString();
            authorizationManager.getAuthorization(part);
            writePart(partNumber, part);
        } catch (GatewayException e) {
            throw e;
        } catch (IOException e) {
            throw new RequestException("Unable to parse for part:\n" + partNumber);
        } catch (AuthorizationManagerException e) {
            LOG.log(Level.WARNING, "Unable to get authorization for part:" + partNumber + ", " + partString);
            LOG.log(Level.FINE, "Authorization failure exception:\n" , e);
        }
    }

    private void writePart(final int partNumber, final Part part) throws GatewayException {
        try {
            final InputStream inputStream = Files.newInputStream(part.getCacheFilePath());
            multipartOutputStream.writePart(new StreamingBodyPart(inputStream, MediaTypes.APPLICATION_DICOM_TYPE));
            inputStream.close();
        } catch (IOException e) {
            throw new GatewayException("Unable to write part " + partNumber + ": " + part, e);
        }
    }

    private String getBoundary() throws RequestException {
        final String boundary = contentType.getParameters().get("boundary");
        if (boundary == null) {
            throw new RequestException("Missing Boundary Parameter");
        }

        return boundary;
    }
}
