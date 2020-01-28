package online.kheops.auth_server.webhook;

import online.kheops.auth_server.capability.CapabilitiesResponse;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.report_provider.ReportProviderResponse;
import online.kheops.auth_server.study.StudyResponse;
import online.kheops.auth_server.user.UserResponse;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

public class NewSeriesWebhook implements WebhookResult{

    @XmlElement(name = "host")
    private String instance;
    @XmlElement(name = "album_id")
    private String albumId;
    @XmlElement(name = "event_type")
    private String eventType;
    @XmlElement(name = "event_time")
    private LocalDateTime eventTime;
    @XmlElement(name = "source")
    private UserResponse sourceUser;
    @XmlElement(name = "is_manual_trigger")
    private boolean isManualTrigger;

    @XmlElement(name = "updated_study")
    private StudyResponse updatedStudy;
    @XmlElement(name = "report_provider")
    private ReportProviderResponse reportProvider;
    @XmlElement(name = "capability_token")
    private CapabilitiesResponse capability;

    private NewSeriesWebhook() { /*empty*/ }

    public NewSeriesWebhook(String albumId, AlbumUser sourceUser, Series series, String instance, boolean isManualTrigger) {
        this(albumId, sourceUser, instance, isManualTrigger);
        updatedStudy = new StudyResponse(series.getStudy());
        updatedStudy.addSeries(series);
    }

    public NewSeriesWebhook(String albumId, AlbumUser sourceUser, String instance, boolean isManualTrigger) {
        this.instance = instance;
        this.albumId = albumId;
        this.eventType = WebhookType.NEW_SERIES.name();
        this.eventTime = eventTime.now();
        this.sourceUser = new UserResponse(sourceUser);
        this.isManualTrigger = isManualTrigger;

    }

    public void addSeries(Series series) {
        if(updatedStudy == null) {
            updatedStudy = new StudyResponse(series.getStudy());
        }
        updatedStudy.addSeries(series);
    }

    public void setReportProvider(ReportProvider reportProvider) {
        this.reportProvider = new ReportProviderResponse(reportProvider);
        this.reportProvider.webhookResponse();
    }

    public void setCapabilityToken(Capability capability) {
        this.capability = new CapabilitiesResponse(capability);
    }

    public boolean containSeries() {
        return updatedStudy.containSeries();
    }

    @Override
    public WebhookType getType() {
        return WebhookType.NEW_SERIES;
    }
}
