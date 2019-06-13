package online.kheops.auth_server.report_provider;


import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;


public class ReportProviderClientMetadataResponse {

    @XmlElement(name = "valid")
    private Boolean valid;
    @XmlElement(name = "jwks_uri")
    private String jwksUri;
    @XmlElement(name = "token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;
    @XmlElement(name = "token_endpoint_auth_signing_alg")
    private String tokenEndpointAuthSigningAlg;
    @XmlElement(name = "redirect_uri")
    private String redirectUri;
    @XmlElement(name = "client_name")
    private String clienNname;
    @XmlElement(name = "client_uri")
    private String clientUri;
    @XmlElement(name = "contacts")
    private ArrayList<String> contacts;


    public ReportProviderClientMetadataResponse() {
        valid = false;
    }

    public String getJwksUri() { return jwksUri; }
    public String getTokenEndpointAuthMethod() { return tokenEndpointAuthMethod; }
    public String getTokenEndpointAuthSigningAlg() { return tokenEndpointAuthSigningAlg; }
    public String getRedirectUri() { return redirectUri; }
    public String getClienNname() { return clienNname; }
    public String getClientUri() { return clientUri; }
    public ArrayList<String> getContacts() { return contacts; }

    public Boolean getValid() { return valid; }
    public void setValid(Boolean valid) { this.valid = valid; }
}
