package online.kheops.auth_server.webhook;

import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.util.ErrorResponse;

import java.util.List;
import java.util.Optional;

import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_FORM_PARAMETER;
import static online.kheops.auth_server.webhook.Webhooks.*;

public class WebhookPatchParameters {

    public static class WebhookPatchParametersBuilder {

        private String albumId;
        private String webhookId;

        private Optional<String> url;
        private Optional<String> name;
        private Optional<String> secret;
        private Optional<Boolean> enabled;
        private Optional<Boolean> newSeries;
        private Optional<Boolean> removeSeries;
        private Optional<Boolean> deleteAlbum;
        private Optional<Boolean> newUser;

        private boolean removeSecret;

        private List<String> events;
        private List<String> addEvents;
        private List<String> removeEvents;

        public WebhookPatchParametersBuilder() {
            url = Optional.empty();
            name = Optional.empty();
            secret = Optional.empty();
            enabled = Optional.empty();
            newSeries = Optional.empty();
            newUser = Optional.empty();
            removeSeries = Optional.empty();
            deleteAlbum = Optional.empty();
            removeSecret = false;
        }

        public WebhookPatchParametersBuilder setUrl(String url)
                throws BadQueryParametersException {
            if(url != null ) {
                validUrl(url);
                this.url = Optional.of(url);
            }
            return this;
        }

        public WebhookPatchParametersBuilder setName(String name)
                throws BadQueryParametersException {
            if (name != null) {
                name = name.trim();
                validName(name);
                this.name = Optional.of(name);
            }
            return this;
        }

        public WebhookPatchParametersBuilder setSecret(String secret)
                throws BadQueryParametersException {
            if(secret != null) {
                if (secret.compareTo("") == 0) {
                    removeSecret=true;
                } else {
                    validSecret(secret);
                    this.secret = Optional.of(secret);
                }
            }
            return this;
        }

        public WebhookPatchParametersBuilder setEnabled(Boolean enabled) {
            this.enabled = Optional.ofNullable(enabled);
            return this;
        }

        public WebhookPatchParametersBuilder setEvents(List<String> events) {
            this.events = events;
            return this;
        }

        public WebhookPatchParametersBuilder setAddEvents(List<String> addEvents) {
            this.addEvents = addEvents;
            return this;
        }

        public WebhookPatchParametersBuilder setRemoveEvents(List<String> removeEvents) {
            this.removeEvents = removeEvents;
            return this;
        }


        public WebhookPatchParametersBuilder setAlbumId(String albumId) {
            this.albumId = albumId;
            return this;
        }

        public WebhookPatchParametersBuilder setWebhookId(String webhookId) {
            this.webhookId = webhookId;
            return this;
        }

        public WebhookPatchParameters build()
                throws BadQueryParametersException {

            if(!events.isEmpty() && (!addEvents.isEmpty() || !removeEvents.isEmpty())) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_FORM_PARAMETER)
                        .detail("Do not use 'event' and 'add/remove_event' at the same time")
                        .build();
                throw new BadQueryParametersException(errorResponse);
            }

            if(!events.isEmpty()) {
                newSeries = Optional.of(false);
                newUser = Optional.of(false);
                deleteAlbum = Optional.of(false);
                removeSeries = Optional.of(false);
                for (String event : events) {
                    if (event.equalsIgnoreCase(WebhookType.NEW_SERIES.name())) {
                        newSeries = Optional.of(true);
                    } else if (event.equalsIgnoreCase(WebhookType.NEW_USER.name())) {
                        newUser = Optional.of(true);
                    } else if (event.equalsIgnoreCase(WebhookType.REMOVE_SERIES.name())) {
                        removeSeries = Optional.of(true);
                    } else if (event.equalsIgnoreCase(WebhookType.DELETE_ALBUM.name())) {
                        deleteAlbum = Optional.of(true);
                    } else if (!event.equalsIgnoreCase("")) {
                        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                                .message(BAD_FORM_PARAMETER)
                                .detail("Param 'event' contain an unknown value")
                                .build();
                        throw new BadQueryParametersException(errorResponse);
                    }
                }
            } else if (!addEvents.isEmpty()) {
                for (String event : addEvents) {
                    if (event.equalsIgnoreCase(WebhookType.NEW_SERIES.name())) {
                        newSeries = Optional.of(true);
                    } else if (event.equalsIgnoreCase(WebhookType.NEW_USER.name())) {
                        newUser = Optional.of(true);
                    } else if (event.equalsIgnoreCase(WebhookType.REMOVE_SERIES.name())) {
                        removeSeries = Optional.of(true);
                    } else if (event.equalsIgnoreCase(WebhookType.DELETE_ALBUM.name())) {
                        deleteAlbum = Optional.of(true);
                    } else {
                        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                                .message(BAD_FORM_PARAMETER)
                                .detail("Param 'add_event' contain an unknown value")
                                .build();
                        throw new BadQueryParametersException(errorResponse);
                    }
                }
            } else if (!removeEvents.isEmpty()) {
                for (String event : removeEvents) {
                    if (event.equalsIgnoreCase(WebhookType.NEW_SERIES.name())) {
                        newSeries = Optional.of(false);
                    } else if (event.equalsIgnoreCase(WebhookType.NEW_USER.name())) {
                        newUser = Optional.of(false);
                    } else if (event.equalsIgnoreCase(WebhookType.REMOVE_SERIES.name())) {
                        removeSeries = Optional.of(false);
                    } else if (event.equalsIgnoreCase(WebhookType.DELETE_ALBUM.name())) {
                        deleteAlbum = Optional.of(false);
                    } else {
                        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                                .message(BAD_FORM_PARAMETER)
                                .detail("Param 'remove_event' contain an unknown value")
                                .build();
                        throw new BadQueryParametersException(errorResponse);
                    }
                }
            }
            return new WebhookPatchParameters(this);
        }

    }

    private Optional<String> url;
    private Optional<String> name;
    private Optional<String> secret;

    private Optional<Boolean> enabled;
    private String albumId;
    private String webhookId;
    private boolean removeSecret;
    private Optional<Boolean> newSeries;
    private Optional<Boolean> removeSeries;
    private Optional<Boolean> newUser;
    private Optional<Boolean> deleteAlbum;

    private WebhookPatchParameters(WebhookPatchParametersBuilder builder) {
        url = builder.url;
        name = builder.name;
        secret = builder.secret;
        enabled = builder.enabled;
        removeSecret = builder.removeSecret;
        newSeries = builder.newSeries;
        removeSeries = builder.removeSeries;
        deleteAlbum = builder.deleteAlbum;
        newUser = builder.newUser;
        albumId = builder.albumId;
        webhookId = builder.webhookId;
    }

    public Optional<String> getUrl() { return url; }
    public  Optional<String> getName() { return name; }
    public  Optional<String> getSecret() { return secret; }
    public  Optional<Boolean> isEnabled() { return enabled; }
    public  String getAlbumId() { return albumId; }
    public  String getwebhookId() { return webhookId; }
    public boolean isRemoveSecret() { return removeSecret; }
    public  Optional<Boolean> isNewSeries() { return newSeries; }
    public  Optional<Boolean> isRemoveSeries() { return removeSeries; }
    public  Optional<Boolean> isNewUser() { return newUser; }
    public  Optional<Boolean> isDeleteAlbum() { return deleteAlbum; }
}
