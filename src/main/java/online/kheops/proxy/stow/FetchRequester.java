package online.kheops.proxy.stow;

import online.kheops.proxy.tokens.AuthorizationToken;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

public class FetchRequester {
    private static final Logger LOG = Logger.getLogger(FetchRequester.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient();

    private final UriBuilder fetchUriBuilder;
    private final AuthorizationToken bearerToken;

    private final Set<String> studies = new HashSet<>();

    public static FetchRequester newFetchRequester(URI authorizationServerRoot, AuthorizationToken authorizationToken) {
        return new FetchRequester(authorizationServerRoot, authorizationToken);
    }

    private FetchRequester(URI authorizationServerRoot, AuthorizationToken authorizationToken) {
        this.bearerToken = Objects.requireNonNull(authorizationToken);
        fetchUriBuilder = UriBuilder.fromUri(Objects.requireNonNull(authorizationServerRoot)).path("studies/{StudyInstanceUID}/fetch");
    }

    public void addStudies(final Set<String> addedStudies) {
        studies.addAll(addedStudies);
    }

    public void fetch() {
        studies.forEach(this::triggerFetch);
    }

    private void triggerFetch(String studyInstanceUID) {
        URI uri = fetchUriBuilder.build(studyInstanceUID);

        try (final Response response = CLIENT.target(uri)
                    .request()
                    .header(AUTHORIZATION, bearerToken.getHeaderValue())
                    .post(Entity.text(""))) {
            if (response.getStatusInfo().getFamily() != SUCCESSFUL) {
                LOG.log(SEVERE, () -> "Error while triggering fetch for studyInstanceUID:" + studyInstanceUID + "status code:" + response.getStatus());
            }
        } catch (ProcessingException | WebApplicationException e) {
            LOG.log(SEVERE, "Error while triggering fetch for studyInstanceUID:" + studyInstanceUID, e);
        }
    }
}
