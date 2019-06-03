package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.ReportProvider;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.xml.bind.annotation.XmlElement;

import java.util.Collections;
import java.util.List;

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
    @XmlElement(name = "dicomweb_uri_endpoint")
    private String wadoEndpoint;
    @XmlElement(name = "token_endpoint")
    private String tokenEndpoint;
    @XmlElement(name = "introspection_endpoint")
    private String introspectUri;
    @XmlElement(name = "userinfo_endpoint")
    private String userInfoEndpoint;
//    @XmlElement(name = "jwks_uri")
//    private String jwksUri;
    @XmlElement(name = "grant_types_supported")
    private List<String> grantTypesSupported = Collections.singletonList("authorization_code");
    @XmlElement(name = "token_endpoint_auth_methods_supported")
    private List<String> tokenEndpointAuthMethodsSupported = Collections.singletonList("private_key_jwt");
    @XmlElement(name = "token_endpoint_auth_signing_alg_values_supported")
    private List<String> tokenEndpointAuthSigningAlgValuesSupported = Collections.singletonList("RS256");
    @XmlElement(name = "introspection_endpoint_auth_methods_supported")
    private List<String> introspectionEndpointAuthMethodsSupported = Collections.singletonList("private_key_jwt");
    @XmlElement(name = "introspection_endpoint_auth_signing_alg_values_supported")
    private List<String> introspectionEndpointAuthSigningAlgValuesSupported = Collections.singletonList("RS256");

    @XmlElement(name = "response_types_supported")
    private List<String> responseTypesSupported = Collections.singletonList("code");
    @XmlElement(name = "response_modes_supported")
    private List<String> responseModes_Supported = Collections.singletonList("query");

    public ConfigurationResponse() { /*empty*/ }

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
        wadoEndpoint = kheopsRootUri + "/api/wado";
        tokenEndpoint = kheopsRootUri + "/api/token";
        introspectUri = kheopsRootUri + "/api/token/introspect";
        userInfoEndpoint = kheopsRootUri + "/api/userinfo";
    }
}
