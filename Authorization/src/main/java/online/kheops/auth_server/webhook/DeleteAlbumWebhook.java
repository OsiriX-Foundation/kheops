package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.user.UserResponse;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

public class DeleteAlbumWebhook implements WebhookResult{

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


    private DeleteAlbumWebhook() { /*empty*/ }

    public DeleteAlbumWebhook(String albumId, AlbumUser sourceUser, String instance, boolean isManualTrigger) {
        this.instance = instance;
        this.albumId = albumId;
        this.eventTime = LocalDateTime.now();
        this.sourceUser = new UserResponse(sourceUser);
        this.isManualTrigger = isManualTrigger;
    }

    public void setCapabilityToken(Capability capability) { sourceUser.setCapabilityToken(capability); }

    @Override
    public WebhookType getType() {
        return WebhookType.DELETE_ALBUM;
    }
}