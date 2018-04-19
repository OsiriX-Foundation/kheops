package online.kheops.auth_server;

import online.kheops.auth_server.entity.Series;

import javax.persistence.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

public class SeriesFetchTask implements Runnable {
    @Override
    public void run() {
        System.out.println("fetching series");

        // find series with a modality of null;

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("online.kheops");
        EntityManager em = factory.createEntityManager();
        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            List<Series> unpopulatedSeries;
            TypedQuery<Series> studyQuery = em.createQuery("select s from Series s where s.modality = null", Series.class);
            unpopulatedSeries = studyQuery.getResultList();

            UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs/studies/{StudyInstanceUID}/series?SeriesInstanceUID={SeriesInstanceUID}");

            for (Series series: unpopulatedSeries) {
                // TODO acquire a token to access the server
                Client client = ClientBuilder.newClient();
                client.register(SeriesDTOMarshaller.class);

                URI uri = uriBuilder.build(series.getStudy().getStudyInstanceUID(), series.getSeriesInstanceUID());
                try {

                    List<SeriesDTO> seriesList = client.target(uri).request().accept("application/dicom+json").get(new GenericType<List<SeriesDTO>>() {});

                    if (seriesList != null && seriesList.size() > 0) {
                        series.setModality(seriesList.get(0).getModality());
                        em.persist(series);
                    }

                    System.out.println("seriesUID: " + series.getSeriesInstanceUID() + " studyUID: " + series.getStudy().getStudyInstanceUID());
                } catch (Exception exception) {
                    exception.printStackTrace();
                } catch (Throwable t) {
                    System.out.println(t.getLocalizedMessage());
                }
            }

            tx.commit();
        } finally {
            em.close();
            factory.close();
        }
    }
}
