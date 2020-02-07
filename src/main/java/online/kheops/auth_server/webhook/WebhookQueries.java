package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Webhook;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

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

    public static List<Webhook> getWebhooks(Album album, Integer limit, Integer offset, EntityManager em) {
            return em.createQuery("SELECT w from Webhook w join w.album a where a = :album order by w.creationTime desc", Webhook.class)
                    .setParameter("album", album)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
    }

    public static List<Webhook> getWebhooks(Album album, Integer limit, String url, Integer offset, EntityManager em) {
            return em.createQuery("SELECT w from Webhook w join w.album a where a = :album and w.url = :url order by w.creationTime desc", Webhook.class)
                    .setParameter("album", album)
                    .setParameter("url", url)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
    }


    public static Long getNumberOfWebhooks( Album album, EntityManager em) {
        return em.createQuery("SELECT count(w) from Webhook w join w.album a where a = :album", Long.class)
                .setParameter("album", album)
                .getSingleResult();
    }

}
