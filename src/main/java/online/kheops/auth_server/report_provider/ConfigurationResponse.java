package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.ReportProvider;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.xml.bind.annotation.XmlElement;

import static online.kheops.auth_server.report_provider.ReportProviderQueries.getReportProviderWithClientId;

public class ConfigurationResponse {

    @XmlElement(name = "album_id")
    private String albumId;
    @XmlElement(name = "client_id")
    private String clientId;
    @XmlElement(name = "issuer")
    private String issuer;
    @XmlElement(name = "return_uri")
    private String returnUri;
    @XmlElement(name = "dicomweb_uri")
    private String dicomwebUri;
    @XmlElement(name = "wado_uri")
    private String wadoUri;
    @XmlElement(name = "token_uri")
    private String tokenUri;
    @XmlElement(name = "introspect_uri")
    private String introspectUri;


    private ConfigurationResponse() { /*empty*/ }

    public ConfigurationResponse(String clientId, String kheopsRootUri)
            throws ClientIdNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        final ReportProvider reportProvider;
        try {
            tx.begin();
            reportProvider = getReportProviderWithClientId(clientId, em);
            albumId = reportProvider.getAlbum().getId();
        } catch (NoResultException e){
            throw new ClientIdNotFoundException("ClientId: "+ clientId + " Not Found");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        this.clientId = clientId;
        issuer = kheopsRootUri;
        returnUri = kheopsRootUri + "/albums/" + albumId;
        dicomwebUri = kheopsRootUri + "/api";
        wadoUri = kheopsRootUri + "/api";
        tokenUri = kheopsRootUri + "/api/token";
        introspectUri = kheopsRootUri + "/api/token/introspect";

    }
}
