package online.kheops.auth_server.token;

import online.kheops.auth_server.accesstoken.AccessToken;

import javax.json.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class IntrospectResponse {
    private static final String INACTIVE_JSON = new IntrospectResponse().toJson();

    private boolean active;
    private String issuer;
    private List<String> audience;
    private String subject;
    private String scope;
    private String authorizedParty;
    private String actingParty;
    private String clientId;
    private String capabilityTokenId;
    private Long expiresAt;
    private Long issuedAt;
    private Long notBefore;
    private List<String> studyUIDs;

    private String redirectUri;
    private String albumId;

    private void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    private void setAudience(List<String> audience) {
        this.audience = audience;
    }

    private void setSubject(String subject) {
        this.subject = subject;
    }

    private void setScope(String scope) {
        this.scope = scope;
    }

    private void setAuthorizedParty(String authorizedParty) {
        this.authorizedParty = authorizedParty;
    }

    private void setActingParty(String actingParty) {
        this.actingParty = actingParty;
    }

    private void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setCapabilityTokenId(String capabilityTokenId) {
        this.capabilityTokenId = capabilityTokenId;
    }

    private void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    private void setIssuedAt(Long issuedAt) {
        this.issuedAt = issuedAt;
    }

    private void setNotBefore(Long notBefore) {
        this.notBefore = notBefore;
    }

    private void setStudyUIDs(List<String> studyUIDs) {
        this.studyUIDs = studyUIDs;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    private IntrospectResponse() {}

    public static String getInactiveResponseJson() {
        return INACTIVE_JSON;
    }

    public static IntrospectResponse from(AccessToken accessToken) {

        final IntrospectResponse response = new IntrospectResponse();

        response.active = true;
        response.setSubject(accessToken.getSubject());

        accessToken.getIssuer().ifPresent(response::setIssuer);
        accessToken.getAudience().ifPresent(response::setAudience);
        accessToken.getActingParty().ifPresent(response::setActingParty);
        accessToken.getScope().ifPresent(response::setScope);
        accessToken.getClientId().ifPresent(response::setClientId);
        accessToken.getCapabilityTokenId().ifPresent(response::setCapabilityTokenId);
        accessToken.getAuthorizedParty().ifPresent(response::setAuthorizedParty);
        accessToken.getExpiresAt().ifPresent(instant -> response.setExpiresAt(instant.getEpochSecond()));
        accessToken.getIssuedAt().ifPresent(instant -> response.setIssuedAt(instant.getEpochSecond()));
        accessToken.getNotBefore().ifPresent(instant -> response.setNotBefore(instant.getEpochSecond()));
        accessToken.getStudyUIDs().ifPresent(response::setStudyUIDs);

        return response;
    }

    public String toJson() {

        JsonObjectBuilder objectBuilder = Json.createObjectBuilder().add("active", active);

        if (active) {
            if (issuer != null) {
                objectBuilder.add("iss", issuer);
            }
            if (subject != null) {
                objectBuilder.add("sub", subject);
            }
            if (scope != null) {
                objectBuilder.add("scope", scope);
            }
            if (authorizedParty != null) {
                objectBuilder.add("azp", authorizedParty);
            }
            if (clientId != null) {
                objectBuilder.add("client_id", clientId);
            }
            if (capabilityTokenId != null) {
                objectBuilder.add("cap_token", capabilityTokenId);
            }
            if (expiresAt != null) {
                objectBuilder.add("exp", expiresAt);
            }
            if (issuedAt != null) {
                objectBuilder.add("iat", issuedAt);
            }
            if (notBefore != null) {
                objectBuilder.add("nbf", notBefore);
            }
            if (redirectUri != null) {
                objectBuilder.add("redirect_uri", redirectUri);
            }
            if (albumId != null) {
                objectBuilder.add("album_id", albumId);
            }

            if (actingParty != null) {
                objectBuilder.add("act", Json.createObjectBuilder().add("sub", actingParty));
            }

            if (audience != null && !audience.isEmpty()) {
                if (audience.size() == 1) {
                    objectBuilder.add("aud", audience.get(0));
                } else {
                    JsonArrayBuilder audienceBuilder = Json.createArrayBuilder();
                    audience.forEach(audienceBuilder::add);
                    objectBuilder.add("aud", audienceBuilder);
                }
            }
            if (studyUIDs != null && !studyUIDs.isEmpty()) {
                if (studyUIDs.size() == 1) {
                    objectBuilder.add("studyUID", studyUIDs.get(0));
                } else {
                    JsonArrayBuilder studiesBuilder = Json.createArrayBuilder();
                    studyUIDs.forEach(studiesBuilder::add);
                    objectBuilder.add("studyUID", studiesBuilder);
                }
            }
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JsonWriter jsonWriter = Json.createWriter(outputStream)) {
            jsonWriter.write(objectBuilder.build());

            return outputStream.toString(UTF_8.name());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
