package online.kheops.auth_server.report_provider;


import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ReportProviderClientMetadata {
//https://tools.ietf.org/html/rfc7591


    @XmlElement(name = "valid")
    private Boolean valid;

    @XmlElement(name = "error_description")
    private String errorDescription;

    @XmlElement(name = "redirect_uri")
    private String redirectUri;
    @XmlElement(name = "token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;
    @XmlElement(name = "introspection_endpoint_auth_method")
    private String introspectionEndpointAuthMethod;
    @XmlElement(name = "grant_types")
    private ArrayList<String> grantTypes;
    @XmlElement(name = "response_type")
    private String responseType;
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
    @XmlElement(name = "software_id")
    private String softwareId;
    @XmlElement(name = "software_version")
    private String softwareVersion;
    @XmlElement(name = "supported_modalities")
    private ArrayList<String> supportedModalities;

    @XmlElement(name = "token_endpoint_auth_signing_alg")
    private String tokenEndpointAuthSigningAlg;
    @XmlElement(name = "introspection_endpoint_auth_signing_alg")
    private String introspectionEndpointAuthSigningAlg;

    public enum ValidationResult {
        OK("OK"),
        CONFIG_URI_SYNTAX_INVALID("config_uri syntax not valid"),
        REDIRECT_URI_IS_NULL("redirect_uri is null"),
        REDIRECT_URI_SYNTAX_INVALID("redirect_uri syntax not valid"),
        JWKS_URI_IS_NULL("jwks_uri is null"),
        JWKS_URI_SYNTAX_INVALID("jwks_uri syntax not valid"),
        BAD_REDIRECT_URI_ROOT("jwks_uri root does not match the config uri"),
        BAD_JWKS_URI_ROOT("redirect_uri root does not match the config uri"),
        RESPONSE_TYPE_MISSING("missing response_type"),
        RESPONSE_TYPE_UNKNOWN("unknown response_type"),
        TOKEN_ENDPOINT_AUTH_METHOD_MISSING("missing token_endpoint_auth_method"),
        TOKEN_ENDPOINT_AUTH_METHOD_UNKNOWN("unknown token_endpoint_auth_method"),
        TOKEN_ENDPOINT_AUTH_SIGNING_ALG_MISSING("missing token_endpoint_auth_signing_alg"),
        TOKEN_ENDPOINT_AUTH_SIGNING_ALG_UNKNOWN("unknown token_endpoint_auth_signing_alg");

        private final String description;

        ValidationResult(final String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public ReportProviderClientMetadata() {
        valid = false;
    }

    public String getJwksUri() { return jwksUri; }
    public String getTokenEndpointAuthMethod() { return tokenEndpointAuthMethod; }
    public String getTokenEndpointAuthSigningAlg() { return tokenEndpointAuthSigningAlg; }
    public String getRedirectUri() { return redirectUri; }
    public String getClientName() { return clientName; }
    public String getClientUri() { return clientUri; }

    public String getResponseType() {
        return responseType;
    }

    public List<String> getContacts() { return contacts; }

    public Boolean getValid() { return valid; }
    public void setValid(Boolean valid) { this.valid = valid; }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public ValidationResult validateForConfigUri(String configUriString) {
        final URI configUri;
        try {
            configUri = new URI(configUriString);
        } catch (URISyntaxException e) {
            return ValidationResult.CONFIG_URI_SYNTAX_INVALID;
        }

        if (redirectUri == null) {
            return ValidationResult.REDIRECT_URI_IS_NULL;
        }
        final URI parsedRedirectUri;
        try {
            parsedRedirectUri = new URI(redirectUri);
        } catch (URISyntaxException e) {
            return ValidationResult.REDIRECT_URI_SYNTAX_INVALID;
        }
        if (!configUri.getScheme().equals(parsedRedirectUri.getScheme()) ||
                !configUri.getAuthority().equals(parsedRedirectUri.getAuthority())) {
            return ValidationResult.BAD_REDIRECT_URI_ROOT;
        }

        if (responseType == null) {
            return ValidationResult.RESPONSE_TYPE_MISSING;
        }

        if (responseType.equals("code")) {
            if (tokenEndpointAuthMethod == null) {
                return ValidationResult.TOKEN_ENDPOINT_AUTH_METHOD_MISSING;
            }

            if (tokenEndpointAuthMethod.equals("private_key_jwt")) {
                if (jwksUri == null) {
                    return ValidationResult.JWKS_URI_IS_NULL;
                }
                final URI parsedJwksUri;
                try {
                    parsedJwksUri = new URI(jwksUri);
                } catch (URISyntaxException e) {
                    return ValidationResult.JWKS_URI_SYNTAX_INVALID;
                }
                if (!configUri.getScheme().equals(parsedJwksUri.getScheme()) ||
                        !configUri.getAuthority().equals(parsedJwksUri.getAuthority())) {
                    return ValidationResult.BAD_JWKS_URI_ROOT;
                }
            } else {
                return ValidationResult.TOKEN_ENDPOINT_AUTH_METHOD_UNKNOWN;
            }

            if (tokenEndpointAuthSigningAlg == null) {
                return ValidationResult.TOKEN_ENDPOINT_AUTH_SIGNING_ALG_MISSING;
            } else if (!tokenEndpointAuthSigningAlg.equals("RS256")) {
                return ValidationResult.TOKEN_ENDPOINT_AUTH_SIGNING_ALG_UNKNOWN;
            }
        } else if (!responseType.equals("token")) {
            return ValidationResult.RESPONSE_TYPE_UNKNOWN;
        }

        return ValidationResult.OK;
    }
}
