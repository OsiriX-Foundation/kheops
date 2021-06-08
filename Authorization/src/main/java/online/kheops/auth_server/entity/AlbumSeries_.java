package online.kheops.auth_server.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AlbumSeries.class)
public abstract class AlbumSeries_ {

	public static volatile SingularAttribute<AlbumSeries, Album> album;
	public static volatile SingularAttribute<AlbumSeries, Series> series;
	public static volatile SingularAttribute<AlbumSeries, Long> pk;
	public static volatile SingularAttribute<AlbumSeries, Boolean> favorite;

	public static final String ALBUM = "album";
	public static final String SERIES = "series";
	public static final String PK = "pk";
	public static final String FAVORITE = "favorite";

}

