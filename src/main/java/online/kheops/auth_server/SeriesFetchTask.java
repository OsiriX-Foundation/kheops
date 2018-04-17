package online.kheops.auth_server;

import online.kheops.auth_server.entity.Series;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.json.JSONReader;
import org.glassfish.jersey.client.ClientConfig;

import javax.json.Json;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.persistence.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
                //Joels-MacBook-Pro:sql spalte$ curl -v --header "Accept: application/json" localhost:8042/dicom-web/studies/2.16.840.1.113669.632.20.121711.10000158860/series | less

                // TODO acquire a token to access the server
//                ClientConfig cc = new ClientConfig().register(new SeriesXMLUnmarshaller());
                Client client = ClientBuilder.newClient();
                client.register(SeriesXMLUnmarshaller.class);

                URI uri = uriBuilder.build(series.getStudy().getStudyInstanceUID(), series.getSeriesInstanceUID());
                try {

//                    String theString = client.target(uri).request().accept("application/dicom+xml").get(String.class);
//                    List<JSONSeries> xmlList = client.target(uri).request().accept("application/dicom+xml").get(new GenericType<List<JSONSeries>>() {});
//                    List<String> xmlList = client.target(uri).request().accept("application/dicom+xml").get(new GenericType<List<String>>() {});

//
//
                    Response response = client.target(uri).request().accept("application/dicom+json").get();
////                    JSONSeries jsonSeries = response.readEntity(JSONSeries.class);
////                    List<JSONSeries> jsonSerieslist = response.readEntity(JSONSeries.class);
//
//                    List<JSONSeries> xmlList= response.readEntity(new GenericType<List<JSONSeries>>() {});
//                    for (JSONSeries xmlString: xmlList) {
//
//                        System.out.println(xmlString);
//                    }


//                    String modality = null;
                    String jsonString = response.readEntity(String.class);
                    JsonParser parser = Json.createParser(new StringReader(jsonString));

                    JSONReader jsonReader = new JSONReader(parser);
                    jsonReader.readDatasets(new JSONReader.Callback() {
                        @Override
                        public void onDataset(Attributes fmi, Attributes dataset) {
                            String modality = dataset.getString(0x00080060);
                            Map<String, Object> properties =  dataset.getProperties();
                            System.out.println(modality);
                            System.out.println(properties);
                        }
                    });
                    Attributes metadata = jsonReader.readDataset(null);
                    Map<String, Object> properties =  metadata.getProperties();
                    System.out.println(properties);

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
