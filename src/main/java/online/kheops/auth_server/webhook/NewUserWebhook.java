package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.user.UserResponse;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

public class NewUserWebhook {

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

    @XmlElement(name = "new_user")
    private UserResponse newUser;

    private NewUserWebhook() { /*empty*/ }


    public NewUserWebhook(String albumId, AlbumUser sourceUser, AlbumUser newUser, String instance, boolean isManualTrigger) {
        this.instance = instance;
        this.albumId = albumId;
        this.eventType = WebhookTypes.NEW_USER.name();
        this.eventTime = eventTime.now();
        this.newUser = new UserResponse(newUser);
        this.sourceUser = new UserResponse(sourceUser);
        this.isManualTrigger = isManualTrigger;
    }
}
