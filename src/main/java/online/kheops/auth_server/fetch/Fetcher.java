package online.kheops.auth_server.fetch;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.PACSAuthTokenBuilder;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.marshaller.AttributesListMarshaller;
import org.dcm4che3.data.Attributes;

import javax.persistence.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Fetcher {
    private static final Logger LOG = Logger.getLogger(Fetcher.class.getName());

    private static final Client CLIENT = newClient();
    private static UriBuilder uriBuilder = null;


    private static Client newClient() {
        final Client client = ClientBuilder.newClient();
        client.register(AttributesListMarshaller.class);
        return client;
    }

    private Fetcher() {
    }

    static void setDicomWebURI(URI dicomWebURI) {
        uriBuilder = UriBuilder.fromUri(Objects.requireNonNull(dicomWebURI)).path("studies/{StudyInstanceUID}/series").queryParam("SeriesInstanceUID", "{SeriesInstanceUID}");
    }

    public static void fetchStudy(String studyInstanceUID) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            final TypedQuery<String> query = em.createQuery("select s.seriesInstanceUID from Series s where s.study.studyInstanceUID = :studyInstanceUID", String.class);
            query.setParameter("studyInstanceUID", studyInstanceUID);
            query.getResultList().forEach(seriesUID -> fetchSeries(studyInstanceUID, seriesUID));

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    private static boolean fetchSeries(String studyUID, String seriesUID) {
        final URI uri = uriBuilder.build(studyUID, seriesUID);

        final Attributes attributes;
        try {
            String authToken = PACSAuthTokenBuilder.newBuilder().withStudyUID(studyUID).withSeriesUID(seriesUID).build();
            List<Attributes> seriesList = CLIENT.target(uri).request().accept("application/dicom+json").header("Authorization", "Bearer " + authToken).get(new GenericType<List<Attributes>>() {
            });
            if (seriesList == null || seriesList.isEmpty()) {
                throw new WebApplicationException("GET to fetch series returned nothing");
            }
            attributes = seriesList.get(0);
        } catch (WebApplicationException e) {
            LOG.log(Level.SEVERE, "Unable to fetch QIDO data for StudyInstanceUID:" + studyUID + " SeriesInstanceUID: " + seriesUID, e);
            return false;
        }

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            TypedQuery<Series> query = em.createQuery("select s from Series s where s.seriesInstanceUID = :seriesInstanceUID", Series.class);
            query.setParameter("seriesInstanceUID", seriesUID);
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

            Series series = query.getSingleResult();
            series.mergeAttributes(attributes);
            series.setPopulated(true);
            LOG.log(Level.WARNING, "FETCH SERIES : " + series.getSeriesInstanceUID());
            tx.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error while storing series: " + seriesUID, e);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return true;
    }
}
