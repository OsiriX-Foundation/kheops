package online.kheops.auth_server.report_provider;


import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.user.UserResponse;
import online.kheops.auth_server.user.UserResponseBuilder;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

public class ReportProviderResponse {

    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "url")
    private String url;
    @XmlElement(name = "client_id")
    private String clientId;
    @XmlElement(name = "user")
    private UserResponse user;
    @XmlElement(name = "created_time")
    private LocalDateTime createdTime;

    private ReportProviderResponse() { /*empty*/ }

    protected ReportProviderResponse(ReportProvider reportProvider) {
        name = reportProvider.getName();
        url = reportProvider.getUrl();
        clientId = reportProvider.getClientId();

        final UserResponseBuilder userResponseBuilder = new UserResponseBuilder();
        user = userResponseBuilder.setSub(reportProvider.getUser().getKeycloakId()).setEmail(reportProvider.getUser().getEmail()).build();

        createdTime = reportProvider.getCreationTime();
    }
}
