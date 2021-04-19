package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Webhook;
import online.kheops.auth_server.entity.WebhookTrigger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

import static online.kheops.auth_server.util.JPANamedQueryConstants.*;

public class WebhookQueries {

    private WebhookQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static Webhook findWebhookById(String webhookID, EntityManager em)
            throws WebhookNotFoundException {

        try {
            return em.createNamedQuery("Webhook.findById", Webhook.class)
                    .setParameter(WEBHOOK_ID, webhookID)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new WebhookNotFoundException();
        }
    }

    public static WebhookTrigger findWebhookTriggerById(String webhookTriggerID, EntityManager em)
            throws WebhookNotFoundException {

        try {
            return em.createNamedQuery("WebhookTrigger.findById", WebhookTrigger.class)
                    .setParameter(WEBHOOK_TRIGGER_ID, webhookTriggerID)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new WebhookNotFoundException();
        }
    }

    public static Webhook getWebhook(String webhookID, Album album, EntityManager em)
            throws WebhookNotFoundException {
        try {
            return em.createNamedQuery("Webhook.findByIdAndAlbum", Webhook.class)
                    .setParameter(WEBHOOK_ID, webhookID)
                    .setParameter(ALBUM, album)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new WebhookNotFoundException();
        }
    }

    public static List<Webhook> getWebhooks(Album album, Integer limit, Integer offset, EntityManager em) {
            return em.createNamedQuery("Webhook.findAllByAlbum", Webhook.class)
                    .setParameter(ALBUM, album)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
    }

    public static List<Webhook> getWebhooks(Album album, Integer limit, String url, Integer offset, EntityManager em) {
            return em.createNamedQuery("Webhook.findAllByAlbumAndUrl", Webhook.class)
                    .setParameter(ALBUM, album)
                    .setParameter(WEBHOOK_URL, url)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
    }

    public static Long getNumberOfWebhooks( Album album, EntityManager em) {
        return em.createNamedQuery("Webhook.countByAlbum", Long.class)
                .setParameter(ALBUM, album)
                .getSingleResult();
    }
    public static Long getNumberOfWebhooks( Album album, String url, EntityManager em) {
        return em.createNamedQuery("Webhook.countByAlbumAndUrl", Long.class)
                .setParameter(ALBUM, album)
                .setParameter(WEBHOOK_URL, url)
                .getSingleResult();
    }
}
