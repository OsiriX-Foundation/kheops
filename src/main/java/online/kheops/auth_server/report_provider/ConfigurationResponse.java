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
    @XmlElement(name = "dicomweb_endpoint")
    private String dicomwebEndpoint;
    @XmlElement(name = "wado_endpoint")
    private String wadoEndpoint;
    @XmlElement(name = "token_endpoint")
    private String tokenEndpoint;
    @XmlElement(name = "introspect_uri")
    private String introspectUri;
    @XmlElement(name = "userinfo_endpoint")
    private String userInfoEndpoint;
    @XmlElement(name = "jwks_uri")
    private String jwksUri;


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
        dicomwebEndpoint = kheopsRootUri + "/api";
        wadoEndpoint = kheopsRootUri + "/api";
        tokenEndpoint = kheopsRootUri + "/api/token";
        introspectUri = kheopsRootUri + "/api/token/introspect";
        userInfoEndpoint = kheopsRootUri + "/api/userinfo";
        jwksUri = kheopsRootUri + "/api/certs";

    }
}
