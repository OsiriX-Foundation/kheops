package online.kheops.auth_server.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AlbumUser.class)
public abstract class AlbumUser_ {

	public static volatile SingularAttribute<AlbumUser, Album> album;
	public static volatile SingularAttribute<AlbumUser, Boolean> newSeriesNotifications;
	public static volatile SingularAttribute<AlbumUser, Boolean> admin;
	public static volatile SingularAttribute<AlbumUser, Long> pk;
	public static volatile SingularAttribute<AlbumUser, Boolean> newCommentNotifications;
	public static volatile SingularAttribute<AlbumUser, Boolean> favorite;
	public static volatile SingularAttribute<AlbumUser, User> user;

	public static final String ALBUM = "album";
	public static final String NEW_SERIES_NOTIFICATIONS = "newSeriesNotifications";
	public static final String ADMIN = "admin";
	public static final String PK = "pk";
	public static final String NEW_COMMENT_NOTIFICATIONS = "newCommentNotifications";
	public static final String FAVORITE = "favorite";
	public static final String USER = "user";

}

