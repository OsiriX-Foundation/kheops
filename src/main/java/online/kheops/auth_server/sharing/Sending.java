package online.kheops.auth_server.sharing;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.event.MutationType;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.report_provider.ClientIdNotFoundException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.series.SeriesUIDFavoritePair;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.KheopsLogBuilder.*;
import online.kheops.auth_server.webhook.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.getAlbumUser;
import static online.kheops.auth_server.report_provider.ReportProviders.getReportProvider;
import static online.kheops.auth_server.series.Series.*;
import static online.kheops.auth_server.series.SeriesQueries.*;
import static online.kheops.auth_server.study.Studies.getOrCreateStudy;
import static online.kheops.auth_server.user.Users.getUser;
import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;
import static online.kheops.auth_server.util.ErrorResponse.Message.SERIES_NOT_FOUND;

public class Sending {

    private Sending() {
        throw new IllegalStateException("Utility class");
    }

    public static void deleteStudyFromInbox(User callingUser, String studyInstanceUID, KheopsLogBuilder kheopsLogBuilder)
            throws SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final List<Series> seriesList = findSeriesListByStudyUIDFromInbox(callingUser, studyInstanceUID, em);

            if (seriesList.isEmpty()) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(SERIES_NOT_FOUND)
                        .detail("The series does not exist in your inbox")
                        .build();
                throw new SeriesNotFoundException(errorResponse);
            }

            for (final Series series: seriesList) {
                callingUser.getInbox().removeSeries(series, em);
                kheopsLogBuilder.series(series.getSeriesInstanceUID());
            }

            tx.commit();
            kheopsLogBuilder.action(ActionType.REMOVE_STUDY)
                    .album("inbox")
                    .study(studyInstanceUID)
                    .log();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteSeriesFromInbox(User callingUser, String studyInstanceUID, String seriesInstanceUID, KheopsLogBuilder kheopsLogBuilder)
            throws SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Series series = findSeriesByStudyUIDandSeriesUIDFromInbox(callingUser, studyInstanceUID, seriesInstanceUID, em);
            callingUser.getInbox().removeSeries(series, em);

            tx.commit();
            kheopsLogBuilder.action(ActionType.REMOVE_SERIES)
                    .album("inbox")
                    .study(studyInstanceUID)
                    .series(seriesInstanceUID)
                    .log();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteStudyFromAlbum(KheopsPrincipal kheopsPrincipal, String albumId, String studyInstanceUID, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, SeriesNotFoundException {


        //TODO: webhook pour chaque séries supprimée
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
            final Album callingAlbum = getAlbum(albumId, em);

            final List<Series> availableSeries = findSeriesListByStudyUIDFromAlbum(callingAlbum, studyInstanceUID, em);

            if (availableSeries.isEmpty()) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(SERIES_NOT_FOUND)
                        .detail("The series does not exist in the album")
                        .build();
                throw new SeriesNotFoundException(errorResponse);
            }


            final Study study = availableSeries.get(0).getStudy();
            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                mutation = Events.albumPostStudyMutation(capability, callingAlbum, MutationType.REMOVE_STUDY, study, availableSeries);
            } else {
                mutation = Events.albumPostStudyMutation(callingUser, callingAlbum, MutationType.REMOVE_STUDY, study, availableSeries);
            }

            for(Series series : availableSeries) {
                callingAlbum.removeSeries(series, em);
                kheopsLogBuilder.series(series.getSeriesInstanceUID());
            }

            em.persist(mutation);

            callingAlbum.updateLastEventTime();

            tx.commit();
            kheopsLogBuilder.action(ActionType.REMOVE_STUDY)
                    .album(albumId)
                    .study(studyInstanceUID)
                    .log();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteSeriesFromAlbum(ServletContext context, KheopsPrincipal kheopsPrincipal, String albumId, String studyInstanceUID, String seriesInstanceUID, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, SeriesNotFoundException, UserNotMemberException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
            final Album callingAlbum = getAlbum(albumId, em);
            final AlbumUser callingAlbumUser = getAlbumUser(callingAlbum, callingUser, em);

            final Series availableSeries = findSeriesByStudyUIDandSeriesUIDFromAlbum(callingAlbum, studyInstanceUID, seriesInstanceUID, em);

            final RemoveSeriesWebhook removeSeriesWebhook = new RemoveSeriesWebhook(albumId, callingAlbumUser, availableSeries, context.getInitParameter(HOST_ROOT_PARAMETER), false);

            callingAlbum.removeSeries(availableSeries, em);
            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                mutation = Events.albumPostSeriesMutation(capability, callingAlbum, MutationType.REMOVE_SERIES, availableSeries);
                removeSeriesWebhook.setCapabilityToken(capability);
            } else {
                mutation = Events.albumPostSeriesMutation(callingUser, callingAlbum, MutationType.REMOVE_SERIES, availableSeries);
            }

            em.persist(mutation);
            callingAlbum.updateLastEventTime();

            final List<WebhookAsyncRequest> webhookAsyncRequests = new ArrayList<>();

            for (Webhook webhook : callingAlbum.getWebhooks()) {
                if (webhook.getRemoveSeries() && webhook.isEnabled()) {
                    final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), false, WebhookType.REMOVE_SERIES, webhook);
                    em.persist(webhookTrigger);
                    webhookTrigger.addSeries(availableSeries);
                    webhookAsyncRequests.add(new WebhookAsyncRequest(webhook, removeSeriesWebhook, webhookTrigger));
                }
            }
            tx.commit();
            kheopsLogBuilder.action(ActionType.REMOVE_SERIES)
                    .album(albumId)
                    .study(studyInstanceUID)
                    .series(seriesInstanceUID)
                    .log();
            for (WebhookAsyncRequest webhookAsyncRequest : webhookAsyncRequests) {
                webhookAsyncRequest.firstRequest();
            }
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void putSeriesInAlbum(ServletContext context, KheopsPrincipal kheopsPrincipal, String albumId, String studyInstanceUID, String seriesInstanceUID, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, ClientIdNotFoundException, UserNotMemberException, SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
            final Album targetAlbum = getAlbum(albumId, em);
            final AlbumUser targetAlbumUser = getAlbumUser(targetAlbum, callingUser, em);

            Series availableSeries = getOrCreateSeries(studyInstanceUID, seriesInstanceUID, em);

            if (targetAlbum.containsSeries(availableSeries, em)) {
                kheopsLogBuilder.action(ActionType.SHARE_SERIES_WITH_ALBUM)
                        .album(albumId)
                        .study(studyInstanceUID)
                        .series(seriesInstanceUID)
                        .log();
                return;
            }

            final AlbumSeries albumSeries = new AlbumSeries(targetAlbum, availableSeries);
            em.persist(albumSeries);

            final NewSeriesWebhook newSeriesWebhook = new NewSeriesWebhook(albumId, targetAlbumUser, availableSeries, context.getInitParameter(HOST_ROOT_PARAMETER), false);

            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                mutation = Events.albumPostSeriesMutation(capability, targetAlbum, MutationType.IMPORT_SERIES, availableSeries);
                newSeriesWebhook.setCapabilityToken(capability);
            } else if (kheopsPrincipal.getClientId().isPresent()) {
                ReportProvider reportProvider = getReportProvider(kheopsPrincipal.getClientId().orElseThrow(IllegalStateException::new));
                reportProvider = em.merge(reportProvider);
                newSeriesWebhook.setReportProvider(reportProvider);
                mutation = Events.newReport(callingUser, targetAlbum, reportProvider, MutationType.IMPORT_SERIES, availableSeries);
            } else {
                mutation = Events.albumPostSeriesMutation(callingUser, targetAlbum, MutationType.IMPORT_SERIES, availableSeries);
            }
            em.persist(mutation);
            targetAlbum.updateLastEventTime();

            kheopsLogBuilder.action(ActionType.SHARE_SERIES_WITH_ALBUM)
                    .album(albumId)
                    .study(studyInstanceUID)
                    .series(seriesInstanceUID)
                    .log();

            final List<WebhookAsyncRequest> webhookAsyncRequests = new ArrayList<>();

            if (availableSeries.isPopulated() && availableSeries.getStudy().isPopulated()) {
                for (Webhook webhook : targetAlbum.getWebhooks()) {
                    if (webhook.getNewSeries() && webhook.isEnabled()) {
                        final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), false, WebhookType.NEW_SERIES, webhook);
                        em.persist(webhookTrigger);
                        webhookTrigger.addSeries(availableSeries);
                        webhookAsyncRequests.add(new WebhookAsyncRequest(webhook, newSeriesWebhook, webhookTrigger));
                    }
                }
            }
            tx.commit();
            for (WebhookAsyncRequest webhookAsyncRequest : webhookAsyncRequests) {
                webhookAsyncRequest.firstRequest();
            }
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void putStudyInAlbum(ServletContext context, KheopsPrincipal kheopsPrincipal, String albumId, String studyInstanceUID, String fromAlbumId, Boolean fromInbox, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, SeriesNotFoundException, UserNotMemberException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
            final Album targetAlbum = getAlbum(albumId, em);
            final AlbumUser targetAlbumUser = getAlbumUser(targetAlbum, callingUser, em);
            final NewSeriesWebhook newSeriesWebhook = new NewSeriesWebhook(albumId, targetAlbumUser, context.getInitParameter(HOST_ROOT_PARAMETER),false);

            final List<Series> availableSeries = getSeriesList(callingUser, studyInstanceUID, fromAlbumId, fromInbox, em);
            final List<Series> seriesListWebhook = new ArrayList<>();
            final List<Series> seriesListEvent = new ArrayList<>();

            boolean allSeriesAlreadyExist = true;
            for (Series series: availableSeries) {
                if (!targetAlbum.containsSeries(series, em)) {
                    final AlbumSeries albumSeries = new AlbumSeries(targetAlbum, series);
                    em.persist(albumSeries);
                    allSeriesAlreadyExist = false;
                    if(series.isPopulated() && series.getStudy().isPopulated()) {
                        newSeriesWebhook.addSeries(series);
                        seriesListWebhook.add(series);
                    }
                    seriesListEvent.add(series);
                }
                kheopsLogBuilder.series(series.getSeriesInstanceUID());
            }

            if (allSeriesAlreadyExist) {
                if(fromAlbumId != null) {
                    kheopsLogBuilder.fromAlbum(fromAlbumId);
                } else {
                    kheopsLogBuilder.fromAlbum("inbox");
                }
                kheopsLogBuilder.album(albumId)
                        .action(ActionType.SHARE_STUDY_WITH_ALBUM)
                        .study(studyInstanceUID)
                        .log();
                return;
            }
            final Study study = availableSeries.get(0).getStudy();
            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                mutation = Events.albumPostStudyMutation(capability, targetAlbum, MutationType.IMPORT_STUDY, study, seriesListEvent);
                newSeriesWebhook.setCapabilityToken(capability);
            } else {
                mutation = Events.albumPostStudyMutation(callingUser, targetAlbum, MutationType.IMPORT_STUDY, study, seriesListEvent);
            }

            em.persist(mutation);

            targetAlbum.updateLastEventTime();

            if(fromAlbumId != null) {
                kheopsLogBuilder.fromAlbum(fromAlbumId);
            } else {
                kheopsLogBuilder.fromAlbum("inbox");
            }
            kheopsLogBuilder.album(albumId)
                    .action(ActionType.SHARE_STUDY_WITH_ALBUM)
                    .study(studyInstanceUID)
                    .log();

            final List<WebhookAsyncRequest> webhookAsyncRequests = new ArrayList<>();

            if(newSeriesWebhook.containSeries()) {
                for (Webhook webhook : targetAlbum.getWebhooks()) {
                    if (webhook.getNewSeries() && webhook.isEnabled()) {
                        final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), false, WebhookType.NEW_SERIES, webhook);
                        em.persist(webhookTrigger);
                        for (Series series : seriesListWebhook) {
                            webhookTrigger.addSeries(series);
                        }
                        webhookAsyncRequests.add(new WebhookAsyncRequest(webhook, newSeriesWebhook, webhookTrigger));
                    }
                }
            }
            tx.commit();
            for (WebhookAsyncRequest webhookAsyncRequest : webhookAsyncRequests) {
                webhookAsyncRequest.firstRequest();
            }
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void shareStudyWithUser(User callingUser, String targetUsername, String studyInstanceUID, String fromAlbumId, Boolean fromInbox, KheopsLogBuilder kheopsLogBuilder)
            throws UserNotFoundException, AlbumNotFoundException, SeriesNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final User targetUser = getUser(targetUsername, em);

            if (callingUser == targetUser) {
                if(fromAlbumId != null) {
                    appropriateStudy(callingUser, studyInstanceUID, fromAlbumId, kheopsLogBuilder);
                    return;
                }
                throw new BadRequestException("CallingUser ant targetUser are the same : it's for appropriate a study. But queryParam 'album' is null");
            }

            final List<Series> availableSeries = getSeriesList(callingUser, studyInstanceUID, fromAlbumId, fromInbox, em);

            final Album inbox = targetUser.getInbox();
            for (Series series : availableSeries) {
                if (!inbox.containsSeries(series, em)) {
                    final AlbumSeries albumSeries = new AlbumSeries(inbox, series);
                    em.persist(albumSeries);
                }
                kheopsLogBuilder.series(series.getSeriesInstanceUID());
            }

            tx.commit();
            if(fromAlbumId != null) {
                kheopsLogBuilder.fromAlbum(fromAlbumId);
            } else {
                kheopsLogBuilder.fromAlbum("inbox");
            }
            kheopsLogBuilder.targetUser(targetUser.getSub())
                    .action(ActionType.SHARE_STUDY_WITH_USER)
                    .study(studyInstanceUID)
                    .log();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

    }

    public static void shareSeriesWithUser(User callingUser, String targetUsername, String studyInstanceUID, String seriesInstanceUID, KheopsLogBuilder kheopsLogBuilder)
            throws UserNotFoundException, SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User targetUser = getUser(targetUsername, em);
            callingUser = em.merge(callingUser);

            kheopsLogBuilder.targetUser(targetUser.getSub())
                    .action(ActionType.SHARE_SERIES_WITH_USER)
                    .study(studyInstanceUID)
                    .series(seriesInstanceUID);

            if (targetUser == callingUser) { // the user is requesting access to a new series
                appropriateSeries(callingUser, studyInstanceUID, seriesInstanceUID, kheopsLogBuilder);
            }

            final Series series = getSeries(studyInstanceUID, seriesInstanceUID, em);
            final Album inbox = targetUser.getInbox();
            if(inbox.containsSeries(series, em)) {
                //target user has already access to the series

                kheopsLogBuilder.log();
                return;
            }

            final AlbumSeries albumSeries = new AlbumSeries(inbox, series);
            em.persist(albumSeries);

            tx.commit();
            kheopsLogBuilder.log();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void appropriateSeries(User callingUser, String studyInstanceUID, String seriesInstanceUID, KheopsLogBuilder kheopsLogBuilder) {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            kheopsLogBuilder.study(studyInstanceUID)
                    .series(seriesInstanceUID)
                    .action(ActionType.APPROPRIATE_SERIES);

            try {
                final Series storedSeries = getSeries(studyInstanceUID, seriesInstanceUID, em);

                if (isOrphan(storedSeries, em)) {
                    //here the series exists but she is orphan
                    final Album inbox = callingUser.getInbox();
                    final AlbumSeries inboxSeries = new AlbumSeries(inbox, storedSeries);
                    em.persist(inboxSeries);
                    tx.commit();

                    kheopsLogBuilder.log();
                    return; //appropriate OK
                } else if (isSeriesInInbox(callingUser, storedSeries, em)) {
                    kheopsLogBuilder.log();
                    return;
                } else {
                    final Series series = findSeriesBySeriesAndAlbumWithSendPermission(callingUser, storedSeries, em);
                    final Album inbox = callingUser.getInbox();
                    final AlbumSeries inboxSeries = new AlbumSeries(inbox, series);
                    em.persist(inboxSeries);
                    tx.commit();
                    kheopsLogBuilder.log();
                    return;
                }

            } catch (SeriesNotFoundException ignored) {/*empty*/}

            // from here the series does not exists
            // find if the study already exists
            final Study study = getOrCreateStudy(studyInstanceUID, em);

            final Series series = new Series(seriesInstanceUID);
            study.addSeries(series);
            final Album inbox = callingUser.getInbox();
            final AlbumSeries inboxSeries = new AlbumSeries(inbox, series);
            em.persist(series);
            em.persist(inboxSeries);
            tx.commit();
            kheopsLogBuilder.log();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void appropriateSeriesFromToken(User callingUser, String studyInstanceUID, String seriesInstanceUID, KheopsLogBuilder kheopsLogBuilder)
            throws SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            kheopsLogBuilder.study(studyInstanceUID)
                    .series(seriesInstanceUID)
                    .action(ActionType.APPROPRIATE_SERIES);

            final Series storedSeries = getSeries(studyInstanceUID, seriesInstanceUID, em);

            if(isSeriesInInbox(callingUser, storedSeries, em)) {
                kheopsLogBuilder.log();
                return;
            }

            final Album inbox = callingUser.getInbox();
            final AlbumSeries inboxSeries = new AlbumSeries(inbox, storedSeries);
            em.persist(inboxSeries);
            tx.commit();

            kheopsLogBuilder.log();

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void appropriateStudy(User callingUser, String studyInstanceUID, String albumId, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);
            final Album inbox = callingUser.getInbox();

            final List<Series> seriesLst = findSeriesListByStudyUIDFromAlbum(album, studyInstanceUID, em);

            for (Series series : seriesLst) {
                if(!inbox.containsSeries(series, em)) {
                    final AlbumSeries inboxSeries = new AlbumSeries(inbox, series);
                    em.persist(inboxSeries);
                }
                kheopsLogBuilder.series(series.getSeriesInstanceUID());
            }

            tx.commit();
            kheopsLogBuilder.action(ActionType.APPROPRIATE_STUDY)
                    .study(studyInstanceUID)
                    .fromAlbum(albumId)
                    .log();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static Map<String, Boolean> availableSeriesUIDs(User callingUser, String studyInstanceUID, String fromAlbumId, Boolean fromInbox)
            throws AlbumNotFoundException, StudyNotFoundException {
        Map<String, Boolean> availableSeriesUIDs;

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            callingUser = em.merge(callingUser);

            if (fromAlbumId != null) {
                final Album album = getAlbum(fromAlbumId, em);
                availableSeriesUIDs = findAllSeriesInstanceUIDbyStudyUIDfromAlbum(callingUser, album, studyInstanceUID, em);
            } else if (fromInbox) {
                availableSeriesUIDs = findAllSeriesInstanceUIDbySeriesIUIDfromInbox(callingUser, studyInstanceUID, em);
            } else {
                availableSeriesUIDs = findAllSeriesInstanceUIDbySeriesIUIDfromAlbumandInbox(callingUser, studyInstanceUID, em);
            }
        } finally {
            em.close();
        }

        return availableSeriesUIDs;
    }

    public static  List<Series> getSeriesList(User callingUser, String studyInstanceUID, String fromAlbumId, Boolean fromInbox, EntityManager em)
            throws AlbumNotFoundException , SeriesNotFoundException{
        final List<Series> availableSeries;

        if (fromAlbumId != null) {
            final Album callingAlbum = getAlbum(fromAlbumId, em);

            availableSeries = findSeriesListByStudyUIDFromAlbum(callingAlbum, studyInstanceUID, em);
        } else if (fromInbox) {
            availableSeries = findSeriesListByStudyUIDFromInbox(callingUser, studyInstanceUID, em);
        } else {
            availableSeries = findSeriesListByStudyUIDFromAlbumAndInbox(callingUser, studyInstanceUID, em);
        }

        if (availableSeries.isEmpty()) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("The series does not exist or you don't have access")
                    .build();
            throw new SeriesNotFoundException(errorResponse);
        }
        return availableSeries;
    }
}
