package online.kheops.auth_server.entity;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(WebhookAttempt.class)
public abstract class WebhookAttempt_ {

	public static volatile SingularAttribute<WebhookAttempt, WebhookTrigger> webhookTrigger;
	public static volatile SingularAttribute<WebhookAttempt, Long> pk;
	public static volatile SingularAttribute<WebhookAttempt, LocalDateTime> time;
	public static volatile SingularAttribute<WebhookAttempt, Long> attempt;
	public static volatile SingularAttribute<WebhookAttempt, Long> status;

	public static final String WEBHOOK_TRIGGER = "webhookTrigger";
	public static final String PK = "pk";
	public static final String TIME = "time";
	public static final String ATTEMPT = "attempt";
	public static final String STATUS = "status";

}

