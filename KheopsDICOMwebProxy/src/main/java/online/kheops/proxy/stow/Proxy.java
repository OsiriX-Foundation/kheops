package online.kheops.proxy.stow;

import online.kheops.proxy.id.InstanceID;
import online.kheops.proxy.id.SeriesID;
import online.kheops.proxy.multipart.MultipartWriter
        ;
import online.kheops.proxy.multipart.StreamingBodyPart;
import online.kheops.proxy.part.Part;
import online.kheops.proxy.stow.authorization.AuthorizationManager;
import online.kheops.proxy.stow.authorization.AuthorizationManagerException;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LOCATION;
import static org.glassfish.jersey.media.multipart.Boundary.BOUNDARY_PARAMETER;

public final class Proxy {
    private static final Logger LOG = Logger.getLogger(Proxy.class.getName());

    private final Providers providers;

    private final InputStream inputStream;
    private final MediaType contentType;

    private final AuthorizationManager authorizationManager;

    private MultipartWriter multipartWriter;

    private final Consumer<Set<SeriesID>> sentSeries;

    public Proxy(final Providers providers, final MediaType contentType, final InputStream inputStream, final AuthorizationManager authorizationManager, Consumer<Set<SeriesID>> sentSeries)
    {
        this.providers = Objects.requireNonNull(providers);
        this.contentType = Objects.requireNonNull(contentType);
        this.inputStream = Objects.requireNonNull(inputStream);
        this.authorizationManager = Objects.requireNonNull(authorizationManager);
        this.sentSeries = sentSeries;
    }

    public void processStream(final MultipartWriter multipartWriter) throws GatewayException, RequestException
    {
        this.multipartWriter = Objects.requireNonNull(multipartWriter);

        final MultipartParser multipartParser = new MultipartParser(getBoundary());
        try {
            multipartParser.parse(inputStream, this::processPart);
        } catch (GatewayException e) {
            throw e;
        } catch (IOException e) {
            throw new RequestException("Error parsing input", e);
        }
    }

    private void processPart(final int partNumber, final MultipartInputStream multipartInputStream)
            throws GatewayException {

        final String[] partString = {"Unknown part"};
        List<String> fileID = new ArrayList<>();
        try (final Part part = Part.getInstance(contentType, providers, multipartInputStream, fileID::add)) {
            partString[0] = part.toString();
            Set<InstanceID> authorizedInstanceIDs = authorizationManager.getAuthorization(part, fileID.isEmpty() ? null : fileID.get(0));
            writePart(partNumber, authorizedInstanceIDs, part);
            if (sentSeries != null) {
                sentSeries.accept(authorizedInstanceIDs.stream()
                        .map((InstanceID::getSeriesID))
                        .collect(Collectors.toSet()));
            }
        } catch (GatewayException e) {
            throw e;
        } catch (IOException e) {
            authorizationManager.addProcessingFailure(fileID.isEmpty() ? null : fileID.get(0));
            LOG.log(WARNING, e, () ->  "IOException while parsing part:\n" + partNumber);
        } catch (AuthorizationManagerException e) {
            LOG.log(WARNING, e, () -> "Unable to get authorization for part:" + partNumber + ", " + partString[0]);
        }
    }

    private void writePart(final int partNumber, final Set<InstanceID> instanceIDs, final Part part) throws GatewayException {
        try (final InputStream partInputStream = part.newInputStreamForInstance(instanceIDs)) {
            final StreamingBodyPart streamingBodyPart = new StreamingBodyPart(partInputStream, part.getMediaType());
            part.getContentLocation().ifPresent(contentLocation -> streamingBodyPart.getHeaders().putSingle(CONTENT_LOCATION, contentLocation.toString()));
            multipartWriter.writePart(streamingBodyPart);
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
