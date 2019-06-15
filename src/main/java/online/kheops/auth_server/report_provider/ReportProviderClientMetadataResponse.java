package online.kheops.auth_server.report_provider;


import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;


public class ReportProviderClientMetadataResponse {
//https://tools.ietf.org/html/rfc7591


    @XmlElement(name = "valid")
    private Boolean valid;

    @XmlElement(name = "redirect_uri")
    private String redirectUri;
    @XmlElement(name = "token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;
    @XmlElement(name = "grant_types")
    private ArrayList<String> grantTypes;
    @XmlElement(name = "response_types")
    private ArrayList<String> responseTypes;
    @XmlElement(name = "client_name")
    private String clientName;
    @XmlElement(name = "client_uri")
    private String clientUri;
    @XmlElement(name = "logo_uri")
    private String logoUri;
    @XmlElement(name = "scope")
    private String scope;
    @XmlElement(name = "contacts")
    private ArrayList<String> contacts;
    @XmlElement(name = "tos_uri")
    private String tosUri;
    @XmlElement(name = "policy_uri")
    private String policyUri;
    @XmlElement(name = "jwks_uri")
    private String jwksUri;
    @XmlElement(name = "jwks")
    private String jwks;
    @XmlElement(name = "software_id")
    private String softwareId;
    @XmlElement(name = "software_version")
    private String softwareVersion;

    @XmlElement(name = "token_endpoint_auth_signing_alg")
    private String tokenEndpointAuthSigningAlg;

    public ReportProviderClientMetadataResponse() {
        valid = false;
    }

    public String getJwksUri() { return jwksUri; }
    public String getTokenEndpointAuthMethod() { return tokenEndpointAuthMethod; }
    public String getTokenEndpointAuthSigningAlg() { return tokenEndpointAuthSigningAlg; }
    public String getRedirectUri() { return redirectUri; }
    public String getClientName() { return clientName; }
    public String getClientUri() { return clientUri; }
    public ArrayList<String> getContacts() { return contacts; }

    public Boolean getValid() { return valid; }
    public void setValid(Boolean valid) { this.valid = valid; }

    public boolean isValid() {
        return jwksUri != null && tokenEndpointAuthMethod != null && tokenEndpointAuthSigningAlg != null && redirectUri != null;
    }
}
