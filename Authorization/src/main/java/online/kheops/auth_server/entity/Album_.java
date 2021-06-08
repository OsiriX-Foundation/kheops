package online.kheops.auth_server.entity;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Album.class)
public abstract class Album_ {

	public static volatile SetAttribute<Album, Webhook> webhooksNewSeriesEnabled;
	public static volatile SetAttribute<Album, Webhook> webhooksNewUserEnabled;
	public static volatile SetAttribute<Album, Capability> capabilities;
	public static volatile SingularAttribute<Album, UserPermission> userPermission;
	public static volatile SetAttribute<Album, AlbumUser> albumUser;
	public static volatile SingularAttribute<Album, String> description;
	public static volatile SetAttribute<Album, AlbumSeries> albumSeries;
	public static volatile SingularAttribute<Album, User> inboxUser;
	public static volatile SetAttribute<Album, Webhook> webhooks;
	public static volatile SingularAttribute<Album, String> name;
	public static volatile SingularAttribute<Album, LocalDateTime> createdTime;
	public static volatile SingularAttribute<Album, LocalDateTime> lastEventTime;
	public static volatile SingularAttribute<Album, Long> pk;
	public static volatile SingularAttribute<Album, String> id;

	public static final String WEBHOOKS_NEW_SERIES_ENABLED = "webhooksNewSeriesEnabled";
	public static final String WEBHOOKS_NEW_USER_ENABLED = "webhooksNewUserEnabled";
	public static final String CAPABILITIES = "capabilities";
	public static final String USER_PERMISSION = "userPermission";
	public static final String ALBUM_USER = "albumUser";
	public static final String DESCRIPTION = "description";
	public static final String ALBUM_SERIES = "albumSeries";
	public static final String INBOX_USER = "inboxUser";
	public static final String WEBHOOKS = "webhooks";
	public static final String NAME = "name";
	public static final String CREATED_TIME = "createdTime";
	public static final String LAST_EVENT_TIME = "lastEventTime";
	public static final String PK = "pk";
	public static final String ID = "id";

}

