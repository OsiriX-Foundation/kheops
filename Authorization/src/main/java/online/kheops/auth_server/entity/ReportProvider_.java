package online.kheops.auth_server.entity;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ReportProvider.class)
public abstract class ReportProvider_ {

	public static volatile SingularAttribute<ReportProvider, String> clientId;
	public static volatile SingularAttribute<ReportProvider, LocalDateTime> creationTime;
	public static volatile SingularAttribute<ReportProvider, Boolean> removed;
	public static volatile SingularAttribute<ReportProvider, Album> album;
	public static volatile SingularAttribute<ReportProvider, String> name;
	public static volatile SingularAttribute<ReportProvider, Long> pk;
	public static volatile SingularAttribute<ReportProvider, String> url;

	public static final String CLIENT_ID = "clientId";
	public static final String CREATION_TIME = "creationTime";
	public static final String REMOVED = "removed";
	public static final String ALBUM = "album";
	public static final String NAME = "name";
	public static final String PK = "pk";
	public static final String URL = "url";

}

