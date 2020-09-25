package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.report_provider.ReportProviderResponse;
import online.kheops.auth_server.study.StudyResponse;
import online.kheops.auth_server.user.UserResponse;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RemoveSeriesWebhook implements WebhookResult{

    @XmlElement(name = "host")
    private String instance;
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

        private String instance;
        private String albumId;
        private UserResponse sourceUser;
        private ReportProvider reportProvider;
        private Capability capability;
        private boolean isManualTrigger;
        private boolean removeAllSeries;
        private Study study;
        private List<Series> seriesList;

        public Builder() {
            seriesList = new ArrayList<>();
        }

        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public Builder albumId(String albumId) {
            this.albumId = albumId;
            return this;
        }

        public Builder isManualTrigger(boolean isManualTrigger) {
            this.isManualTrigger = isManualTrigger;
            return this;
        }

        public Builder sourceUser(AlbumUser sourceUser) {
            this.sourceUser = new UserResponse(sourceUser);
            return this;
        }
        public Builder reportProvider(ReportProvider reportProvider) {
            this.reportProvider = reportProvider;
            return this;
        }

        public Builder capabilityToken(Capability capability) {
            this.capability = capability;
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

            if(reportProvider != null) {
                sourceUser.setReportProvider(reportProvider, ReportProviderResponse.Type.WEBHOOK);
            } else if (capability != null) {
                sourceUser.setCapabilityToken(capability);
            }
            removeSeriesWebhook.sourceUser = sourceUser;

            removeSeriesWebhook.instance = instance;
            removeSeriesWebhook.albumId = albumId;
            removeSeriesWebhook.isManualTrigger = isManualTrigger;
            removeSeriesWebhook.eventTime = LocalDateTime.now();

            removeSeriesWebhook.removedStudy = new StudyResponse(study, instance);
            if (removeAllSeries) {
                removeSeriesWebhook.removedStudy.hideRetrieveUrl();
                removeSeriesWebhook.removeAllSeries = true;
            }
            for(Series series:seriesList) {
                removeSeriesWebhook.removedStudy.addSeries(series, true);
            }
            return removeSeriesWebhook;

        }
    }
}