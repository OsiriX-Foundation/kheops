package online.kheops.auth_server.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(WebhookTrigger.class)
public abstract class WebhookTrigger_ {

	public static volatile SingularAttribute<WebhookTrigger, Webhook> webhook;
	public static volatile SingularAttribute<WebhookTrigger, Boolean> removeSeries;
	public static volatile SetAttribute<WebhookTrigger, Series> series;
	public static volatile SingularAttribute<WebhookTrigger, Boolean> newUser;
	public static volatile SingularAttribute<WebhookTrigger, Long> pk;
	public static volatile SingularAttribute<WebhookTrigger, String> id;
	public static volatile SingularAttribute<WebhookTrigger, Boolean> newSeries;
	public static volatile SingularAttribute<WebhookTrigger, User> user;
	public static volatile SingularAttribute<WebhookTrigger, Boolean> isManualTrigger;
	public static volatile SetAttribute<WebhookTrigger, WebhookAttempt> webhookAttempts;

	public static final String WEBHOOK = "webhook";
	public static final String REMOVE_SERIES = "removeSeries";
	public static final String SERIES = "series";
	public static final String NEW_USER = "newUser";
	public static final String PK = "pk";
	public static final String ID = "id";
	public static final String NEW_SERIES = "newSeries";
	public static final String USER = "user";
	public static final String IS_MANUAL_TRIGGER = "isManualTrigger";
	public static final String WEBHOOK_ATTEMPTS = "webhookAttempts";

}

