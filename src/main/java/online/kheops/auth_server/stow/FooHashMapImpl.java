package online.kheops.auth_server.stow;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.KheopsInstance;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.MutationType;
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
import static online.kheops.auth_server.event.Events.albumPostStudyMutation;

public class FooHashMapImpl implements FooHashMap {

    @Inject
    KheopsInstance kheopsInstance;

    private static final Logger LOG = Logger.getLogger(FooHashMapImpl.class.getName());

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final int TIME_TO_LIVE = 10;//seconds

    private static Level0_StudyLevel level0StudyLevel = new Level0_StudyLevel();

    public FooHashMapImpl() { /*empty*/ }

    public void addHashMapData(Study study, Series series, Album destination, boolean isInbox, boolean isNewStudy, boolean isNewSeries, Integer numberOfInstances, Source source, boolean isNewInDestination, boolean isSend) {
        scheduler.schedule(() -> addData(study, series, isNewStudy, isNewSeries, destination, isInbox, numberOfInstances, source, isNewInDestination, isSend), 0, TimeUnit.SECONDS);
    }


    private void addData(Study study, Series series, boolean isNewStudy, boolean isNewSeries, Album destination, boolean isInbox, Integer numberOfInstances, Source source, boolean isNewInDestination, boolean isSend) {
        if (level0StudyLevel.containsStudy(study)) {
            level0StudyLevel.get(study).cancelScheduledFuture();
        }
        level0StudyLevel.put(scheduler.schedule(() -> callWebhook(study), TIME_TO_LIVE, TimeUnit.SECONDS), study, series, isNewStudy, isNewSeries, numberOfInstances, source, destination, isInbox, isNewInDestination, isSend);
    }

    private void callWebhook(Study studyIn) {
        String log = "callWebhook:";
        log += level0StudyLevel.toString(studyIn);
        LOG.info(log);

        final Level1_SourceLevel level1SourceLevel = level0StudyLevel.get(studyIn);

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final Study study = em.merge(studyIn);

            final List<WebhookAsyncRequest> webhookAsyncRequests = new ArrayList<>();

            for (Map.Entry<Source, Level2_DestinationLevel> entry : level1SourceLevel.getSources().entrySet()) {
                final Source source = entry.getKey();
                final Level2_DestinationLevel level2DestinationLevel = entry.getValue();

                if (level1SourceLevel.isNewStudy()) {
                    for (Map.Entry<Album, Level3_SeriesLevel> entry1 : level2DestinationLevel.getDestinations().entrySet()) {
                        final Album album = em.merge(entry1.getKey());
                        final Level3_SeriesLevel level3SeriesLevel = entry1.getValue();

                        if (!level3SeriesLevel.isInbox()) {

                            if (!album.getWebhooksNewSeriesEnabled().isEmpty()) {
                                //if destination contain webhooks

                                final NewSeriesWebhook.Builder newSeriesWebhookBuilder = NewSeriesWebhook.builder()
                                        .setDestination(album.getId())
                                        .isAutomatedTrigger()
                                        .setStudy(study)
                                        .setSource(source)
                                        .setKheopsInstance(kheopsInstance.get());
                                if(level2DestinationLevel.isSend()) {
                                    newSeriesWebhookBuilder.isSent();
                                    for (Map.Entry<Series, Level4_InstancesLevel> entry2 : level3SeriesLevel.getSeries().entrySet()) {
                                        final Series series = em.merge(entry2.getKey());
                                        em.refresh(series);
                                        final Level4_InstancesLevel level4InstancesLevel = entry2.getValue();
                                        newSeriesWebhookBuilder.addSeries(series, null);
                                    }
                                } else {
                                    newSeriesWebhookBuilder.isUpload();
                                    for (Map.Entry<Series, Level4_InstancesLevel> entry2 : level3SeriesLevel.getSeries().entrySet()) {
                                        final Series series = em.merge(entry2.getKey());
                                        em.refresh(series);
                                        final Level4_InstancesLevel level4InstancesLevel = entry2.getValue();
                                        newSeriesWebhookBuilder.addSeries(series, series.getNumberOfSeriesRelatedInstances() - level4InstancesLevel.getNumberOfInstances());
                                    }
                                }

                                for (Map.Entry<Series, Level4_InstancesLevel> entry2 : level3SeriesLevel.getSeries().entrySet()) {
                                    final Series series = em.merge(entry2.getKey());
                                    em.refresh(series);
                                    final Level4_InstancesLevel level4InstancesLevel = entry2.getValue();
                                    newSeriesWebhookBuilder.addSeries(series, series.getNumberOfSeriesRelatedInstances() - level4InstancesLevel.getNumberOfInstances());
                                }

                                final NewSeriesWebhook newSeriesWebhook = newSeriesWebhookBuilder.build();
                                for (Webhook webhook : album.getWebhooksNewSeriesEnabled()) {
                                    final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), false, WebhookType.NEW_SERIES, webhook);
                                    newSeriesWebhookBuilder.getSeries().forEach(webhookTrigger::addSeries);
                                    em.persist(webhookTrigger);
                                    webhookAsyncRequests.add(new WebhookAsyncRequest(webhook, newSeriesWebhook, webhookTrigger));
                                }
                            }

                            source.getCapabilityToken().ifPresentOrElse(
                                    capability ->
                                            em.persist(albumPostStudyMutation(em.merge(capability), album, MutationType.IMPORT_STUDY, study, new ArrayList<>(level3SeriesLevel.getSeries().keySet()))),
                                    () ->
                                            em.persist(albumPostStudyMutation(em.merge(source.getUser()), album, MutationType.IMPORT_STUDY, study, new ArrayList<>(level3SeriesLevel.getSeries().keySet())))
                            );
                        }
                    }
                } else { //ce n'est pas une nouvelle study
                    if(level2DestinationLevel.isSend()) {   // c'est un send (on applique le webhook uniquement sur les destination)
                        for (Map.Entry<Album, Level3_SeriesLevel> entry1 : level2DestinationLevel.getDestinations().entrySet()) {
                            final Album album = em.merge(entry1.getKey());
                            final Level3_SeriesLevel level3SeriesLevel = entry1.getValue();

                            final NewSeriesWebhook.Builder newSeriesWebhookBuilder = NewSeriesWebhook.builder()
                                .setKheopsInstance(kheopsInstance.get())
                                .setDestination(album.getId())
                                .isAutomatedTrigger()
                                .setStudy(study)
                                .setSource(source)
                                .isSent();

                            for (Map.Entry<Series, Level4_InstancesLevel> entry2 : level3SeriesLevel.getSeries().entrySet()) {
                                final Series series = em.merge(entry2.getKey());
                                em.refresh(series);
                                final Level4_InstancesLevel level4InstancesLevel = entry2.getValue();
                                newSeriesWebhookBuilder.addSeries(series, null);
                            }

                            if (!newSeriesWebhookBuilder.getSeries().isEmpty()) {
                                source.getCapabilityToken().ifPresentOrElse(
                                        capability ->
                                                em.persist(albumPostStudyMutation(em.merge(capability), album, MutationType.IMPORT_STUDY, study, new ArrayList<>(newSeriesWebhookBuilder.getSeries()))),
                                        () ->
                                                em.persist(albumPostStudyMutation(em.merge(source.getUser()), album, MutationType.IMPORT_STUDY, study, new ArrayList<>(newSeriesWebhookBuilder.getSeries())))
                                );
                            }
                            final NewSeriesWebhook newSeriesWebhook = newSeriesWebhookBuilder.build();
                            for (Webhook webhook : album.getWebhooksNewSeriesEnabled()) {
                                final WebhookTrigger webhookTrigger = new WebhookTrigger(new WebhookRequestId(em).getRequestId(), false, WebhookType.NEW_SERIES, webhook);
                                newSeriesWebhookBuilder.getSeries().forEach(webhookTrigger::addSeries);
                                em.persist(webhookTrigger);
                                webhookAsyncRequests.add(new WebhookAsyncRequest(webhook, newSeriesWebhook, webhookTrigger));
                            }
                        }

                    } else { // c'est un upload si'il y a des nouvelles images partout sinon uniquement destination
                        for (Album album : findAlbumsWithEnabledNewSeriesWebhooks(study.getStudyInstanceUID(), em)) {

                            final NewSeriesWebhook.Builder newSeriesWebhookBuilder = NewSeriesWebhook.builder()
                                    .setKheopsInstance(kheopsInstance.get())
                                    .setDestination(album.getId())
                                    .isAutomatedTrigger()
                                    .setStudy(study)
                                    .setSource(source)
                                    .isUpload();

                            if (level2DestinationLevel.getDestinations().containsKey(album)) {
                                if (!level2DestinationLevel.getDestination(album).isInbox()) {
                                    for (Map.Entry<Series, Level4_InstancesLevel> entry2 : level2DestinationLevel.getDestination(album).getSeries().entrySet()) {
                                        final Series series = em.merge(entry2.getKey());
                                        em.refresh(series);
                                        final Level4_InstancesLevel level4InstancesLevel = entry2.getValue();

                                        newSeriesWebhookBuilder.addSeries(series, series.getNumberOfSeriesRelatedInstances() - level4InstancesLevel.getNumberOfInstances());

                                    }
                                    source.getCapabilityToken().ifPresentOrElse(
                                            capability ->
                                                    em.persist(albumPostStudyMutation(em.merge(capability), album, MutationType.IMPORT_STUDY, study, new ArrayList<>(newSeriesWebhookBuilder.getSeries()))),
                                            () ->
                                                    em.persist(albumPostStudyMutation(em.merge(source.getUser()), album, MutationType.IMPORT_STUDY, study, new ArrayList<>(newSeriesWebhookBuilder.getSeries())))
                                    );
                                }
                            } else {
                                //pour un album non destination
                                //si la series à des nouvelles instance => webhook
                                for (Map.Entry<Series, Integer> seriesUIDNumberOfStudy : level2DestinationLevel.getAllSeries().entrySet()) {
                                    final Series series = em.merge(seriesUIDNumberOfStudy.getKey());
                                    em.refresh(series);
                                    if(series.getNumberOfSeriesRelatedInstances() - seriesUIDNumberOfStudy.getValue() > 0) {
                                        if (album.containsSeries(series, em)) {
                                            newSeriesWebhookBuilder.addSeries(series, series.getNumberOfSeriesRelatedInstances() - seriesUIDNumberOfStudy.getValue());
                                        }
                                    }
                                }
                            }

                            if(!newSeriesWebhookBuilder.getSeries().isEmpty()) {
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
                }
            }

            /*
            new study in destination (with list of series)=>si fooHashMap NE contient PAS la study
            new series in destination =>si fooHashMap NE contient PAS la séries
            ?? new instances ?? (update series) or (add instances in series)
             */


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
            level0StudyLevel.remove(studyIn);
        }
    }

}