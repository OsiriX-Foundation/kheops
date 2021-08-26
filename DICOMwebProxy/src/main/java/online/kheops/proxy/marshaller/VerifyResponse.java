package online.kheops.proxy.marshaller;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class VerifyResponse {

    @XmlElement(name = "is_valid")
    private boolean isValid;
    @XmlElement(name = "has_series_add_access")
    private Boolean hasSeriesAddAccess;
    @XmlElement(name = "reason")
    private String reason;
    @XmlElement(name = "unmatching_tags")
    private List<String> unmatchingTags;

    public VerifyResponse() { /*empty*/ }

    public boolean isValid() { return isValid; }
    public Boolean getHasSeriesAddAccess() { return hasSeriesAddAccess; }
    public String getReason() { return reason; }
    public List<String> getUnmatchingTags() { return unmatchingTags; }
}
