package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.study.StudyResponse;
import online.kheops.auth_server.user.UserResponse;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static online.kheops.auth_server.report_provider.ReportProviderResponse.Type.WEBHOOK;

public class RemoveSeriesWebhook implements WebhookResult{

    @XmlElement(name = "host")
    private String kheopsInstance;
    @XmlElement(name = "album_id")
    private String albumId;
    @XmlElement(name = "event_time")
    private LocalDateTime eventTime;
    @XmlElement(name = "source")
    private UserResponse sourceUser;
    @XmlElement(name = "is_manual_trigger")
    private boolean isManualTrigger;
    @XmlElement(name = "remove_all_series")
    private boolean removeAllSeries;

    @XmlElement(name = "removed_study")
    private StudyResponse removedStudy;


    private RemoveSeriesWebhook() { /*empty*/ }

    @Override
    public WebhookType getType() {
        return WebhookType.REMOVE_SERIES;
    }

    public static class Builder {

        private String kheopsInstance;
        private String albumId;
        private UserResponse sourceUser;
        private boolean isManualTrigger;
        private boolean removeAllSeries;
        private Study study;
        private List<Series> seriesList;
        private Source source;
        private Boolean isAdmin;

        public Builder() {
            seriesList = new ArrayList<>();
        }

        public Builder albumId(String albumId) {
            this.albumId = albumId;
            return this;
        }

        public Builder isManualTrigger(boolean isManualTrigger) {
            this.isManualTrigger = isManualTrigger;
            return this;
        }

        public Builder kheopsInstance(String kheopsInstance) {
            this.kheopsInstance = kheopsInstance;
            return this;
        }

        public Builder source(Source source) {
            this.source = source;
            return this;
        }

        public Builder isAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }

        public Builder study(Study study) {
            this.study = study;
            return this;
        }

        public Builder addSeries(Series series) {
            this.seriesList.add(series);
            return this;
        }

        public Builder removeAllSeries(boolean removeAllSeries) {
            this.removeAllSeries = removeAllSeries;
            return this;
        }



        public RemoveSeriesWebhook build() {
            final RemoveSeriesWebhook removeSeriesWebhook = new RemoveSeriesWebhook();

            removeSeriesWebhook.kheopsInstance = kheopsInstance;
            removeSeriesWebhook.albumId = albumId;
            removeSeriesWebhook.isManualTrigger = isManualTrigger;
            removeSeriesWebhook.eventTime = LocalDateTime.now();

            final StudyResponse.Builder studyResponseBuilder = new StudyResponse.Builder(study)
                    .uidOnly(false)
                    .kheopsInstance(kheopsInstance);
            if (removeAllSeries) {
                removeSeriesWebhook.removeAllSeries = true;
            } else {
                studyResponseBuilder.showRetrieveUrl().hideRetrieveUrlSeriesOnly();
            }
            for(Series series:seriesList) {
                studyResponseBuilder.addSeries(series);
            }
            removeSeriesWebhook.removedStudy = studyResponseBuilder.build();

            sourceUser = new UserResponse(source.getUser());
            if (isAdmin != null) {
                sourceUser.setIsAdmin(isAdmin);
            }

            source.getCapabilityToken().ifPresent(sourceUser::setCapabilityToken);
            source.getReportProvider().ifPresent(reportProvider -> sourceUser.setReportProvider(reportProvider, WEBHOOK));

            removeSeriesWebhook.sourceUser = sourceUser;
            return removeSeriesWebhook;
        }
    }
}