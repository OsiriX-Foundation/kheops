package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.series.SeriesResponse;
import online.kheops.auth_server.user.UserResponse;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class NewSeriesWebhook {

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

    @XmlElement(name = "new_series")
    private ArrayList<SeriesResponse> newSeries;

    private NewSeriesWebhook() { /*empty*/ }

    public NewSeriesWebhook(String albumId, AlbumUser sourceUser, Series series, String instance, boolean isManualTrigger) {
        this.instance = instance;
        this.albumId = albumId;
        this.eventType = WebhookTypes.NEW_SERIES.name();
        this.eventTime = eventTime.now();
        this.sourceUser = new UserResponse(sourceUser);
        this.isManualTrigger = isManualTrigger;
        this.newSeries = new ArrayList<>();
        this.newSeries.add(new SeriesResponse(series));
    }

    public NewSeriesWebhook(String albumId, AlbumUser sourceUser, String instance, boolean isManualTrigger) {
        this.instance = instance;
        this.albumId = albumId;
        this.eventType = WebhookTypes.NEW_SERIES.name();
        this.eventTime = eventTime.now();
        this.sourceUser = new UserResponse(sourceUser);
        this.isManualTrigger = isManualTrigger;
        this.newSeries = new ArrayList<>();
    }

    public void addSeries(Series series) {
        this.newSeries.add(new SeriesResponse(series));
    }


}
