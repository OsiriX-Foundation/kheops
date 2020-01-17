package online.kheops.auth_server.webhook;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContext;

import java.util.ArrayList;
import java.util.List;

import static online.kheops.auth_server.album.Albums.*;
import static online.kheops.auth_server.event.Events.MutationType.*;
import static online.kheops.auth_server.event.Events.MutationType.EDIT_WEBHOOK;
import static online.kheops.auth_server.event.Events.MutationType.TRIGGER_WEBHOOK;
import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;
import static online.kheops.auth_server.util.ErrorResponse.Message.SERIES_NOT_FOUND;
import static online.kheops.auth_server.util.KheopsLogBuilder.ActionType.*;
import static online.kheops.auth_server.webhook.WebhookQueries.findWebhookById;

public class Webhooks {

    private Webhooks() {
        throw new IllegalStateException("Utility class");
    }

    public static WebhookResponse createWebhook(String url, String albumId, User callingUser, String name, String secret, boolean newSeries, boolean newUser, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final WebhookResponse webhookResponse;

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final Album album = getAlbum(albumId, em);
            final Webhook newWebhook = new Webhook(name, url, album, callingUser, secret, newSeries, newUser);

            em.persist(newWebhook);

            final Mutation mutation = Events.albumPostMutation(callingUser, album, CREATE_WEBHOOK);
            em.persist(mutation);

            tx.commit();

            webhookResponse = new WebhookResponse(newWebhook);

            kheopsLogBuilder.action(NEW_WEBHOOK)
                    .album(albumId)
                    .webhookID(newWebhook.getId())
                    .log();

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return webhookResponse;
    }

    public static WebhookResponse editWebhook(String webhookId, String url, String albumId, User callingUser, String name, String secret, Boolean newSeries, Boolean newUser, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, WebhookNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final WebhookResponse webhookResponse;

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final Album album = getAlbum(albumId, em);
            final Webhook webhook = WebhookQueries.getWebhook(webhookId, album, em);

            if (url != null) {
                webhook.setUrl(url);
            }
            if (name != null) {
                webhook.setName(name);
            }
            if (secret != null) {
                webhook.setSecret(secret);
            }
            if (newSeries != null) {
                webhook.setNewSeries(newSeries);
            }
            if (newUser != null) {
                webhook.setNewUser(newUser);
            }

            final Mutation mutation = Events.albumPostMutation(callingUser, album, EDIT_WEBHOOK);
            em.persist(mutation);

            tx.commit();

            webhookResponse = new WebhookResponse(webhook);

            kheopsLogBuilder.action(KheopsLogBuilder.ActionType.EDIT_WEBHOOK)
                    .album(albumId)
                    .webhookID(webhook.getId())
                    .log();

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

            for (WebhookHistory webhookHistory:webhook.getWebhookHistory()) {
                em.remove(webhookHistory);
            }
            em.remove(webhook);

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


    public static WebhookResponse getWebhook(String webhookID, String albumId, Integer historyLimit, Integer historyOffset, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, WebhookNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final WebhookResponse webhookResponse;

        try {
            tx.begin();

            final Album album = getAlbum(albumId, em);
            final Webhook webhook = WebhookQueries.getWebhook(webhookID, album, em);

            tx.commit();

            kheopsLogBuilder.action(GET_WEBHOOK)
                    .album(albumId)
                    .webhookID(webhookID)
                    .log();

            webhookResponse = new WebhookResponse(webhook);

            int maxHistory = 5;
            for(WebhookHistory webhookHistory:webhook.getWebhookHistory()) {
                webhookResponse.addHistory(webhookHistory);
                maxHistory--;
                if(maxHistory == 0) {
                    break;
                }
            }

            for(WebhookHistory webhookHistory:webhook.getWebhookHistory()) {
                if (historyOffset > 0) {
                    historyOffset--;
                } else {
                    webhookResponse.addFullHistory(webhookHistory);
                    historyLimit--;
                    if (historyLimit == 0) {
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
        return webhookResponse;
    }
    public static PairListXTotalCount<WebhookResponse> getWebhooks(String albumId, Integer limit, Integer offset)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final List<WebhookResponse> webhookResponseList = new ArrayList<>();
        final Long numberOfWebhook;

        try {
            tx.begin();

            final Album album = getAlbum(albumId, em);
            final List<Webhook> webhookList = WebhookQueries.getWebhooks(album, limit, offset, em);
            numberOfWebhook = WebhookQueries.getNumberOfWebhooks(album, em);

            for(Webhook webhook:webhookList) {
                final WebhookResponse webhookResponse = new WebhookResponse(webhook);
                webhookResponseList.add(webhookResponse);
                int maxHistory = 5;
                for(WebhookHistory webhookHistory:webhook.getWebhookHistory()) {
                    webhookResponse.addHistory(webhookHistory);
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

    public static void triggerWebhook(ServletContext context, String webhookID, String albumId, String user, String studyInstanceUID, String seriesInstanceUID, User callingUser)
            throws WebhookNotFoundException, AlbumNotFoundException, UserNotMemberException, SeriesNotFoundException, UserNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final Album album = getAlbum(albumId, em);
            final Webhook webhook = WebhookQueries.getWebhook(webhookID, album, em);
            final AlbumUser albumCallingUser = getAlbumUser(album, callingUser, em);

            if (user != null) {
                final User targetUser = getOrCreateUser(user);
                final AlbumUser albumTargetUser = getAlbumUser(album, targetUser, em);

                if (isMemberOfAlbum(targetUser, album, em)) {
                    final NewUserWebhook newUserWebhook = new NewUserWebhook(albumId, albumCallingUser, albumTargetUser, context.getInitParameter(HOST_ROOT_PARAMETER),true);
                    new WebhookAsyncRequest(webhook, newUserWebhook, true);
                } else {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message("User not a member")
                            .detail("The trigged user is not a member of the album")
                            .build();
                    throw new UserNotMemberException(errorResponse);
                }
            } else if (seriesInstanceUID != null) {
                Series series = getSeries(studyInstanceUID, seriesInstanceUID, em);
                if(album.containsSeries(series, em)) {
                    final NewSeriesWebhook newSeriesWebhook = new NewSeriesWebhook(albumId, albumCallingUser, series, context.getInitParameter(HOST_ROOT_PARAMETER),true);
                    new WebhookAsyncRequest(webhook, newSeriesWebhook, true);
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

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }


    public static boolean webhookExist(String webhookId) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            findWebhookById(webhookId, em);
        } catch (WebhookNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
        return true;
    }

}
