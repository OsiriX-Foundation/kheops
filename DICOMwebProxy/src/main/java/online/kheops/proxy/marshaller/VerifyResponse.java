package online.kheops.proxy.marshaller;

import javax.xml.bind.annotation.XmlElement;

public class VerifyResponse {

    @XmlElement(name = "is_valid")
    private boolean isValid;
    @XmlElement(name = "has_series_add_access")
    private Boolean hasSeriesAddAccess;
    @XmlElement(name = "reason")
    private String reason;

    public VerifyResponse() { /*empty*/ }

    public boolean isValid() { return isValid; }
    public Boolean getHasSeriesAddAccess() { return hasSeriesAddAccess; }
    public String getReason() { return reason; }
}
