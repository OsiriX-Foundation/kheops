package online.kheops.auth_server;

import online.kheops.auth_server.entity.Series;

import javax.persistence.*;
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

            for (Series series: unpopulatedSeries) {
                //Joels-MacBook-Pro:sql spalte$ curl -v --header "Accept: application/json" localhost:8042/dicom-web/studies/2.16.840.1.113669.632.20.121711.10000158860/series | less

                // TODO acquire a token to access the server

                





                System.out.println("seriesUID: " + series.getSeriesInstanceUID() + " studyUID: " + series.getStudy().getStudyInstanceUID());
            }

            tx.commit();
        } finally {
            em.close();
            factory.close();
        }
    }
}
