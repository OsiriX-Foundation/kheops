package online.kheops.auth_server.report_provider;

import javax.xml.bind.annotation.XmlElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigurationResponse {

    @XmlElement(name = "issuer")
    private String issuer;
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
    private List<String> grantTypesSupported = Arrays.asList("authorization_code", "implicit");
    @XmlElement(name = "token_endpoint_auth_methods_supported")
    private List<String> tokenEndpointAuthMethodsSupported = Collections.singletonList("private_key_jwt");
    @XmlElement(name = "token_endpoint_auth_signing_alg_values_supported")
    private List<String> tokenEndpointAuthSigningAlgValuesSupported = Collections.singletonList("RS256");
    @XmlElement(name = "introspection_endpoint_auth_methods_supported")
    private List<String> introspectionEndpointAuthMethodsSupported = Arrays.asList("private_key_jwt", "none");
    @XmlElement(name = "introspection_endpoint_auth_signing_alg_values_supported")
    private List<String> introspectionEndpointAuthSigningAlgValuesSupported = Collections.singletonList("RS256");

    @XmlElement(name = "response_types_supported")
    private List<String> responseTypesSupported = Arrays.asList("code", "token");
    @XmlElement(name = "response_modes_supported")
    private List<String> responseModesSupported = Arrays.asList("query", "fragment");

    public ConfigurationResponse() { /*empty*/ }

    public ConfigurationResponse(String kheopsRootUri) {

        issuer = kheopsRootUri;
        dicomwebEndpoint = kheopsRootUri + "/api";
        wadoEndpoint = kheopsRootUri + "/api/wado";
        tokenEndpoint = kheopsRootUri + "/api/token";
        introspectUri = kheopsRootUri + "/api/token/introspect";
        userInfoEndpoint = kheopsRootUri + "/api/userinfo";
    }
}
