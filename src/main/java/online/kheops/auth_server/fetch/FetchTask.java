package online.kheops.auth_server.fetch;

import com.fasterxml.classmate.GenericType;
import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.PACSAuthTokenBuilder;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.marshaller.JSONAttributesListMarshaller;
import online.kheops.auth_server.util.Consts;

import javax.persistence.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import org.dcm4che3.data.Attributes;

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
       /* UriBuilder uriBuilder = UriBuilder.fromUri(dicomWebURI).path("studies/{StudyInstanceUID}/series").queryParam("SeriesInstanceUID", "{SeriesInstanceUID}");

        Client client = ClientBuilder.newClient();
        client.register(JSONAttributesListMarshaller.class);

        for (UIDPair seriesUID: unpopulatedSeriesUIDs) {
            URI uri = uriBuilder.build(seriesUID.getStudyInstanceUID(), seriesUID.getSeriesInstanceUID());

            final Attributes attributes;
            try {
                String authToken = PACSAuthTokenBuilder.newBuilder().withStudyUID(seriesUID.getStudyInstanceUID()).withSeriesUID(seriesUID.getSeriesInstanceUID()).build();
                List<Attributes> seriesList = client.target(uri).request().accept("application/dicom+json").header("Authorization", "Bearer "+authToken).get(new GenericType<List<Attributes>>() {});
                if (seriesList == null || seriesList.isEmpty()) {
                    throw new WebApplicationException("GET to fetch series returned nothing");
                }
                attributes = seriesList.get(0);
            } catch (WebApplicationException e) {
                LOG.log(Level.SEVERE,"Unable to fetch QIDO data for StudyInstanceUID:" + seriesUID.getStudyInstanceUID() + " SeriesInstanceUID: " + seriesUID.getSeriesInstanceUID(), e);
                continue;
            }

            EntityManager em = EntityManagerListener.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                TypedQuery<Series> query = em.createQuery("select s from Series s where s.seriesInstanceUID = :seriesInstanceUID", Series.class);
                query.setParameter("seriesInstanceUID", seriesUID.seriesInstanceUID);
                query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

                Series series = query.getSingleResult();
                series.mergeAttributes(attributes);
                series.setPopulated(true);

                tx.commit();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error while storing series: " + seriesUID.getSeriesInstanceUID(), e);
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        }*/
    }

    private void fetchUnpopulatedStudies(List<String> unpopulatedStudyUIDs) {
       /* UriBuilder uriBuilder = UriBuilder.fromUri(dicomWebURI).path("studies").queryParam("StudyInstanceUID", "{StudyInstanceUID}");

        Client client = ClientBuilder.newClient();
        client.register(JSONAttributesListMarshaller.class);

        for (String studyInstanceUID: unpopulatedStudyUIDs) {
            final URI uri = uriBuilder.build(studyInstanceUID);

            final Attributes attributes;
            try {
                String authToken = PACSAuthTokenBuilder.newBuilder().withStudyUID(studyInstanceUID).withAllSeries().build();
                List<Attributes> studyList = client.target(uri).request().accept("application/dicom+json").header("Authorization", "Bearer "+authToken).get(new GenericType<List<Attributes>>() {});

                if (studyList == null || studyList.isEmpty()) {
                    throw new WebApplicationException("GET to fetch study returned nothing");
                }
                attributes = studyList.get(0);
            } catch (WebApplicationException e) {
                LOG.log(Level.SEVERE, "Unable to fetch QIDO data for StudyInstanceUID:" + studyInstanceUID, e);
                continue;
            }

            EntityManager em = EntityManagerListener.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                TypedQuery<Study> query = em.createQuery("select s from Study s where s.studyInstanceUID = :StudyInstanceUID", Study.class);
                query.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
                query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

                Study study = query.getSingleResult();
                study.mergeAttributes(attributes);
                study.setPopulated(true);

                tx.commit();
            } catch (Exception e) {
                LOG.log(Level.SEVERE,"Error while fetching study: " + studyInstanceUID, e);
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        }
*/
    }

}
