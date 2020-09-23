package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Mutation;
import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.event.MutationType;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.PairListXTotalCount;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import online.kheops.auth_server.util.KheopsLogBuilder.ActionType;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;

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

    public static ReportProviderResponse newReportProvider(User callingUser, String albumId, String name, String url, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final ReportProvider reportProvider;

        try {
            tx.begin();

            final Album album = getAlbum(albumId, em);
            reportProvider = new ReportProvider(url, name, album, new ClientId(em).getClientId());

            callingUser = em.merge(callingUser);
            em.persist(reportProvider);

            final Mutation mutation = reportProviderMutation(callingUser, album, reportProvider, MutationType.CREATE_REPORT_PROVIDER);
            em.persist(mutation);

            album.updateLastEventTime();
            album.updateLastEventTime();
            tx.commit();

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        kheopsLogBuilder.album(albumId)
                .action(ActionType.NEW_REPORT_PROVIDER)
                .clientID(reportProvider.getClientId())
                .log();
        return new ReportProviderResponse(reportProvider, ReportProviderResponse.Type.FULL);
    }

    public static ReportProviderClientMetadata callConfigURL(ReportProvider reportProvider)
            throws ReportProviderUriNotValidException {

        try {
            ReportProviderClientMetadata clientMetadata = ClientBuilder.newClient().target(reportProvider.getUrl()).request().get(ReportProviderClientMetadata.class);

            ReportProviderClientMetadata.ValidationResult validationResult = clientMetadata.validateForConfigUri(reportProvider.getUrl());
            if (validationResult != ReportProviderClientMetadata.ValidationResult.OK) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("error")
                        .detail(validationResult.getDescription())
                        .build();
                throw new ReportProviderUriNotValidException(errorResponse);
            }
            return clientMetadata;
        } catch (ProcessingException | WebApplicationException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("error")
                    .detail("report provider uri not valid")
                    .build();
            throw new ReportProviderUriNotValidException(errorResponse, e);
        }
    }

    public static String getRedirectUri(ReportProvider reportProvider)
            throws ReportProviderUriNotValidException {
        ReportProviderClientMetadata clientMetadata = callConfigURL(reportProvider);
        return clientMetadata.getRedirectUri();
    }

    public static String getResponseType(ReportProvider reportProvider)
            throws ReportProviderUriNotValidException {
        ReportProviderClientMetadata clientMetadata = callConfigURL(reportProvider);
        return clientMetadata.getResponseType();
    }

    public static String getJwksUri(ReportProvider reportProvider)
            throws ReportProviderUriNotValidException {
        ReportProviderClientMetadata clientMetadata = callConfigURL(reportProvider);
        return clientMetadata.getJwksUri();
    }

    public static String getConfigIssuer(ReportProvider reportProvider) throws ReportProviderUriNotValidException{
        final URI configurationUri;
        try {
            configurationUri = new URI(reportProvider.getUrl());
        } catch (URISyntaxException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("error")
                    .detail("Unable to get issuer from the report provider configuration URL")
                    .build();
            throw new ReportProviderUriNotValidException(errorResponse, e);
        }

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

    public static ReportProviderClientMetadata getClientMetadata (String configUrl)
            throws ReportProviderUriNotValidException {
        try {
            new URI(configUrl);
        } catch (URISyntaxException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("error")
                    .detail("syntax not valid")
                    .build();
            throw new ReportProviderUriNotValidException(errorResponse);
        }

        try {
            final ClientConfig configuration = new ClientConfig();
            configuration.property(ClientProperties.CONNECT_TIMEOUT, 5000);
            configuration.property(ClientProperties.READ_TIMEOUT, 5000);


            ReportProviderClientMetadata clientMetadata = ClientBuilder.newClient(configuration).target(configUrl).request().get(ReportProviderClientMetadata.class);

            ReportProviderClientMetadata.ValidationResult validationResult = clientMetadata.validateForConfigUri(configUrl);
            if (validationResult != ReportProviderClientMetadata.ValidationResult.OK) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("error")
                        .detail(validationResult.getDescription())
                        .build();
                throw new ReportProviderUriNotValidException(errorResponse);
            }
            return clientMetadata;
        } catch (ProcessingException | WebApplicationException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("error")
                    .detail("error during request")
                    .build();
            throw new ReportProviderUriNotValidException(errorResponse);
        }
    }


    public static PairListXTotalCount<ReportProviderResponse> getReportProviders(String albumId, Integer limit, Integer offset, KheopsLogBuilder kheopsLogBuilder) {

        final EntityManager em = EntityManagerListener.createEntityManager();

        final List<ReportProviderResponse> reportProviders = new ArrayList<>();
        final long totalCount;
        final List<ReportProvider> reportProvidersEntity;

        try {
            reportProvidersEntity = getReportProvidersWithAlbumId(albumId, limit, offset, em);
            totalCount = countReportProviderWithAlbumId(albumId, em);
        } finally {
            em.close();
        }

        for (ReportProvider reportProvider : reportProvidersEntity) {
            reportProviders.add(new ReportProviderResponse(reportProvider, ReportProviderResponse.Type.FULL));
        }

        kheopsLogBuilder.album(albumId)
                .action(ActionType.LIST_REPORT_PROVIDERS)
                .log();
        return new PairListXTotalCount<>(totalCount, reportProviders);
    }

    public static ReportProviderResponse getReportProvider(String albumId, String clientId, KheopsLogBuilder kheopsLogBuilder)
            throws ClientIdNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final ReportProvider reportProvider;

        try {
            reportProvider = getReportProviderWithClientId(clientId, em);

            if (!reportProvider.getAlbum().getId().equals(albumId)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("Client ID not found")
                        .detail("Client ID not found")
                        .build();
                throw new ClientIdNotFoundException(errorResponse);
            }

        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Client ID not found")
                    .detail("Client ID not found")
                    .build();
            throw new ClientIdNotFoundException(errorResponse);
        } finally {
            em.close();
        }

        kheopsLogBuilder.action(ActionType.GET_REPORT_PROVIDER)
                .clientID(clientId)
                .album(albumId)
                .log();
        return new ReportProviderResponse(reportProvider, ReportProviderResponse.Type.FULL);
    }

    public static void deleteReportProvider(User callingUser, String albumId, String clientId, KheopsLogBuilder kheopsLogBuilder)
            throws ClientIdNotFoundException, AlbumNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final ReportProvider reportProvider;

        try {
            tx.begin();

            reportProvider = getReportProviderWithClientId(clientId, em);

            if (!reportProvider.getAlbum().getId().equals(albumId)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("Client ID not found")
                        .detail("Client ID not found")
                        .build();
                throw new ClientIdNotFoundException(errorResponse);
            }

            reportProvider.setAsRemoved();

            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);
            final Mutation mutation = reportProviderMutation(callingUser, album, reportProvider, MutationType.DELETE_REPORT_PROVIDER);
            em.persist(mutation);
            album.updateLastEventTime();
            tx.commit();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Client ID not found")
                    .detail("Client ID not found")
                    .build();
            throw new ClientIdNotFoundException(errorResponse);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        kheopsLogBuilder.album(albumId)
                .clientID(clientId)
                .action(ActionType.DELETE_REPORT_PROVIDER)
                .log();
    }

    public static ReportProviderResponse editReportProvider(User callingUser, String albumId, String clientId, String url, String name, KheopsLogBuilder kheopsLogBuilder)
            throws ClientIdNotFoundException, AlbumNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final ReportProvider reportProvider;

        try {
            tx.begin();

            reportProvider = getReportProviderWithClientId(clientId, em);

            if (!reportProvider.getAlbum().getId().equals(albumId)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("Client ID not found")
                        .detail("Client ID not found")
                        .build();
                throw new ClientIdNotFoundException(errorResponse);
            }

            if (!(url == null || url.isEmpty())) {
                reportProvider.setUrl(url);
                kheopsLogBuilder.scope("url");
            }

            if (!(name == null || name.isEmpty())) {
                reportProvider.setName(name);
                kheopsLogBuilder.scope("name");
            }

            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);
            final Mutation mutation = reportProviderMutation(callingUser, album, reportProvider, MutationType.EDIT_REPORT_PROVIDER);
            em.persist(mutation);
            album.updateLastEventTime();
            tx.commit();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Client ID not found")
                    .detail("Client ID not found")
                    .build();
            throw new ClientIdNotFoundException(errorResponse);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        kheopsLogBuilder.album(albumId)
                .clientID(clientId)
                .action(ActionType.EDIT_REPORT_PROVIDER)
                .log();
        return new ReportProviderResponse(reportProvider, ReportProviderResponse.Type.FULL);
    }

    public static ReportProvider getReportProvider(String clientId)
            throws ClientIdNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final ReportProvider reportProvider;

        try {
            reportProvider = getReportProviderWithClientId(clientId, em);
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Client ID not found")
                    .detail("Client ID not found")
                    .build();
            throw new ClientIdNotFoundException(errorResponse);
        } finally {
            em.close();
        }

        return reportProvider;
    }
}