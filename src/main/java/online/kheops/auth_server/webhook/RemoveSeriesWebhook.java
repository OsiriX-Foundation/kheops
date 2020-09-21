package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.report_provider.ReportProviderResponse;
import online.kheops.auth_server.study.StudyResponse;
import online.kheops.auth_server.user.UserResponse;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

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

    public RemoveSeriesWebhook(String albumId, AlbumUser sourceUser, Series series, String instance, boolean isManualTrigger) {
        this(albumId, sourceUser, instance, isManualTrigger);
        removedStudy = new StudyResponse(series.getStudy(), instance);
        removedStudy.addSeries(series);
        removedStudy.hideRetrieveUrl();
    }

    public RemoveSeriesWebhook(String albumId, AlbumUser sourceUser, String instance, boolean isManualTrigger) {
        this.instance = instance;
        this.albumId = albumId;
        this.eventTime = LocalDateTime.now();
        this.sourceUser = new UserResponse(sourceUser);
        this.isManualTrigger = isManualTrigger;
    }

    public void addSeries(Series series) {
        if(removedStudy == null) {
            removedStudy = new StudyResponse(series.getStudy(), instance);
        }
        removedStudy.addSeries(series);
        removedStudy.hideRetrieveUrl();
    }

    public void setRemoveAllSeries(boolean removeAllSeries) { this.removeAllSeries = removeAllSeries; }

    public void setReportProvider(ReportProvider reportProvider) { sourceUser.setReportProvider(reportProvider, ReportProviderResponse.Type.WEBHOOK); }

    public void setCapabilityToken(Capability capability) { sourceUser.setCapabilityToken(capability); }

    @Override
    public WebhookType getType() {
        return WebhookType.REMOVE_SERIES;
    }
}