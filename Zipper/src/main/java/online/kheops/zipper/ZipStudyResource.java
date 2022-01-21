package online.kheops.zipper;

import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.logging.Level.WARNING;
import static java.util.zip.Deflater.NO_COMPRESSION;
import static javax.ws.rs.core.HttpHeaders.*;
import static javax.ws.rs.core.Response.Status.BAD_GATEWAY;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.glassfish.jersey.media.multipart.Boundary.BOUNDARY_PARAMETER;

@javax.ws.rs.Path("/studies")
public final class ZipStudyResource {
    private static final Logger LOG = Logger.getLogger(ZipStudyResource.class.getName());

    private static final String ALBUM = "album";
    private static final String INBOX = "inbox";
    private static final String STUDY_INSTANCE_UID = "StudyInstanceUID";

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    private static final Client CLIENT = ClientBuilder.newClient();
    private static final String DICOM_ZIP_FILENAME = "DICOM.ZIP";

    @Context
    ServletContext context;

    @HeaderParam(HEADER_X_FORWARDED_FOR)
    String headerXForwardedFor;

    @GET
    @javax.ws.rs.Path("/{" + STUDY_INSTANCE_UID +"}")
    @Produces("application/zip")
    public Response streamStudy(@PathParam(STUDY_INSTANCE_UID) String studyInstanceUID,
                                @HeaderParam(AUTHORIZATION) String authorizationHeader,
                                @QueryParam(ALBUM) String fromAlbum,
                                @QueryParam(INBOX) Boolean fromInbox) {
        checkValidUID(studyInstanceUID, STUDY_INSTANCE_UID);

        final URI dicomWebProxyURI = dicomWebProxyURI();

        final WebTarget upstreamTarget =  CLIENT.target(dicomWebProxyURI)
                .path("/capabilities/password/dicomweb/studies/{StudyInstanceUID}");
        if (fromAlbum != null) {
            upstreamTarget.queryParam(ALBUM, fromAlbum);
        }
        if (fromInbox != null) {
            upstreamTarget.queryParam(INBOX, fromInbox);
        }

        final Response upstreamResponse;
        try {
            upstreamResponse = upstreamTarget.resolveTemplate("StudyInstanceUID", studyInstanceUID)
                    .request("multipart/related;type=\"application/dicom\"")
                    .header(AUTHORIZATION, authorizationHeader)
                    .header(HEADER_X_FORWARDED_FOR, headerXForwardedFor)
                    .get();
        } catch (ProcessingException e) {
            LOG.log(WARNING, "Unable to get the upstream", e);
            throw new WebApplicationException(Response.status(BAD_GATEWAY).entity("Error downloading the public key").build());
        }

        if (!upstreamResponse.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            upstreamResponse.close();
            if (upstreamResponse.getStatus() == UNAUTHORIZED.getStatusCode()) {
                LOG.log(WARNING, "Unauthorized");
                throw new WebApplicationException(Response.status(UNAUTHORIZED).build());
            } else {
                LOG.log(WARNING, "Unable to get upstream with status" + upstreamResponse.getStatus());
                throw new WebApplicationException(Response.status(BAD_GATEWAY).build());
            }
        }

        StreamingOutput streamingOutput = output -> {
            try (final InputStream inputStream = new BufferedInputStream(upstreamResponse.readEntity(InputStream.class));
                 final ZipOutputStream zipStream = new ZipOutputStream(output);
                 final DicomDirGenerator dicomDirGenerator = DicomDirGenerator.newInstance()) {
                zipStream.setLevel(NO_COMPRESSION);

                MultipartParser.Handler handler = (int partNumber, MultipartInputStream in) -> {
                    in.readHeaderParams();

                    try (final TeeInputStream teeInputStream = new TeeInputStream(new FilterInputStream(in) {
                                    @Override
                                    public void close() {/* close shield */}
                                }, zipStream)) {

                        final Path path = Paths.get("DICOM",
                                Integer.toString(partNumber/1000),
                                Integer.toString(partNumber));
                        zipStream.putNextEntry(new ZipEntry(path.toString()));
//                        dicomDirGenerator.add(teeInputStream, path);
                        teeInputStream.finish();
                        zipStream.closeEntry();
                    }
                };

                final String boundary = MediaType.valueOf(upstreamResponse.getHeaderString(CONTENT_TYPE)).getParameters().get(BOUNDARY_PARAMETER);
                new MultipartParser(boundary).parse(inputStream, handler);

//                zipStream.putNextEntry(new ZipEntry("DICOMDIR"));
//                dicomDirGenerator.write(zipStream);
//                zipStream.closeEntry();
                zipStream.flush();
            } finally {
                upstreamResponse.close();
            }
        };

        return Response.ok(streamingOutput)
                .type("application/zip")
                .header(CONTENT_DISPOSITION, "attachment; filename=\"" + DICOM_ZIP_FILENAME + "\"")
                .build();
    }

    private URI dicomWebProxyURI() {
        URI dicomWebURI;
        try {
            dicomWebURI = new URI(context.getInitParameter("online.kheops.dicomwebproxy.uri"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("online.kheops.dicomwebproxy.uri is not a valid URI", e);
        }
        return dicomWebURI;
    }

    private void checkValidUID(String uid, @SuppressWarnings("SameParameterValue") String name) {
        try {
            new Oid(uid);
        } catch (GSSException exception) {
            throw new BadRequestException(name + " is not a valid UID");
        }
    }
}
