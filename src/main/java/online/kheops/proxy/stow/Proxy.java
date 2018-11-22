package online.kheops.proxy.stow;

import online.kheops.proxy.id.InstanceID;
import online.kheops.proxy.multipart.MultipartOutputStream;
import online.kheops.proxy.multipart.StreamingBodyPart;
import online.kheops.proxy.part.Part;
import online.kheops.proxy.stow.authorization.AuthorizationManager;
import online.kheops.proxy.stow.authorization.AuthorizationManagerException;
import online.kheops.proxy.stow.resource.Resource;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LOCATION;
import static org.glassfish.jersey.media.multipart.Boundary.BOUNDARY_PARAMETER;

public final class Proxy {
    private static final Logger LOG = Logger.getLogger(Resource.class.getName());

    private final Providers providers;

    private final InputStream inputStream;
    private final MediaType contentType;

    private final MultipartOutputStream multipartOutputStream;
    private final AuthorizationManager authorizationManager;

    public Proxy(final Providers providers, final MediaType contentType, final InputStream inputStream, final MultipartOutputStream multipartOutputStream, final AuthorizationManager authorizationManager)
                    throws GatewayException, RequestException {
        this.providers = providers;
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
        try (Part part = Part.getInstance(providers, multipartInputStream)) {
            partString = part.toString();
            writePart(partNumber, authorizationManager.getAuthorization(part), part);
        } catch (GatewayException e) {
            throw e;
        } catch (IOException e) {
            throw new RequestException("Unable to parse for part:\n" + partNumber);
        } catch (AuthorizationManagerException e) {
            LOG.log(WARNING, "Unable to get authorization for part:" + partNumber + ", " + partString);
            LOG.log(FINE, "Authorization failure exception:\n" , e);
        }
    }

    private void writePart(final int partNumber, final Set<InstanceID> instanceIDs, final Part part) throws GatewayException {
        try (final InputStream inputStream = part.newInputStreamForInstance(instanceIDs)) {
            final StreamingBodyPart streamingBodyPart = new StreamingBodyPart(inputStream, part.getMediaType());
            part.getContentLocation().ifPresent(contentLocation -> streamingBodyPart.getHeaders().putSingle(CONTENT_LOCATION, contentLocation.toString()));
            multipartOutputStream.writePart(streamingBodyPart);
        } catch (IOException e) {
            throw new GatewayException("Unable to write part " + partNumber + ": " + part, e);
        }
    }

    private String getBoundary() throws RequestException {
        final String boundary = contentType.getParameters().get(BOUNDARY_PARAMETER);
        if (boundary == null) {
            throw new RequestException("Missing Boundary Parameter");
        }

        return boundary;
    }
}
