package online.kheops.auth_server.report_provider;


import online.kheops.auth_server.entity.ReportProvider;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

public class ReportProviderResponse {

    public enum Type {
        FULL {
            @Override
            public void buildResponse(ReportProviderResponse response, ReportProvider reportProvider) {
                response.name = reportProvider.getName();
                response.url = reportProvider.getUrl();
                response.clientId = reportProvider.getClientId();
                response.createdTime = reportProvider.getCreationTime();
            }
        },
        WEBHOOK{
            @Override
            public void buildResponse(ReportProviderResponse response, ReportProvider reportProvider) {
                response.name = reportProvider.getName();
                response.clientId = reportProvider.getClientId();
            }
        },
        EVENT{
            @Override
            public void buildResponse (ReportProviderResponse response, ReportProvider reportProvider) {
                response.name = reportProvider.getName();
                response.clientId = reportProvider.getClientId();
                response.isRemoved = reportProvider.isRemoved();
            }
        };

        public abstract void buildResponse(ReportProviderResponse response, ReportProvider reportProvider);
    }

    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "url")
    private String url;
    @XmlElement(name = "client_id")
    private String clientId;
    @XmlElement(name = "created_time")
    private LocalDateTime createdTime;
    @XmlElement(name = "is_removed")
    private Boolean isRemoved;


    private ReportProviderResponse() { /*empty*/ }

    public ReportProviderResponse(ReportProvider reportProvider, Type type) {
        type.buildResponse(this, reportProvider);
    }

}