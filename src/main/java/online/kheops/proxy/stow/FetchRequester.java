package online.kheops.proxy.stow;

import online.kheops.proxy.id.SeriesID;
import online.kheops.proxy.tokens.AuthorizationToken;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Level.SEVERE;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

public class FetchRequester {
    private static final Logger LOG = Logger.getLogger(FetchRequester.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient();

    private final UriBuilder fetchUriBuilder;
    private final AuthorizationToken bearerToken;
    private String albumId;

    private final MultivaluedMap<String,SeriesID> studies = new MultivaluedHashMap<>();

    public static FetchRequester newFetchRequester(URI authorizationServerRoot, AuthorizationToken authorizationToken, String albumId) {
        return new FetchRequester(authorizationServerRoot, authorizationToken, albumId);
    }

    private FetchRequester(URI authorizationServerRoot, AuthorizationToken authorizationToken, String albumId) {
        this.bearerToken = Objects.requireNonNull(authorizationToken);
        this.albumId = albumId;
        fetchUriBuilder = UriBuilder.fromUri(Objects.requireNonNull(authorizationServerRoot)).path("studies/{StudyInstanceUID}/series/fetch");
    }

    public void addSeries(final Set<SeriesID> addedSeries) {
        for(SeriesID seriesID:addedSeries) {
            studies.add(seriesID.getStudyUID(), seriesID);
        }
    }

    public void fetch() {
        studies.forEach(this::triggerFetch);
    }

    private void triggerFetch(String studyInstanceUID, Collection<SeriesID> seriesIDs) {

        final Set<SeriesID> seriesIDSet = new HashSet<>(seriesIDs);

        final Form form = new Form();
        form.param("album", albumId);
        seriesIDSet.forEach(seriesID -> form.param("SeriesInstanceUID", seriesID.getSeriesUID()));

        URI uri = fetchUriBuilder.build(studyInstanceUID);

        try (final Response response = CLIENT.target(uri)
                    .request()
                    .header(AUTHORIZATION, bearerToken.getHeaderValue())
                    .post(Entity.form(form))) {
            if (response.getStatusInfo().getFamily() != SUCCESSFUL) {
                final String responseString = response.readEntity(String.class);
                LOG.log(SEVERE, () -> "Error while triggering fetch for studyInstanceUID:" + studyInstanceUID +
                        " series: " + seriesIDSet.toString() +
                        " status code:" + response.getStatus() +
                        " response:" + responseString);
            }
        } catch (ProcessingException | WebApplicationException e) {
            LOG.log(SEVERE, "Error while triggering fetch for studyInstanceUID:" + studyInstanceUID, e);
        }
    }
}
