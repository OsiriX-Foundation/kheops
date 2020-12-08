package online.kheops.auth_server.stow;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.KheopsInstance;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.webhook.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.album.AlbumQueries.findAlbumsWithEnabledNewSeriesWebhooks;
import static online.kheops.auth_server.album.Albums.getAlbumUser;

public class FooHashMapImpl implements FooHashMap {

    @Inject
    KheopsInstance kheopsInstance;

    private static final Logger LOG = Logger.getLogger(FooHashMapImpl.class.getName());

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final int TIME_TO_LIVE = 10;//seconds

    private static Level0 level0 = new Level0();

    public FooHashMapImpl() { /*empty*/ }

    public void addHashMapData(Study study, Series series, Album destination, boolean isInbox, Integer numberOfNewInstances, Source source, boolean isNewInDestination, boolean isSend) {
        scheduler.schedule(() -> addData(study, series, destination, isInbox, numberOfNewInstances, source, isNewInDestination, isSend), 0, TimeUnit.SECONDS);
    }

    private void addData(Study study, Series series, Album destination, boolean isInbox, Integer numberOfNewInstances, Source source, boolean isNewInDestination, boolean isSend) {
        level0.put(scheduler.schedule(() -> callWebhook(study, source), TIME_TO_LIVE, TimeUnit.SECONDS), study, series, numberOfNewInstances, source, destination, isInbox, isNewInDestination, isSend);
    }

    private void callWebhook(Study studyIn, Source source) {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final List<WebhookAsyncRequest> webhookAsyncRequests = new ArrayList<>();
            Study study = em.merge(studyIn);
            em.refresh(study);
            final Level0Key level0Key = new Level0Key(study, source);
            final Level0Value level0Value = level0.get(level0Key);
            final Set<Level1Key> level1Keys = level0Value.getKeys();

            final HashMap<Series, Integer> newUploadedSeries = getNewUploadedSeries(level0Value, level1Keys, em);

            for (Level1Key destination : level1Keys) {
                final Album album = em.merge(destination.getAlbum());
                final Level1Value level1Value = level0Value.get(destination);
                Boolean isAdmin = null;
                try {
                    final AlbumUser albumUser = getAlbumUser(album, source.getUser(), em);
                    isAdmin = albumUser.isAdmin();
                } catch (UserNotMemberException e){ /*empty*/ }

                final NewSeriesWebhook.Builder newSeriesWebhookBuilder = NewSeriesWebhook.builder()
                        .setKheopsInstance(kheopsInstance.get())
                        .isAutomatedTrigger()
                        .setSource(source)
                        .isAdmin(isAdmin)
                        .setStudy(study)
                        .setDestination(album.getId());

                for (Series unmergedSeries : level1Value.getSeries()) {
                    Series series = em.merge(unmergedSeries);
                    em.refresh(series);

                    if(level1Value.get(series).isSendUpload()) {
                        newSeriesWebhookBuilder.isSent();
                        newSeriesWebhookBuilder.addSeries(series);
                    } else {
                        newSeriesWebhookBuilder.isUpload();
                        if (level1Value.get(series).isNewInDestination()) {
                            newSeriesWebhookBuilder.addSeries(series, series.getNumberOfSeriesRelatedInstances());
                        } else {
                            if (newUploadedSeries.get(series) != 0) {
                                newSeriesWebhookBuilder.addSeries(series, newUploadedSeries.get(series));
                            }
                        }
                    }
                }

                for (Map.Entry<Series, Integer> s: newUploadedSeries.entrySet()) {
                    if (album.containsSeries(s.getKey(), em) && !newSeriesWebhookBuilder.getSeries().contains(s.getKey())) {
                        newSeriesWebhookBuilder.addSeries(s.getKey(), s.getValue());
                    }
                }

                if (!newSeriesWebhookBuilder.getSeries().isEmpty()) {
                    final NewSeriesWebhook newSeriesWebhook = newSeriesWebhookBuilder.build();
                    for (Webhook webhook : album.getWebhooksNewSeriesEnabled()) {
                        final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), false, WebhookType.NEW_SERIES, webhook);
                        newSeriesWebhookBuilder.getSeries().forEach(webhookTrigger::addSeries);
                        em.persist(webhookTrigger);
                        webhookAsyncRequests.add(new WebhookAsyncRequest(webhook, newSeriesWebhook, webhookTrigger));
                    }
                }
            }

            //for all others albums that contains the study
            for (Album album : findAlbumsWithEnabledNewSeriesWebhooks(study.getStudyInstanceUID(), em)) {
                if (!level1Keys.contains(new Level1Key(album))) {
                    final NewSeriesWebhook.Builder newSeriesWebhookBuilder = NewSeriesWebhook.builder()
                        .setKheopsInstance(kheopsInstance.get())
                        .isAutomatedTrigger()
                        .setSource(source)
                        .setStudy(study)
                        .setDestination(album.getId());
                    for (Map.Entry<Series, Integer> s : newUploadedSeries.entrySet()) {
                        if (album.containsSeries(s.getKey(), em)) {
                            newSeriesWebhookBuilder.addSeries(s.getKey(), s.getValue());
                        }
                    }
                    if (!newSeriesWebhookBuilder.getSeries().isEmpty()) {
                        final NewSeriesWebhook newSeriesWebhook = newSeriesWebhookBuilder.build();
                        for (Webhook webhook : album.getWebhooksNewSeriesEnabled()) {
                            final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), false, WebhookType.NEW_SERIES, webhook);
                            newSeriesWebhookBuilder.getSeries().forEach(webhookTrigger::addSeries);
                            em.persist(webhookTrigger);
                            webhookAsyncRequests.add(new WebhookAsyncRequest(webhook, newSeriesWebhook, webhookTrigger));
                        }
                    }
                }
            }

            tx.commit();
            for (WebhookAsyncRequest webhookAsyncRequest : webhookAsyncRequests) {
                webhookAsyncRequest.firstRequest();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING,"", e);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
            level0.remove(studyIn, source);
        }
    }

    private HashMap<Series, Integer> getNewUploadedSeries(Level0Value level0Value, Set<Level1Key> level1Keys, EntityManager em) {
        final HashMap<Series, Integer> newUploadedSeries = new HashMap<>();
        for (Level1Key destination : level1Keys) {
            final Level1Value level1Value = level0Value.get(destination);
            for (Series unmergedSeries : level1Value.getSeries()) {
                Series series = em.merge(unmergedSeries);
                em.refresh(series);
                if(level0Value.get(destination).get(series).getNumberOfNewInstances() != 0) {
                    newUploadedSeries.compute(series, (s, nb) -> (nb == null)
                            ? level0Value.get(destination).get(series).getNumberOfNewInstances()
                            : nb + level0Value.get(destination).get(series).getNumberOfNewInstances() );

                }
            }
        }
        return newUploadedSeries;
    }
}