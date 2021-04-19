package online.kheops.auth_server.webhook;

import online.kheops.auth_server.KheopsInstance;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.WebhookTrigger;
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

    public WebhookTriggerResponse(WebhookTrigger webhookTrigger, KheopsInstance kheopsInstance) {

        id = webhookTrigger.getId();
        isManualTrigger = webhookTrigger.isManualTrigger();
        if(webhookTrigger.getNewSeries()) {
            event = WebhookType.NEW_SERIES.name().toLowerCase();
            StudyResponse.Builder studyResponseBuilder = null;
            for (Series series: webhookTrigger.getSeries()) {
                if (studyResponseBuilder == null) {
                    studyResponseBuilder = new StudyResponse.Builder(series.getStudy())
                            .showRetrieveUrl()
                            .kheopsInstance(kheopsInstance.get())
                            .uidOnly(true);
                }
                studyResponseBuilder.addSeries(series);
            }
            studyResponse = studyResponseBuilder != null ? studyResponseBuilder.build() : null;

        } else if(webhookTrigger.getNewUser()) {
            event = WebhookType.NEW_USER.name().toLowerCase();
            final UserResponseBuilder userResponseBuilder = new UserResponseBuilder();
            user = userResponseBuilder.setEmail(webhookTrigger.getUser().getEmail())
                    .setName(webhookTrigger.getUser().getName())
                    .setSub(webhookTrigger.getUser().getSub())
                    .build();
        } else if(webhookTrigger.getRemoveSeries()) {
            event = WebhookType.REMOVE_SERIES.name().toLowerCase();
            StudyResponse.Builder studyResponseBuilder = null;
            for (Series series: webhookTrigger.getSeries()) {
                if (studyResponseBuilder == null) {
                    studyResponseBuilder = new StudyResponse.Builder(series.getStudy())
                            .hideRetrieveUrl()
                            .uidOnly(true);
                }
                studyResponseBuilder.addSeries(series);
            }
            studyResponse = studyResponseBuilder != null ? studyResponseBuilder.build() : null;
        }

        if(!webhookTrigger.getWebhookAttempts().isEmpty()) {
            webhookAttemptResponseList = new ArrayList<>();
            webhookTrigger.getWebhookAttempts().forEach(
                    webhookAttempt -> webhookAttemptResponseList.add(new WebhookAttemptResponse(webhookAttempt)));
        }
    }
}
