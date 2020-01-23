package online.kheops.auth_server.report_provider;


import online.kheops.auth_server.entity.ReportProvider;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

public class ReportProviderResponse {

    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "url")
    private String url;
    @XmlElement(name = "client_id")
    private String clientId;
    @XmlElement(name = "created_time")
    private LocalDateTime createdTime;


    private ReportProviderResponse() { /*empty*/ }

    public ReportProviderResponse(ReportProvider reportProvider) {
        name = reportProvider.getName();
        url = reportProvider.getUrl();
        clientId = reportProvider.getClientId();
        createdTime = reportProvider.getCreationTime();
    }

    public void WebhookResponse() {
        url = null;
        createdTime = null;
    }
}
