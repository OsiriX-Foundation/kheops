package online.kheops.auth_server.entity;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Webhook.class)
public abstract class Webhook_ {

	public static volatile SingularAttribute<Webhook, LocalDateTime> creationTime;
	public static volatile SingularAttribute<Webhook, Album> album;
	public static volatile SingularAttribute<Webhook, String> secret;
	public static volatile SingularAttribute<Webhook, String> url;
	public static volatile SingularAttribute<Webhook, Boolean> enabled;
	public static volatile SetAttribute<Webhook, WebhookTrigger> webhookTriggers;
	public static volatile SingularAttribute<Webhook, Boolean> deleteAlbum;
	public static volatile SingularAttribute<Webhook, Boolean> removeSeries;
	public static volatile SingularAttribute<Webhook, Boolean> newUser;
	public static volatile SingularAttribute<Webhook, String> name;
	public static volatile SingularAttribute<Webhook, Long> pk;
	public static volatile SingularAttribute<Webhook, String> id;
	public static volatile SingularAttribute<Webhook, Boolean> newSeries;
	public static volatile SingularAttribute<Webhook, User> user;

	public static final String CREATION_TIME = "creationTime";
	public static final String ALBUM = "album";
	public static final String SECRET = "secret";
	public static final String URL = "url";
	public static final String ENABLED = "enabled";
	public static final String WEBHOOK_TRIGGERS = "webhookTriggers";
	public static final String DELETE_ALBUM = "deleteAlbum";
	public static final String REMOVE_SERIES = "removeSeries";
	public static final String NEW_USER = "newUser";
	public static final String NAME = "name";
	public static final String PK = "pk";
	public static final String ID = "id";
	public static final String NEW_SERIES = "newSeries";
	public static final String USER = "user";

}

