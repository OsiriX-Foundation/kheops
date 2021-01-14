package online.kheops.auth_server.entity;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Event.class)
public abstract class Event_ {

	public static volatile SingularAttribute<Event, Study> study;
	public static volatile SingularAttribute<Event, Album> album;
	public static volatile SetAttribute<Event, Series> series;
	public static volatile SingularAttribute<Event, LocalDateTime> eventTime;
	public static volatile SingularAttribute<Event, Long> pk;
	public static volatile SingularAttribute<Event, User> user;
	public static volatile SingularAttribute<Event, User> privateTargetUser;

	public static final String STUDY = "study";
	public static final String ALBUM = "album";
	public static final String SERIES = "series";
	public static final String EVENT_TIME = "eventTime";
	public static final String PK = "pk";
	public static final String USER = "user";
	public static final String PRIVATE_TARGET_USER = "privateTargetUser";

}

