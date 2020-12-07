package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.study.StudyResponse;
import online.kheops.auth_server.user.UserResponse;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;

import static online.kheops.auth_server.report_provider.ReportProviderResponse.Type.WEBHOOK;

public class NewSeriesWebhook implements WebhookResult{

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
    @XmlElement(name = "import_source")
    private String importSource;

    @XmlElement(name = "updated_study")
    private StudyResponse updatedStudy;

    private NewSeriesWebhook() { /*empty*/ }


    private NewSeriesWebhook(Builder builder) {
        this.kheopsInstance = builder.kheopInstance;
        albumId = builder.albumId;
        eventTime = LocalDateTime.now();
        sourceUser = builder.sourceUser;
        isManualTrigger = builder.isManualTrigger;
        importSource = builder.importSource;
        updatedStudy = builder.updatedStudy;
    }

    public static Builder builder() { return new Builder(); }

    @Override
    public WebhookType getType() {
        return WebhookType.NEW_SERIES;
    }

    public static class Builder {
        private String albumId;
        private UserResponse sourceUser;
        private boolean isManualTrigger;
        private String importSource;
        private StudyResponse updatedStudy;
        private Study study;
        private String kheopInstance;
        private HashMap<Series, Integer> series = new HashMap<>();
        private Boolean isAdmin;

        public Builder() { /*empty*/ }

        public Builder setDestination(String destination) {
            if (destination == null) {
                throw new IllegalStateException("destiation is null");
            }
            albumId = destination;
            return this;
        }

        public Builder setSource(Source source) {
            if (source == null) {
                throw new IllegalStateException("source is null");
            }

            sourceUser = new UserResponse(source.getUser());
            source.getCapabilityToken().ifPresent(sourceUser::setCapabilityToken);
            source.getReportProvider().ifPresent(reportProvider -> sourceUser.setReportProvider(reportProvider, WEBHOOK));
            return this;
        }

        public Builder setSource(AlbumUser sourceUser) {
            this.sourceUser = new UserResponse(sourceUser);
            return this;
        }

        public Builder isAdmin(Boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }

        public Builder isManualTrigger() {
            this.isManualTrigger = true;
            return this;
        }

        public Builder isAutomatedTrigger() {
            this.isManualTrigger = false;
            return this;
        }

        public Builder setKheopsInstance(String kheopsInstance) {
            this.kheopInstance = kheopsInstance;
            return this;
        }

        public Builder isUpload() {
            importSource = "upload";
            return this;
        }

        public Builder isSent() {
            importSource = "send";
            return this;
        }

        public Builder setStudy(Study study) {
            this.study = study;
            return this;
        }

        public Builder addSeries(Series series, Integer numberOfNewInstances) {
            this.series.put(series, numberOfNewInstances);
            return this;
        }

        public Builder addSeries(Series series) {
            this.series.put(series, null);
            return this;
        }

        public Set<Series> getSeries() { return series.keySet(); }

        public NewSeriesWebhook build() {
            if (isAdmin != null) {
                sourceUser.setIsAdmin(isAdmin);
            }
            final StudyResponse.Builder studyResponseBuilder = new StudyResponse.Builder(study)
                    .showRetrieveUrl()
                    .kheopsInstance(kheopInstance)
                    .uidOnly(false);

            series.forEach(studyResponseBuilder::addSeries);
            updatedStudy = studyResponseBuilder.build();

            return new NewSeriesWebhook(this);
        }

    }
}