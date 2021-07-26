package online.kheops.auth_server.util;

import javax.xml.bind.annotation.XmlElement;

public class VerifyResponse {
    @XmlElement(name = "is_valid")
    private boolean isValid;
    @XmlElement(name = "has_series_add_access")
    private Boolean hasSeriesAddAccess;
    @XmlElement(name = "reason")
    private String reason;

    private VerifyResponse() { /*empty*/ }

    public VerifyResponse(boolean isValid, Boolean hasSeriesAddAccess, String reason) {
        this.isValid = isValid;
        this.hasSeriesAddAccess = hasSeriesAddAccess;
        this.reason = reason;
    }
}
