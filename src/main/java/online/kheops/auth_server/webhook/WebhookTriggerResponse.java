package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.WebhookAttempt;
import online.kheops.auth_server.entity.WebhookTrigger;
import online.kheops.auth_server.entity.WebhookTriggerSeries;
import online.kheops.auth_server.study.StudyResponse;
import online.kheops.auth_server.user.UserResponse;
import online.kheops.auth_server.user.UserResponseBuilder;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class WebhookTriggerResponse {


    @XmlElement(name = "id")
    private String id;
    @XmlElement(name = "is_manual_trigger")
    private boolean isManualTrigger;
    @XmlElement(name = "event")
    private String event;
    @XmlElement(name = "attempts")
    private List<WebhookAttemptResponse> webhookAttemptResponseList;
    @XmlElement(name = "user")
    private UserResponse user;
    @XmlElement(name = "study")
    private StudyResponse studyResponse;



    private WebhookTriggerResponse() { /*Empty*/ }

    public WebhookTriggerResponse(WebhookTrigger webhookTrigger) {

        id = webhookTrigger.getId();
        isManualTrigger = webhookTrigger.isManualTrigger();
        if(webhookTrigger.getNewSeries()) {
            event = WebhookType.NEW_SERIES.name().toLowerCase();
            for (WebhookTriggerSeries webhookTriggerSeries: webhookTrigger.getWebhookTriggersSeries()) {
                if (studyResponse == null) {
                    studyResponse = new StudyResponse(webhookTriggerSeries.getSeries().getStudy());
                }
                studyResponse.addSeries(webhookTriggerSeries.getSeries());
            }
        } else if(webhookTrigger.getNewUser()) {
            event = WebhookType.NEW_USER.name().toLowerCase();
            final UserResponseBuilder userResponseBuilder = new UserResponseBuilder();
            user = userResponseBuilder.setEmail(webhookTrigger.getUser().getEmail())
                    .setLastName(webhookTrigger.getUser().getLastName())
                    .setFirstName(webhookTrigger.getUser().getFirstName())
                    .setSub(webhookTrigger.getUser().getKeycloakId())
                    .build();
        }

        if(!webhookTrigger.getWebhookAttempts().isEmpty()) {
            webhookAttemptResponseList = new ArrayList<>();
            for (WebhookAttempt webhookAttempt : webhookTrigger.getWebhookAttempts()) {
                webhookAttemptResponseList.add(new WebhookAttemptResponse(webhookAttempt));
            }
        }
    }
}
