package online.kheops.auth_server.webhook;

import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.util.ErrorResponse;

import java.util.List;

import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_FORM_PARAMETER;
import static online.kheops.auth_server.webhook.Webhooks.*;

public class WebhookPostParameters {

    private String url;
    private String name;
    private String secret;

    private boolean enabled;
    private String albumId;
    private boolean useSecret;
    private boolean newSeries;
    private boolean removeSeries;
    private boolean newUser;
    private boolean deleteAlbum;

    public WebhookPostParameters() {
        newSeries = false;
        removeSeries = false;
        newUser = false;
        deleteAlbum = false;
    }

    public WebhookPostParameters setUrl(String url)
            throws BadQueryParametersException {
        validUrl(url);
        this.url = url;
        return this;
    }

    public WebhookPostParameters setName(String name)
            throws BadQueryParametersException {
        name = name.trim();
        validName(name);
        this.name = name;
        return this;
    }

    public WebhookPostParameters setSecret(String secret)
            throws BadQueryParametersException {
        if(secret != null) {
            validSecret(secret);
            this.secret = secret;
            useSecret = true;
        } else {
            useSecret = false;
        }
        return this;
    }

    public WebhookPostParameters setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public WebhookPostParameters setEvents(List<String> events)
            throws BadQueryParametersException {
        for (String event : events) {
            if (event.equalsIgnoreCase(WebhookType.NEW_SERIES.name())) {
                newSeries = true;
            } else if (event.equalsIgnoreCase(WebhookType.REMOVE_SERIES.name())) {
                removeSeries = true;
            } else if (event.equalsIgnoreCase(WebhookType.NEW_USER.name())) {
                newUser = true;
            } else if (event.equalsIgnoreCase(WebhookType.DELETE_ALBUM.name())) {
                deleteAlbum = true;
            } else {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_FORM_PARAMETER)
                        .detail("Param 'event' contain an unknown value")
                        .build();
                throw new  BadQueryParametersException(errorResponse);
            }
        }
        return this;
    }

    public WebhookPostParameters setAlbumId(String albumId) {
        this.albumId = albumId;
        return this;
    }

    public String getUrl() { return url; }
    public String getName() { return name; }
    public String getSecret() { return secret; }
    public boolean isEnabled() { return enabled; }
    public String getAlbumId() { return albumId; }
    public boolean isUseSecret() { return useSecret; }
    public boolean isNewSeries() { return newSeries; }
    public boolean isRemoveSeries() { return removeSeries; }
    public boolean isNewUser() { return newUser; }
    public boolean isDeleteAlbum() { return deleteAlbum; }
}
