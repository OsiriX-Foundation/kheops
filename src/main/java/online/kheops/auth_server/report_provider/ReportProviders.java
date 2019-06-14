package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Mutation;
import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.util.PairListXTotalCount;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.event.Events.reportProviderMutation;
import static online.kheops.auth_server.report_provider.ReportProviderQueries.*;

public class ReportProviders {

    private ReportProviders() {
        throw new IllegalStateException("Utility class");
    }

    public static ReportProviderResponse newReportProvider(User callingUser, String albumId, String name, String url)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final ReportProvider reportProvider;

        try {
            tx.begin();

            final Album album = getAlbum(albumId, em);
            reportProvider = new ReportProvider(url, name, album);

            callingUser = em.merge(callingUser);
            final Mutation mutation = reportProviderMutation(callingUser, album, reportProvider, Events.MutationType.CREATE_REPORT_PROVIDER);

            em.persist(mutation);
            em.persist(reportProvider);
            tx.commit();

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return new ReportProviderResponse(reportProvider);
    }

    public static ReportProviderClientMetadataResponse callConfigURL(ReportProvider reportProvider)
            throws ReportProviderUriNotValidException {

        try {
            return ClientBuilder.newClient().target(reportProvider.getUrl()).request().get(ReportProviderClientMetadataResponse.class);
        } catch (Exception e) {
            throw new ReportProviderUriNotValidException("report provider uri not valid");
        }
    }

    public static String getRedirectUri(ReportProvider reportProvider)
            throws ReportProviderUriNotValidException {
        ReportProviderClientMetadataResponse clientMetadata = callConfigURL(reportProvider);
        return clientMetadata.getRedirectUri();
    }

    public static String getJwksUri(ReportProvider reportProvider)
            throws ReportProviderUriNotValidException {
        ReportProviderClientMetadataResponse clientMetadata = callConfigURL(reportProvider);
        return clientMetadata.getJwksUri();
    }

    public static String getConfigIssuer(ReportProvider reportProvider) throws ReportProviderUriNotValidException{
        final URI configurationUri;
        try {
            configurationUri = new URI(reportProvider.getUrl());
        } catch (URISyntaxException e) {
            throw new ReportProviderUriNotValidException("Unable to get issuer from the report provider configuration URL", e);
        }

//        if (!configurationUri.getScheme().equals("https") && !configurationUri.getHost().equals("localhost")) {
//            throw new ReportProviderUriNotValidException("Non https configuration URIs are only allowed for localhost");
//        }

        return configurationUri.getScheme() + "://" + configurationUri.getAuthority();
    }

    public static boolean isValidConfigUrl(String configUrl) {

        try {
            getClientMetadata(configUrl);
            return true;
        } catch (ReportProviderUriNotValidException e) {
            return false;
        }
    }

    public static ReportProviderClientMetadataResponse getClientMetadata (String configUrl)
    throws ReportProviderUriNotValidException {
        try {
            new URI(configUrl);
        } catch (URISyntaxException e) {
            throw new ReportProviderUriNotValidException("syntax not valid");
        }

        try {
            final ClientConfig configuration = new ClientConfig();
            configuration.property(ClientProperties.CONNECT_TIMEOUT, 5000);
            configuration.property(ClientProperties.READ_TIMEOUT, 5000);


            ReportProviderClientMetadataResponse clientMetadata = ClientBuilder.newClient(configuration).target(configUrl).request().get(ReportProviderClientMetadataResponse.class);
            if (clientMetadata.isValid()) {
                return clientMetadata;
            }
        } catch (Exception e) {
            throw new ReportProviderUriNotValidException("error during request");
        }
        throw new ReportProviderUriNotValidException("uri not valid");
    }


    public static PairListXTotalCount<ReportProviderResponse> getReportProviders(String albumId, Integer limit, Integer offset) {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        final List<ReportProviderResponse> reportProviders = new ArrayList<>();
        final long totalCount;
        final List<ReportProvider> reportProvidersEntity;

        try {
            tx.begin();
            reportProvidersEntity = getReportProvidersWithAlbumId(albumId, limit, offset, em);
            totalCount = countReportProviderWithAlbumId(albumId, em);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        for (ReportProvider reportProvider : reportProvidersEntity) {
            reportProviders.add(new ReportProviderResponse(reportProvider));
        }
        return new PairListXTotalCount<>(totalCount, reportProviders);
    }

    public static ReportProviderResponse getReportProvider(String albumId, String clientId)
            throws ClientIdNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final ReportProvider reportProvider;

        try {
            tx.begin();

            reportProvider = getReportProviderWithClientId(clientId, em);

            if (!reportProvider.getAlbum().getId().equals(albumId)) {
                throw new ClientIdNotFoundException("ClientId: " + clientId + " Not Found for the album " + albumId);
            }

            tx.commit();
        } catch (NoResultException e) {
            throw new ClientIdNotFoundException("ClientId: " + clientId + " Not Found");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return new ReportProviderResponse(reportProvider);
    }

    public static void deleteReportProvider(User callingUser, String albumId, String clientId)
            throws ClientIdNotFoundException, AlbumNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final ReportProvider reportProvider;

        try {
            tx.begin();

            reportProvider = getReportProviderWithClientId(clientId, em);

            if (!reportProvider.getAlbum().getId().equals(albumId)) {
                throw new ClientIdNotFoundException("ClientId: " + clientId + " Not Found for the album " + albumId);
            }

            reportProvider.setAsRemoved();

            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);
            final Mutation mutation = reportProviderMutation(callingUser, album, reportProvider, Events.MutationType.DELETE_REPORT_PROVIDER);
            em.persist(mutation);

            tx.commit();
        } catch (NoResultException e) {
            throw new ClientIdNotFoundException("ClientId: " + clientId + " Not Found");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static ReportProviderResponse editReportProvider(User callingUser, String albumId, String clientId, String url, String name, boolean newClientId)
            throws ClientIdNotFoundException, AlbumNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final ReportProvider reportProvider;

        try {
            tx.begin();

            reportProvider = getReportProviderWithClientId(clientId, em);

            if (!reportProvider.getAlbum().getId().equals(albumId)) {
                throw new ClientIdNotFoundException("ClientId: " + clientId + " Not Found for the album " + albumId);
            }

            if (!(url == null || url.isEmpty())) {
                reportProvider.setUrl(url);
            }

            if (!(name == null || name.isEmpty())) {
                reportProvider.setName(name);
            }

            if (newClientId) {
                reportProvider.setClientId(new ClientId().getClientId());
            }

            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);
            final Mutation mutation = reportProviderMutation(callingUser, album, reportProvider, Events.MutationType.EDIT_REPORT_PROVIDER);
            em.persist(mutation);

            tx.commit();
        } catch (NoResultException e) {
            throw new ClientIdNotFoundException("ClientId: " + clientId + " Not Found");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return new ReportProviderResponse(reportProvider);
    }

    public static ReportProvider getReportProvider(String clientId)
            throws ClientIdNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final ReportProvider reportProvider;

        try {
            tx.begin();

            reportProvider = getReportProviderWithClientId(clientId, em);

            tx.commit();
        } catch (NoResultException e) {
            throw new ClientIdNotFoundException("ClientId: " + clientId + " Not Found");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return reportProvider;
    }
}
