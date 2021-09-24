package online.kheops.auth_server.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(WebhookTriggerSeries.class)
public abstract class WebhookTriggerSeries_ {

    public static volatile SingularAttribute<WebhookTriggerSeries, WebhookTrigger> webhookTrigger;
    public static volatile SingularAttribute<WebhookTriggerSeries, Series> series;
    public static volatile SingularAttribute<WebhookTriggerSeries, Long> pk;

    public static final String WEBHOOK_TRIGGER = "webhookTrigger";
    public static final String SERIES = "series";
    public static final String PK = "pk";
}