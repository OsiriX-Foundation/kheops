package online.kheops.auth_server.webhook;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.event.Events.MutationType.CREATE_WEBHOOK;
import static online.kheops.auth_server.event.Events.MutationType.DELETE_WEBHOOK;
import static online.kheops.auth_server.util.KheopsLogBuilder.ActionType.*;
import static online.kheops.auth_server.webhook.WebhookQueries.findWebhookById;
import static online.kheops.auth_server.webhook.WebhookQueries.getWebhook;

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
            final Webhook webhook = getWebhook(webhookID, album, em);

            for (WebhookHistory webhookHistory:webhook.getWebhookHistory()) {
                em.remove(webhookHistory);
            }
            em.remove(webhook);

            final Mutation mutation = Events.albumPostMutation(callingUser, album, DELETE_WEBHOOK);
            em.persist(mutation);

            tx.commit();

            kheopsLogBuilder.action(REMOVE_WEBHOOK)
                    .album(albumId)
                    .log();

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
