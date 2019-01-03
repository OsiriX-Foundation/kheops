package online.kheops.auth_server.fetch;

import online.kheops.auth_server.EntityManagerListener;

import javax.persistence.*;
import java.net.URI;
import java.util.List;


import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
public class FetchTask implements Runnable {

    private static final Logger LOG = Logger.getLogger(FetchTask.class.getName());

    private final URI dicomWebURI;

    public FetchTask(URI dicomWebURI) {
        this.dicomWebURI = dicomWebURI;
    }

    private static class UIDPair {
        private String studyInstanceUID;
        private String seriesInstanceUID;

        public UIDPair(String studyInstanceUID, String seriesInstanceUID) {
            this.studyInstanceUID = studyInstanceUID;
            this.seriesInstanceUID = seriesInstanceUID;
        }

        String getStudyInstanceUID() {
            return studyInstanceUID;
        }

        String getSeriesInstanceUID() {
            return seriesInstanceUID;
        }
    }

    @Override
    public void run() {
        LOG.log(Level.FINE, "Starting Fetch Task");

        try {
            fetchUnpopulatedSeries(unpopulatedSeriesUIDs());
            fetchUnpopulatedStudies(unpopulatedStudyUIDs());
        } catch (IllegalStateException e) {
            LOG.log(Level.FINE,"IllegalStateException while fetching, probably because the server is not up yet", e);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"An error occurred while fetching", e);
        }

        LOG.log(Level.FINE, "Finished Fetch Task");
    }

    private List<UIDPair> unpopulatedSeriesUIDs() {
        EntityManager em = EntityManagerListener.createEntityManager();
        List<UIDPair> unpopulatedSeriesUIDs;

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            //noinspection JpaQlInspection
            TypedQuery<UIDPair> query = em.createQuery("select new online.kheops.auth_server.fetch.FetchTask$UIDPair(s.study.studyInstanceUID, s.seriesInstanceUID) from Series s where s.populated = false", UIDPair.class);
            query.setMaxResults(30);
            unpopulatedSeriesUIDs = query.getResultList();

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return unpopulatedSeriesUIDs;
    }

    private List<String> unpopulatedStudyUIDs() {
        EntityManager em = EntityManagerListener.createEntityManager();
        List<String> unpopulatedStudyUIDs;

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            TypedQuery<String> query = em.createQuery("select s.studyInstanceUID from Study s where s.populated = false", String.class);
            query.setMaxResults(30);
            unpopulatedStudyUIDs = query.getResultList();

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return unpopulatedStudyUIDs;
    }

    private void fetchUnpopulatedSeries(List<UIDPair> unpopulatedSeriesUIDs) {

    }

    private void fetchUnpopulatedStudies(List<String> unpopulatedStudyUIDs) {

    }

}
