package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.entity.Webhook;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

public class WebhookQueries {

    private WebhookQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static Webhook findWebhookById(String webhookID, EntityManager em)
            throws WebhookNotFoundException {

        try {
            return em.createQuery("SELECT w from Webhook w where :webhookId = w.id", Webhook.class)
                    .setParameter("webhookId", webhookID)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new WebhookNotFoundException();
        }
    }

    public static Webhook getWebhook(String webhookID, Album album, EntityManager em)
            throws WebhookNotFoundException {
        try {
            return em.createQuery("SELECT w from Webhook w join w.album a where :webhookId = w.id and a = :album", Webhook.class)
                    .setParameter("webhookId", webhookID)
                    .setParameter("album", album)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new WebhookNotFoundException();
        }

    }

}
