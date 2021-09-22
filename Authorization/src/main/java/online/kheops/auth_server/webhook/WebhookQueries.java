package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
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

    public static Long getNumberOfWebhookTrigger(Webhook webhook, EntityManager em) {
        return em.createNamedQuery("WebhookTrigger.countAllByWebhookPk", Long.class)
                .setParameter(WEBHOOK_PK, webhook.getPk())
                .getSingleResult();
    }

    public static List<WebhookTrigger> getWebhookTriggers(Webhook webhook, int limit, int offset, EntityManager em) {
        return em.createNamedQuery("WebhookTrigger.GetAllByWebhookPk", WebhookTrigger.class)
                .setParameter(WEBHOOK_PK, webhook.getPk())
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }


    public static List<WebhookTrigger> getWebhookTriggersBySeriesList(final List<Series> seriesList, final EntityManager em) {
        List<WebhookTrigger> webhookTriggerList = new ArrayList<>();
        for (Series series : seriesList) {
            webhookTriggerList.addAll(em.createNamedQuery("WebhookTriggerSeries.findAllBySeries", WebhookTrigger.class)
                    .setParameter(SERIES, series)
                    .getResultList());
        }

        return webhookTriggerList;
    }

    public static List<WebhookTrigger> getWebhookTriggersBySeries(final Series series, final EntityManager em) {
        return em.createNamedQuery("WebhookTriggerSeries.findAllBySeries", WebhookTrigger.class)
                .setParameter(SERIES, series)
                .getResultList();
    }

    public static Long getNumberOfWebhooks(Album album, EntityManager em) {
        return em.createNamedQuery("Webhook.countByAlbum", Long.class)
                .setParameter(ALBUM, album)
                .getSingleResult();
    }

    public static Long getNumberOfWebhooks(Album album, String url, EntityManager em) {
        return em.createNamedQuery("Webhook.countByAlbumAndUrl", Long.class)
                .setParameter(ALBUM, album)
                .setParameter(WEBHOOK_URL, url)
                .getSingleResult();
    }

    public static void deleteAllWebhookTriggerSeriesBySeriesList(final List<Series> seriesToDelete, final EntityManager em) {
        for (Series series : seriesToDelete) {
            em.createNamedQuery("WebhookTriggerSeries.deleteAllWebhookTriggerSeriesBySeries")
                    .setParameter(SERIES, series)
                    .executeUpdate();
        }
    }

    public static void deleteAllWebhookTriggerSeriesBySeriesList(final Series seriesToDelete, final EntityManager em) {
        em.createNamedQuery("WebhookTriggerSeries.deleteAllWebhookTriggerSeriesBySeries")
                .setParameter(SERIES, seriesToDelete)
                .executeUpdate();
    }

    public static void deleteAllWebHookAttemptsByWebhookTriggers(final List<WebhookTrigger> webhookTriggers, final EntityManager em) {
        for (WebhookTrigger webhookTrigger : webhookTriggers) {
            em.createNamedQuery("WebhookAttempt.deleteAllWebhookAttemptsByWebhookTrigger")
                    .setParameter(WEBHOOK_TRIGGER_ID, webhookTrigger)
                    .executeUpdate();
        }
    }

    public static void deleteAllWebHookTriggers(final List<WebhookTrigger> webhookTriggers, final EntityManager em) {
        for (WebhookTrigger webhookTrigger : webhookTriggers) {
            em.remove(webhookTrigger);
        }
    }
}