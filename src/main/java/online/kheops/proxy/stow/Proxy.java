package online.kheops.proxy.stow;

import online.kheops.proxy.part.Part;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Proxy {
    private static final Logger LOG = Logger.getLogger(Resource.class.getName());

    private final InputStream inputStream;
    private final MediaType contentType;
    private final String boundary;

    private final Service stowService;

    private final AuthorizationManager authorizationManager;


    public Proxy(MediaType contentType, InputStream inputStream, Service stowService, AuthorizationManager authorizationManager)
            throws RequestException {
        this.contentType = contentType;
        this.inputStream = inputStream;
        this.stowService = stowService;
        this.authorizationManager = authorizationManager;

        boundary = boundary();
    }

    public Response getResponse() throws GatewayException, RequestException {
        processMultipart();

        try {
            return authorizationManager.getResponse(stowService.getResponse());
        } catch (IOException e ) {
            throw new GatewayException("Error getting a response", e);
        }
    }

    private void processMultipart() throws RequestException, GatewayException {
        MultipartParser multipartParser = new MultipartParser(boundary);
        try {
            multipartParser.parse(inputStream, this::processPart);
        } catch (RequestException | GatewayException e) {
            throw e;
        } catch (IOException e) {
            throw new GatewayException("Error parsing input", e);
        }
    }

    private void processPart(int partNumber, MultipartInputStream multipartInputStream)
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

    private void writePart(int partNumber, Part part) throws GatewayException {
        try {
            stowService.write(part);
        } catch (IOException e) {
            throw new GatewayException("Unable to write part " + partNumber + ": " + part, e);
        }
    }

    private String boundary() throws RequestException {
        String boundary = contentType.getParameters().get("boundary");
        if (boundary == null) {
            throw new RequestException("Missing Boundary Parameter");
        }

        return boundary;
    }
}
