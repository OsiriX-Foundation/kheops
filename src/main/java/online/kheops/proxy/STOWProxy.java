package online.kheops.proxy;

import online.kheops.proxy.part.Part;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class STOWProxy {
    private static final Logger LOG = Logger.getLogger(STOWResource.class.getName());

    private final InputStream inputStream;
    private final MediaType contentType;
    private final String boundary;

    private final STOWService stowService;

    private final AuthorizationManager authorizationManager;


    public STOWProxy(MediaType contentType, InputStream inputStream, STOWService stowService, AuthorizationManager authorizationManager)
            throws STOWRequestException {
        this.contentType = contentType;
        this.inputStream = inputStream;
        this.stowService = stowService;
        this.authorizationManager = authorizationManager;

        boundary = boundary();
    }

    public Response getResponse() throws STOWGatewayException, STOWRequestException {
        processMultipart();

        try {
            return authorizationManager.getResponse(stowService.getResponse());
        } catch (IOException e ) {
            throw new STOWGatewayException("Error getting a response", e);
        }
    }

    private void processMultipart() throws STOWRequestException, STOWGatewayException {
        MultipartParser multipartParser = new MultipartParser(boundary);
        try {
            multipartParser.parse(inputStream, this::processPart);
        } catch (STOWRequestException | STOWGatewayException e) {
            throw e;
        } catch (IOException e) {
            throw new STOWGatewayException("Error parsing input", e);
        }
    }

    private void processPart(int partNumber, MultipartInputStream multipartInputStream)
            throws STOWRequestException, STOWGatewayException {

        String partString = "Unknown part";
        try (Part part = Part.getInstance(multipartInputStream)) {
            partString = part.toString();
            authorizationManager.getAuthorization(part);
            writePart(partNumber, part);
        } catch (STOWGatewayException e) {
            throw e;
        } catch (IOException e) {
            throw new STOWRequestException("Unable to parse for part:\n" + partNumber);
        } catch (AuthorizationManagerException e) {
            LOG.log(Level.WARNING, "Unable to get authorization for part:" + partNumber + ", " + partString);
            LOG.log(Level.FINE, "Authorization failure exception:\n" , e);
        }
    }

    private void writePart(int partNumber, Part part) throws STOWGatewayException {
        try {
            stowService.write(part);
        } catch (IOException e) {
            throw new STOWGatewayException("Unable to write part " + partNumber + ": " + part, e);
        }
    }

    private String boundary() throws STOWRequestException {
        String boundary = contentType.getParameters().get("boundary");
        if (boundary == null) {
            throw new STOWRequestException("Missing Boundary Parameter");
        }

        return boundary;
    }
}
