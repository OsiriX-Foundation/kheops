package online.kheops.auth_server.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(User.class)
public abstract class User_ {

	public static volatile SingularAttribute<User, String> sub;
	public static volatile SetAttribute<User, Capability> capabilities;
	public static volatile SetAttribute<User, Mutation> mutations;
	public static volatile SetAttribute<User, AlbumUser> albumUser;
	public static volatile SetAttribute<User, Webhook> webhooks;
	public static volatile SingularAttribute<User, String> name;
	public static volatile SingularAttribute<User, Long> pk;
	public static volatile SetAttribute<User, Event> privateEvent;
	public static volatile SingularAttribute<User, Album> inbox;
	public static volatile SingularAttribute<User, String> email;
	public static volatile SetAttribute<User, Event> events;

	public static final String SUB = "sub";
	public static final String CAPABILITIES = "capabilities";
	public static final String MUTATIONS = "mutations";
	public static final String ALBUM_USER = "albumUser";
	public static final String WEBHOOKS = "webhooks";
	public static final String NAME = "name";
	public static final String PK = "pk";
	public static final String PRIVATE_EVENT = "privateEvent";
	public static final String INBOX = "inbox";
	public static final String EMAIL = "email";
	public static final String EVENTS = "events";

}

