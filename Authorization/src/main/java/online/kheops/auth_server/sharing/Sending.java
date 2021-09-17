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
import online.kheops.auth_server.webhook.delayed_webhook.DelayedWebhook;
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
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static online.kheops.auth_server.album.AlbumQueries.deleteAllAlbumSeriesBySeries;
import static online.kheops.auth_server.album.AlbumQueries.deleteAllAlbumSeriesBySeriesList;
import static online.kheops.auth_server.album.Albums.*;
import static online.kheops.auth_server.event.EventQueries.*;
import static online.kheops.auth_server.report_provider.ReportProviderQueries.getReportProviderWithClientId;
import static online.kheops.auth_server.report_provider.ReportProviders.getReportProvider;
import static online.kheops.auth_server.series.Series.*;
import static online.kheops.auth_server.series.SeriesQueries.*;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.user.Users.getUser;
import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;
import static online.kheops.auth_server.util.ErrorResponse.Message.SERIES_NOT_FOUND;
import static online.kheops.auth_server.webhook.WebhookQueries.*;

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

            for (final Series series : seriesList) {
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

    public static void deleteStudyFromAlbum(ServletContext context, KheopsPrincipal kheopsPrincipal, String albumId, String studyInstanceUID, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, SeriesNotFoundException, UserNotMemberException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
            final Album callingAlbum = getAlbum(albumId, em);
            final AlbumUser callingAlbumUser = getAlbumUser(callingAlbum, callingUser, em);

            final List<Series> availableSeries = findSeriesListByStudyUIDFromAlbum(callingAlbum, studyInstanceUID, em);

            if (availableSeries.isEmpty()) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(SERIES_NOT_FOUND)
                        .detail("The series does not exist in the album")
                        .build();
                throw new SeriesNotFoundException(errorResponse);
            }

            final Source source = new Source(callingUser);
            final RemoveSeriesWebhook.Builder removeSeriesWebhookBuilder = new RemoveSeriesWebhook.Builder()
                    .albumId(albumId)
                    .kheopsInstance(context.getInitParameter(HOST_ROOT_PARAMETER))
                    .isManualTrigger(false);

            final Study study = availableSeries.get(0).getStudy();
            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                source.setCapabilityToken(capability);
                mutation = Events.albumPostStudyMutation(capability, callingAlbum, MutationType.REMOVE_STUDY, study, availableSeries);
                source.setCapabilityToken(capability);
            } else {
                mutation = Events.albumPostStudyMutation(callingUser, callingAlbum, MutationType.REMOVE_STUDY, study, availableSeries);
            }
            removeSeriesWebhookBuilder.source(source);
            removeSeriesWebhookBuilder.isAdmin(callingAlbumUser.isAdmin());

            removeSeriesWebhookBuilder.study(study);
            for (Series series : availableSeries) {
                callingAlbum.removeSeries(series, em);
                removeSeriesWebhookBuilder.addSeries(series);
                kheopsLogBuilder.series(series.getSeriesInstanceUID());
            }
            removeSeriesWebhookBuilder.removeAllSeries(true);

            em.persist(mutation);

            callingAlbum.updateLastEventTime();
            final List<WebhookAsyncRequest> webhookAsyncRequests = new ArrayList<>();

            for (Webhook webhook : callingAlbum.getWebhooks()) {
                if (webhook.getRemoveSeries() && webhook.isEnabled()) {
                    final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), false, WebhookType.REMOVE_SERIES, webhook);
                    em.persist(webhookTrigger);
                    availableSeries.forEach(webhookTrigger::addSeries);
                    webhookAsyncRequests.add(new WebhookAsyncRequest(webhook, removeSeriesWebhookBuilder.build(), webhookTrigger));
                }
            }
            tx.commit();
            kheopsLogBuilder.action(ActionType.REMOVE_STUDY)
                    .album(albumId)
                    .study(studyInstanceUID)
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

            final Source source = new Source(callingUser);
            final RemoveSeriesWebhook.Builder removeSeriesWebhookBuilder = new RemoveSeriesWebhook.Builder()
                    .albumId(albumId)
                    .kheopsInstance(context.getInitParameter(HOST_ROOT_PARAMETER))
                    .isManualTrigger(false)
                    .study(availableSeries.getStudy())
                    .addSeries(availableSeries);

            callingAlbum.removeSeries(availableSeries, em);
            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                mutation = Events.albumPostSeriesMutation(capability, callingAlbum, MutationType.REMOVE_SERIES, availableSeries);
                source.setCapabilityToken(capability);
            } else {
                mutation = Events.albumPostSeriesMutation(callingUser, callingAlbum, MutationType.REMOVE_SERIES, availableSeries);
            }
            removeSeriesWebhookBuilder.source(source);
            removeSeriesWebhookBuilder.isAdmin(callingAlbumUser.isAdmin());
            removeSeriesWebhookBuilder.removeAllSeries(findSeriesListByStudyUIDFromAlbum(callingAlbum, studyInstanceUID, em).isEmpty());

            em.persist(mutation);
            callingAlbum.updateLastEventTime();

            final List<WebhookAsyncRequest> webhookAsyncRequests = new ArrayList<>();

            for (Webhook webhook : callingAlbum.getWebhooks()) {
                if (webhook.getRemoveSeries() && webhook.isEnabled()) {
                    final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), false, WebhookType.REMOVE_SERIES, webhook);
                    em.persist(webhookTrigger);
                    webhookTrigger.addSeries(availableSeries);
                    webhookAsyncRequests.add(new WebhookAsyncRequest(webhook, removeSeriesWebhookBuilder.build(), webhookTrigger));
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

    public static void putSeriesInAlbum(DelayedWebhook delayedWebhook, KheopsPrincipal kheopsPrincipal, String albumId, String studyInstanceUID, String seriesInstanceUID, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, ClientIdNotFoundException, UserNotMemberException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
            final Album targetAlbum = getAlbum(albumId, em);
            if (!isMemberOfAlbum(callingUser, targetAlbum, em)) {
                throw new UserNotMemberException();
            }

            final Series availableSeries = getOrCreateSeries(studyInstanceUID, seriesInstanceUID, em);

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

            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                mutation = Events.albumPostSeriesMutation(capability, targetAlbum, MutationType.IMPORT_SERIES, availableSeries);
            } else if (kheopsPrincipal.getClientId().isPresent()) {
                ReportProvider reportProvider = getReportProvider(kheopsPrincipal.getClientId().orElseThrow(IllegalStateException::new));
                reportProvider = em.merge(reportProvider);
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


            final Source source = new Source(kheopsPrincipal.getUser());
            kheopsPrincipal.getCapability().ifPresent(source::setCapabilityToken);
            kheopsPrincipal.getClientId().ifPresent(clienrtId -> source.setReportProviderClientId(getReportProviderWithClientId(clienrtId, em)));
            delayedWebhook.addWebhookData(availableSeries.getStudy(), availableSeries, targetAlbum, false,
                    0, source, true, true);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void putStudyInAlbum(DelayedWebhook delayedWebhook, KheopsPrincipal kheopsPrincipal, String albumId, String studyInstanceUID, String fromAlbumId, Boolean fromInbox, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, SeriesNotFoundException, StudyNotFoundException, UserNotMemberException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
            final Album targetAlbum = getAlbum(albumId, em);
            if (!isMemberOfAlbum(callingUser, targetAlbum, em)) {
                throw new UserNotMemberException();
            }
            final Study study = getStudy(studyInstanceUID, em);

            final List<Series> availableSeries = getSeriesList(callingUser, studyInstanceUID, fromAlbumId, fromInbox, em);
            final List<Series> seriesListWebhook = new ArrayList<>();
            final List<Series> seriesListEvent = new ArrayList<>();

            boolean allSeriesAlreadyExist = true;
            for (Series series : availableSeries) {
                if (!targetAlbum.containsSeries(series, em)) {
                    final AlbumSeries albumSeries = new AlbumSeries(targetAlbum, series);
                    em.persist(albumSeries);
                    allSeriesAlreadyExist = false;
                    if (series.isPopulated() && series.getStudy().isPopulated()) {
                        seriesListWebhook.add(series);
                    }
                    seriesListEvent.add(series);
                }
                kheopsLogBuilder.series(series.getSeriesInstanceUID());
            }

            if (allSeriesAlreadyExist) {
                if (fromAlbumId != null) {
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

            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().orElseThrow(IllegalStateException::new));
                mutation = Events.albumPostStudyMutation(capability, targetAlbum, MutationType.IMPORT_STUDY, study, seriesListEvent);
            } else {
                mutation = Events.albumPostStudyMutation(callingUser, targetAlbum, MutationType.IMPORT_STUDY, study, seriesListEvent);
            }

            em.persist(mutation);

            targetAlbum.updateLastEventTime();

            if (fromAlbumId != null) {
                kheopsLogBuilder.fromAlbum(fromAlbumId);
            } else {
                kheopsLogBuilder.fromAlbum("inbox");
            }
            kheopsLogBuilder.album(albumId)
                    .action(ActionType.SHARE_STUDY_WITH_ALBUM)
                    .study(studyInstanceUID)
                    .log();


            final Source source = new Source(kheopsPrincipal.getUser());
            kheopsPrincipal.getCapability().ifPresent(source::setCapabilityToken);
            kheopsPrincipal.getClientId().ifPresent(clienrtId -> source.setReportProviderClientId(getReportProviderWithClientId(clienrtId, em)));
            for (Series s : seriesListWebhook) {
                delayedWebhook.addWebhookData(s.getStudy(), s, targetAlbum, false,
                        0, source, true, true);
            }
            tx.commit();
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
                if (fromAlbumId != null) {
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
            if (fromAlbumId != null) {
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
            if (inbox.containsSeries(series, em)) {
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

    public static void appropriateSeries(User callingUser, String studyInstanceUID, String seriesInstanceUID, KheopsLogBuilder kheopsLogBuilder)
            throws SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            kheopsLogBuilder.study(studyInstanceUID)
                    .series(seriesInstanceUID)
                    .action(ActionType.APPROPRIATE_SERIES);

            final Series series = getOrCreateSeries(studyInstanceUID, seriesInstanceUID, em);
            if (isOrphan(series, em)) {
                final Album inbox = callingUser.getInbox();
                final AlbumSeries inboxSeries = new AlbumSeries(inbox, series);
                em.persist(inboxSeries);
                tx.commit();
                kheopsLogBuilder.log();
            } else if (isSeriesInInbox(callingUser, series, em)) {
                kheopsLogBuilder.log();
            } else {
                final Series seriesWithPermission = findSeriesBySeriesAndAlbumWithSendPermission(callingUser, series, em);
                final Album inbox = callingUser.getInbox();
                final AlbumSeries inboxSeries = new AlbumSeries(inbox, seriesWithPermission);
                em.persist(inboxSeries);
                tx.commit();
                kheopsLogBuilder.log();
            }
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

            if (isSeriesInInbox(callingUser, storedSeries, em)) {
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
                if (!inbox.containsSeries(series, em)) {
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
            } else if (Boolean.TRUE.equals(fromInbox)) {
                availableSeriesUIDs = findAllSeriesInstanceUIDbySeriesIUIDfromInbox(callingUser, studyInstanceUID, em);
            } else {
                availableSeriesUIDs = findAllSeriesInstanceUIDbySeriesIUIDfromAlbumandInbox(callingUser, studyInstanceUID, em);
            }
        } finally {
            em.close();
        }

        return availableSeriesUIDs;
    }

    public static List<Series> getSeriesList(User callingUser, String studyInstanceUID, String fromAlbumId, Boolean fromInbox, EntityManager em)
            throws AlbumNotFoundException, SeriesNotFoundException {
        final List<Series> availableSeries;

        if (fromAlbumId != null) {
            final Album callingAlbum = getAlbum(fromAlbumId, em);
            availableSeries = findSeriesListByStudyUIDFromAlbum(callingAlbum, studyInstanceUID, em);
        } else if (Boolean.TRUE.equals(fromInbox)) {
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

    public static void deleteStudyFromKheops(final User user, final String studyInstanceUID) throws StudyNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final Study study = getStudy(studyInstanceUID, em);
            final List<Series> series = findSeriesListByStudyUIDFromAlbumAndInbox(user, study.getStudyInstanceUID(), em);

            deleteAllEventSeriesBySeriesList(series, em);

            final List<WebhookTrigger> webhookTriggers = getWebhookTriggersBySeriesList(series, em);

            deleteAllWebHookAttemptsByWebhookTriggers(webhookTriggers, em);
            deleteAllWebhookTriggerSeriesBySeriesList(series, em);
            deleteAllWebHookTriggersByWebhookPk(webhookTriggers, em);

            deleteAllAlbumSeriesBySeriesList(series, em);
            deleteSeriesList(series, em);

            deleteAllEventsByStudy(study, em);
            em.remove(study);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void permanentDeleteStudy(final String adminPassword, final String studyInstanceUID, final ServletContext context, final KheopsPrincipal kheopsPrincipal)
            throws StudyNotFoundException {
        if (adminPassword != null) {
            final String environmentAdminPassword = System.getenv("KHEOPS_AUTHORIZATION_ADMIN_PASSWORD");

            if (environmentAdminPassword.equals(adminPassword)) {
                deleteStudyFromKheops(kheopsPrincipal.getUser(), studyInstanceUID);
                deleteStudyFromPacs(studyInstanceUID);
            }
        }
    }

    public static void deleteStudyFromPacs(final String studyInstanceUID) throws StudyNotFoundException, ProcessingException {
        // TODO: Delete from dcm4chee docker exec -it pacsarc curl -XPOST http://127.0.0.1:8080/dcm4chee-arc/aets/DCM4CHEE/rs/studies/{STUDY_UID}/reject/113039%5EDCM

        final Response upstreamResponse;
        final Response upstreamResponse1;

        /*final URI uri = UriBuilder.fromUri(getDicomWebURI(context)).path("studies/{StudyInstanceUID}/reject/113039%5EDCM").build(studyInstanceUID);
        String authToken = PepAccessTokenBuilder.newBuilder(kheopsPrincipal)
                .withStudyUID(studyInstanceUID)
                .withAllSeries()
                .withSubject(kheopsPrincipal.getUser().getSub())
                .build();

        // Delete from PACS
                try {
                    upstreamResponse = ClientBuilder.newClient().target(uri).request().header("Authorization", "Bearer " + authToken).post(null);
                } catch (ProcessingException e) {
                    LOG.log(WARNING, "unable to reach upstream", e);
                    return Response.status(BAD_GATEWAY).build();
                }

                if (upstreamResponse.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
                    LOG.log(WARNING, "uri " + uri);
                    LOG.log(WARNING, () -> "bad response from upstream status = " + upstreamResponse.getStatus() + "\n" + upstreamResponse);
                    return Response.status(BAD_GATEWAY).build();
                }

                final URI uri1 = UriBuilder.fromUri(getDicomWebURI()).path("/reject/113039%5EDCM").build(studyInstanceUID);

                try {
                    upstreamResponse1 = ClientBuilder.newClient().target(uri1).request().header("Authorization", "Bearer " + authToken).delete();
                } catch (ProcessingException e) {
                    LOG.log(WARNING, "unable to reach upstream", e);
                    return Response.status(BAD_GATEWAY).build();
                }

                if (upstreamResponse1.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
                    LOG.log(WARNING, "uri " + uri);
                    LOG.log(WARNING, () -> "bad response from upstream status = " + upstreamResponse1.getStatus() + "\n" + upstreamResponse1);
                    return Response.status(BAD_GATEWAY).build();
                }*/

    }

    public static void permanentDeleteSeries(final String adminPassword, final String studyInstanceUID,
                                       final String seriesInstanceUID, final KheopsPrincipal kheopsPrincipal) throws SeriesNotFoundException {
        if (adminPassword != null) {
            final String environmentAdminPassword = System.getenv("KHEOPS_AUTHORIZATION_ADMIN_PASSWORD");

            if (environmentAdminPassword.equals(adminPassword)) {
                deleteSeriesFromKheops(kheopsPrincipal.getUser(), seriesInstanceUID);
                deleteSeriesFromPacs(seriesInstanceUID);
            }
        }
    }

    public static void deleteSeriesFromKheops(final User user, final String seriesInstanceUID) throws SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final Series series = getSeries(seriesInstanceUID, em);

            deleteAllEventSeriesBySeries(series, em);

            final List<WebhookTrigger> webhookTriggers = getWebhookTriggersBySeries(series, em);

            deleteAllWebHookAttemptsByWebhookTriggers(webhookTriggers, em);
            deleteAllWebhookTriggerSeriesBySeriesList(series, em);
            deleteAllWebHookTriggersByWebhookPk(webhookTriggers, em);

            deleteAllAlbumSeriesBySeries(series, em);
            em.remove(series);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteSeriesFromPacs(final String seriesInstanceUID) {

    }


        private static URI getDicomWebURI(final ServletContext context) {
        try {
            return new URI(context.getInitParameter("online.kheops.pacs.uri"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("online.kheops.pacs.uri is not a valid URI", e);
        }
    }
}