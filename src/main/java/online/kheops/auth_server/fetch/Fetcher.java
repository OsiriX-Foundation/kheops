package online.kheops.auth_server.fetch;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.PepAccessTokenBuilder;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.marshaller.JSONAttributesListMarshaller;
import online.kheops.auth_server.token.TokenProvenance;
import online.kheops.auth_server.util.Consts;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.series.SeriesQueries.findSeriesBySeriesUID;
import static online.kheops.auth_server.util.Consts.INCLUDE_FIELD;

public abstract class Fetcher {
    private static final Logger LOG = Logger.getLogger(Fetcher.class.getName());

    private static final Client CLIENT = newClient();
    private static UriBuilder studyUriBuilder = null;
    private static UriBuilder seriesUriBuilder = null;


    private static Client newClient() {
        final Client client = ClientBuilder.newClient();
        client.register(JSONAttributesListMarshaller.class);
        return client;
    }

    private Fetcher() {
    }

    static void setDicomWebURI(URI dicomWebURI) {
        studyUriBuilder = UriBuilder.fromUri(Objects.requireNonNull(dicomWebURI)).path("studies").queryParam("StudyInstanceUID", "{StudyInstanceUID}").queryParam(INCLUDE_FIELD, String.format("%08X", Tag.StudyDescription));
        seriesUriBuilder = UriBuilder.fromUri(Objects.requireNonNull(dicomWebURI)).path("studies/{StudyInstanceUID}/series").queryParam("SeriesInstanceUID", "{SeriesInstanceUID}").queryParam("includefield", String.format("%08X", Tag.BodyPartExamined));
    }

    public static Map<Series, FetchSeriesMetadata> fetchStudy(String studyInstanceUID) {
        final URI studyUri = studyUriBuilder.build(studyInstanceUID);


        final HashMap<Series, FetchSeriesMetadata> result = new HashMap<>();
        final Attributes attributes;
        try {
            String authToken = PepAccessTokenBuilder.newBuilder(new TokenProvenance() {})
                    .withStudyUID(studyInstanceUID).withAllSeries()
                    .withSubject("Fetcher").build();
            List<Attributes> studyList = CLIENT.target(studyUri).request().accept("application/dicom+json").header("Authorization", "Bearer "+authToken).get(new GenericType<List<Attributes>>() {});
            if (studyList == null || studyList.isEmpty()) {
                throw new WebApplicationException("GET to fetch study returned nothing");
            }
            attributes = studyList.get(0);
        } catch (ResponseProcessingException e) {
            logResponseProcessingException(e, studyInstanceUID);
            return result;
        } catch (ProcessingException | WebApplicationException e) {
            LOG.log(Level.SEVERE, "Unable to fetch QIDO data for StudyInstanceUID:" + studyInstanceUID, e);
            return result;
        }

        final List<String> seriesUIDList;
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            TypedQuery<Study> queryStudy = em.createQuery("select s from Study s where s.studyInstanceUID = :StudyInstanceUID", Study.class);
            queryStudy.setParameter(Consts.StudyInstanceUID, studyInstanceUID);
            queryStudy.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            final Study study = queryStudy.getSingleResult();
            study.mergeAttributes(attributes);
            study.setPopulated(true);

            final TypedQuery<String> query = em.createQuery("select s.seriesInstanceUID from Series s where s.study.studyInstanceUID = :studyInstanceUID", String.class);
            query.setParameter("studyInstanceUID", studyInstanceUID);
            seriesUIDList = query.getResultList();

            tx.commit();

            seriesUIDList.forEach(seriesUID -> result.putAll(fetchSeries(studyInstanceUID, seriesUID, em)));

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return result;
    }

    private static Map<Series, FetchSeriesMetadata> fetchSeries(String studyUID, String seriesUID, EntityManager em) {
        final URI uri = seriesUriBuilder.build(studyUID, seriesUID);

        final Attributes attributes;
        final HashMap<Series, FetchSeriesMetadata> result = new HashMap<>();
        try {
            String authToken = PepAccessTokenBuilder.newBuilder(new TokenProvenance() {})
                    .withStudyUID(studyUID)
                    .withSeriesUID(seriesUID)
                    .withSubject("Fetcher")
                    .build();
            List<Attributes> seriesList = CLIENT.target(uri).request().accept("application/dicom+json").header("Authorization", "Bearer " + authToken).get(new GenericType<List<Attributes>>() {
            });
            if (seriesList == null || seriesList.isEmpty()) {
                throw new WebApplicationException("GET to fetch series returned nothing");
            }
            attributes = seriesList.get(0);
        } catch (ResponseProcessingException e) {
            logResponseProcessingException(e, studyUID, seriesUID);
            return result;
        } catch (ProcessingException | WebApplicationException e) {
            LOG.log(Level.SEVERE, "Unable to fetch QIDO data for StudyInstanceUID:" + studyUID + " SeriesInstanceUID: " + seriesUID, e);
            return result;
        }

        final EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            final Series series = findSeriesBySeriesUID(seriesUID, em);
            final int oldValueNumberOfSeriesRelatedInstances = series.getNumberOfSeriesRelatedInstances();
            series.mergeAttributes(attributes);
            series.setPopulated(true);

            result.put(series, new FetchSeriesMetadata(series.getNumberOfSeriesRelatedInstances() - oldValueNumberOfSeriesRelatedInstances));

            tx.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error while storing series: " + seriesUID, e);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
        return result;
    }

    private static void logResponseProcessingException(ResponseProcessingException e, String studyUID) {
        final Response response = e.getResponse();
        try {
            String responseString = e.getResponse().readEntity(String.class);
            LOG.log(Level.SEVERE, "Unable to fetch QIDO data for StudyInstanceUID:" + studyUID + " status:" + response.getStatus() +
                    " response:\n" + responseString, e);
        } catch (ProcessingException | IllegalStateException exception) {
            LOG.log(Level.SEVERE, "Unable to fetch QIDO data for StudyInstanceUID:" + studyUID + " status:" + response.getStatus(), e);
            LOG.log(Level.SEVERE, "Error while getting the response string", exception);
        }
    }

    private static void logResponseProcessingException(ResponseProcessingException e, String studyUID, String seriesUID) {
        final Response response = e.getResponse();
        try {
            String responseString = e.getResponse().readEntity(String.class);
            LOG.log(Level.SEVERE, "Unable to fetch QIDO data for StudyInstanceUID:" + studyUID + " SeriesInstanceUID: " + seriesUID +
                    " status:" + response.getStatus() + " response:\n" + responseString, e);
        } catch (ProcessingException | IllegalStateException exception) {
            LOG.log(Level.SEVERE, "Unable to fetch QIDO data for StudyInstanceUID:" + studyUID + " SeriesInstanceUID: " + seriesUID +
                    " status:" + response.getStatus(), e);
            LOG.log(Level.SEVERE, "Error while getting the response string", exception);
        }
    }
}
