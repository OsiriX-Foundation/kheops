package online.kheops.auth_server.webhook;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.KheopsInstance;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static online.kheops.auth_server.album.Albums.*;
import static online.kheops.auth_server.event.MutationType.*;
import static online.kheops.auth_server.event.MutationType.EDIT_WEBHOOK;
import static online.kheops.auth_server.event.MutationType.TRIGGER_WEBHOOK;
import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.user.Users.getUser;
import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;
import static online.kheops.auth_server.util.Consts.VALID_SCHEMES_WEBHOOK_URL;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_FORM_PARAMETER;
import static online.kheops.auth_server.util.ErrorResponse.Message.SERIES_NOT_FOUND;
import static online.kheops.auth_server.util.KheopsLogBuilder.ActionType.*;
import static online.kheops.auth_server.webhook.WebhookQueries.findWebhookById;
import static online.kheops.auth_server.webhook.WebhookQueries.findWebhookTriggerById;

public class Webhooks {

    private Webhooks() {
        throw new IllegalStateException("Utility class");
    }

    public static WebhookResponse createWebhook(WebhookPostParameters webhookPostParameters, User callingUser, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final WebhookResponse webhookResponse;

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final Album album = getAlbum(webhookPostParameters.getAlbumId(), em);
            final Webhook newWebhook = new Webhook(webhookPostParameters, new WebhookId(em).getId(), album, callingUser);

            em.persist(newWebhook);

            final Mutation mutation = Events.albumPostMutation(callingUser, album, CREATE_WEBHOOK);
            em.persist(mutation);

            webhookResponse = new WebhookResponse(newWebhook);
            kheopsLogBuilder.action(NEW_WEBHOOK)
                    .album(webhookPostParameters.getAlbumId())
                    .webhookID(newWebhook.getId())
                    .log();
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return webhookResponse;
    }

    public static WebhookResponse editWebhook(WebhookPatchParameters webhookPatchParameters, User callingUser, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, WebhookNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final WebhookResponse webhookResponse;

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final Album album = getAlbum(webhookPatchParameters.getAlbumId(), em);
            final Webhook webhook = WebhookQueries.getWebhook(webhookPatchParameters.getwebhookId(), album, em);

            webhookPatchParameters.getUrl().ifPresent(webhook::setUrl);
            webhookPatchParameters.getName().ifPresent(webhook::setName);
            webhookPatchParameters.isNewSeries().ifPresent(webhook::setNewSeries);
            webhookPatchParameters.isRemoveSeries().ifPresent(webhook::setRemoveSeries);
            webhookPatchParameters.isNewUser().ifPresent(webhook::setNewUser);
            webhookPatchParameters.isDeleteAlbum().ifPresent(webhook::setDeleteAlbum);
            webhookPatchParameters.isEnabled().ifPresent(webhook::setEnabled);

            if(webhookPatchParameters.isRemoveSecret()) {
                webhook.setSecret(null);
            } else {
                webhookPatchParameters.getSecret().ifPresent(webhook::setSecret);
            }

            final Mutation mutation = Events.albumPostMutation(callingUser, album, EDIT_WEBHOOK);
            em.persist(mutation);

            webhookResponse = new WebhookResponse(webhook);

            int maxHistory = 5;
            for(WebhookTrigger webhookTrigger:webhook.getWebhookTriggers()) {
                webhookResponse.addTrigger(webhookTrigger);
                maxHistory--;
                if(maxHistory == 0) {
                    break;
                }
            }

            kheopsLogBuilder.action(KheopsLogBuilder.ActionType.EDIT_WEBHOOK)
                    .album(webhookPatchParameters.getAlbumId())
                    .webhookID(webhook.getId())
                    .log();

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return webhookResponse;
    }

    public static void deleteWebhook(String webhookID, String albumId, User callingUser, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, WebhookNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final Album album = getAlbum(albumId, em);
            final Webhook webhook = WebhookQueries.getWebhook(webhookID, album, em);

            deleteWebhook(webhook, em);

            final Mutation mutation = Events.albumPostMutation(callingUser, album, DELETE_WEBHOOK);
            em.persist(mutation);

            tx.commit();

            kheopsLogBuilder.action(REMOVE_WEBHOOK)
                    .album(albumId)
                    .webhookID(webhookID)
                    .log();

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteWebhook(Webhook webhook, EntityManager em) {
        for (WebhookTrigger webhookTrigger:webhook.getWebhookTriggers()) {
            for(WebhookAttempt webhookAttempt:webhookTrigger.getWebhookAttempts()) {
                em.remove(webhookAttempt);
            }
            webhookTrigger.removeAllSeries();
            em.remove(webhookTrigger);
        }
        em.remove(webhook);
    }


    public static WebhookResponse getWebhook(String webhookID, String albumId, Integer triggerLimit, Integer triggerOffset, KheopsInstance kheopsInstance, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, WebhookNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final WebhookResponse webhookResponse;

        try {
            tx.begin();

            final Album album = getAlbum(albumId, em);
            final Webhook webhook = WebhookQueries.getWebhook(webhookID, album, em);

            kheopsLogBuilder.action(GET_WEBHOOK)
                    .album(albumId)
                    .webhookID(webhookID)
                    .log();

            webhookResponse = new WebhookResponse(webhook);

            for(WebhookTrigger webhookTrigger:webhook.getWebhookTriggers()) {
                if (triggerOffset > 0) {
                    triggerOffset--;
                } else {
                    webhookResponse.addFullTriggers(webhookTrigger, kheopsInstance);
                    triggerLimit--;
                    if (triggerLimit == 0) {
                        break;
                    }
                }
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return webhookResponse;
    }
    public static PairListXTotalCount<WebhookResponse> getWebhooks(String albumId, String url, Integer limit, Integer offset)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final List<WebhookResponse> webhookResponseList = new ArrayList<>();
        final Long numberOfWebhook;

        try {
            tx.begin();

            final Album album = getAlbum(albumId, em);
            final List<Webhook> webhookList;
            if (url != null) {
                webhookList = WebhookQueries.getWebhooks(album, limit, url, offset, em);
                numberOfWebhook = WebhookQueries.getNumberOfWebhooks(album, url, em);
            } else {
                webhookList = WebhookQueries.getWebhooks(album, limit, offset, em);
                numberOfWebhook = WebhookQueries.getNumberOfWebhooks(album, em);
            }

            for(Webhook webhook:webhookList) {
                final WebhookResponse webhookResponse = new WebhookResponse(webhook);
                webhookResponseList.add(webhookResponse);
                int maxHistory = 5;
                for(WebhookTrigger webhookTrigger:webhook.getWebhookTriggers()) {
                    webhookResponse.addTrigger(webhookTrigger);
                    maxHistory--;
                    if(maxHistory == 0) {
                        break;
                    }
                }
            }

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return new PairListXTotalCount<>(numberOfWebhook, webhookResponseList);
    }

    public static void triggerNewSeriesWebhook(ServletContext context, String webhookID, String albumId, String studyInstanceUID, List<String> seriesInstanceUID, User callingUser)
            throws WebhookNotFoundException, AlbumNotFoundException, UserNotMemberException, SeriesNotFoundException, StudyNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final Album album = getAlbum(albumId, em);
            final Webhook webhook = WebhookQueries.getWebhook(webhookID, album, em);
            final AlbumUser albumCallingUser = getAlbumUser(album, callingUser, em);
            final Study study = getStudy(studyInstanceUID, em);

            final NewSeriesWebhook.Builder newSeriesWebhookBuilder = NewSeriesWebhook.builder()
                    .isSent()
                    .setKheopsInstance(context.getInitParameter(HOST_ROOT_PARAMETER))
                    .isManualTrigger()
                    .setDestination(albumId)
                    .setSource(albumCallingUser)
                    .setStudy(study);
            final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), true, WebhookType.NEW_SERIES, webhook);
            em.persist(webhookTrigger);

            for (String seriesUID : seriesInstanceUID) {
                final Series series = getSeries(studyInstanceUID, seriesUID, em);
                if (album.containsSeries(series, em)) {
                    webhookTrigger.addSeries(series);
                    newSeriesWebhookBuilder.addSeries(series);
                } else {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(SERIES_NOT_FOUND)
                            .detail("The series does not exist or she is not in the album")
                            .build();
                    throw new SeriesNotFoundException(errorResponse);
                }
            }
            final Mutation mutation = Events.albumPostMutation(callingUser, album, TRIGGER_WEBHOOK);
            em.persist(mutation);
            tx.commit();
            new WebhookAsyncRequest(webhook, newSeriesWebhookBuilder.build(), webhookTrigger).firstRequest();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
    public static void triggerNewUserWebhook(ServletContext context, String webhookID, String albumId, String user, User callingUser)
            throws WebhookNotFoundException, AlbumNotFoundException, UserNotMemberException, UserNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final Album album = getAlbum(albumId, em);
            final Webhook webhook = WebhookQueries.getWebhook(webhookID, album, em);
            final AlbumUser albumCallingUser = getAlbumUser(album, callingUser, em);

            final User targetUser = getUser(user, em);
            final AlbumUser albumTargetUser = getAlbumUser(album, targetUser, em);
            final NewUserWebhook newUserWebhook;
            final WebhookTrigger webhookTrigger;

            if (isMemberOfAlbum(targetUser, album, em)) {
                newUserWebhook = new NewUserWebhook(albumId, albumCallingUser, albumTargetUser, context.getInitParameter(HOST_ROOT_PARAMETER),true);
                webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), true, WebhookType.NEW_USER, webhook);
                webhookTrigger.setUser(targetUser);
                em.persist(webhookTrigger);
            } else {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("User not a member")
                        .detail("The trigged 'user' is not a member of the album")
                        .build();
                throw new UserNotMemberException(errorResponse);
            }

            final Mutation mutation = Events.albumPostMutation(callingUser, album, TRIGGER_WEBHOOK);
            em.persist(mutation);
            tx.commit();
            new WebhookAsyncRequest(webhook, newUserWebhook, webhookTrigger).firstRequest();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }


    public static boolean webhookExist(String webhookId, EntityManager em) {
        try {
            findWebhookById(webhookId, em);
        } catch (WebhookNotFoundException e) {
            return false;
        }
        return true;
    }

    public static boolean webhookTriggerExist(String webhookTriggerId, EntityManager em) {
        try {
            findWebhookTriggerById(webhookTriggerId, em);
        } catch (WebhookNotFoundException e) {
            return false;
        }
        return true;
    }

    public static void validName(String name) throws BadQueryParametersException {
        if(name.length() > Consts.DB_COLUMN_SIZE.WEBHOOK_NAME) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'name' is too long max expected: " + Consts.DB_COLUMN_SIZE.WEBHOOK_NAME + " characters but got :" + name.length())
                    .build();
            throw new  BadQueryParametersException(errorResponse);
        } else if (name.length() == 0) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'name' is too short min expected: 1 character but got : 0")
                    .build();
            throw new  BadQueryParametersException(errorResponse);
        }
    }

    public static void validSecret(String secret) throws BadQueryParametersException {
        if (secret != null && secret.length() > Consts.DB_COLUMN_SIZE.WEBHOOK_SECRET) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'secret' is too long max expected: " + Consts.DB_COLUMN_SIZE.WEBHOOK_SECRET + " characters but got :" + secret.length())
                    .build();
            throw new  BadQueryParametersException(errorResponse);
        } else if (secret != null && secret.length() == 0) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'secret' is too short min expected: 1 character but got : 0")
                    .build();
            throw new  BadQueryParametersException(errorResponse);
        }
    }

    public static void validUrl(String url) throws BadQueryParametersException {
        if(url.length() > Consts.DB_COLUMN_SIZE.WEBHOOK_URL) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'url' is too long max expected: " + Consts.DB_COLUMN_SIZE.WEBHOOK_URL + " characters but got :" + url.length())
                    .build();
            throw new  BadQueryParametersException(errorResponse);
        }
        try {
            final String protocol = new URL(url).getProtocol();
            if (!VALID_SCHEMES_WEBHOOK_URL.contains(protocol)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_FORM_PARAMETER)
                        .detail("'url' not valid, the scheme must be 'http' or 'https'")
                        .build();
                throw new  BadQueryParametersException(errorResponse);
            }
        } catch (MalformedURLException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("'url' not valid")
                    .build();
            throw new  BadQueryParametersException(errorResponse);
        }
    }



    }
