package online.kheops.auth_server.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserPermission.class)
public abstract class UserPermission_ {

	public static volatile SingularAttribute<UserPermission, Boolean> addSeries;
	public static volatile SingularAttribute<UserPermission, Boolean> downloadSeries;
	public static volatile SingularAttribute<UserPermission, Boolean> addUser;
	public static volatile SingularAttribute<UserPermission, Boolean> writeComments;
	public static volatile SingularAttribute<UserPermission, Boolean> sendSeries;
	public static volatile SingularAttribute<UserPermission, Boolean> deleteSeries;

	public static final String ADD_SERIES = "addSeries";
	public static final String DOWNLOAD_SERIES = "downloadSeries";
	public static final String ADD_USER = "addUser";
	public static final String WRITE_COMMENTS = "writeComments";
	public static final String SEND_SERIES = "sendSeries";
	public static final String DELETE_SERIES = "deleteSeries";

}

