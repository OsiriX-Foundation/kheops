package online.kheops.auth_server.webhook;

public class SignedEntity {

    private String secret;
    private WebhookResult entity;


    public SignedEntity(WebhookResult entity, String secret) {
        this.secret = secret;
        this.entity = entity;
    }

    public String getSecret() { return secret; }

    public WebhookResult getEntity() { return entity; }
}
