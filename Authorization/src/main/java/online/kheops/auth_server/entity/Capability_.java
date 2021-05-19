package online.kheops.auth_server.entity;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import online.kheops.auth_server.capability.ScopeType;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Capability.class)
public abstract class Capability_ {

	public static volatile SingularAttribute<Capability, Boolean> downloadPermission;
	public static volatile SingularAttribute<Capability, LocalDateTime> updatedTime;
	public static volatile SingularAttribute<Capability, Album> album;
	public static volatile SingularAttribute<Capability, String> secret;
	public static volatile SingularAttribute<Capability, String> title;
	public static volatile SingularAttribute<Capability, LocalDateTime> lastUsed;
	public static volatile SingularAttribute<Capability, LocalDateTime> notBeforeTime;
	public static volatile SingularAttribute<Capability, LocalDateTime> issuedAtTime;
	public static volatile SingularAttribute<Capability, ScopeType> scopeType;
	public static volatile SingularAttribute<Capability, LocalDateTime> expirationTime;
	public static volatile SingularAttribute<Capability, LocalDateTime> revokedTime;
	public static volatile SingularAttribute<Capability, Boolean> readPermission;
	public static volatile SingularAttribute<Capability, Boolean> appropriatePermission;
	public static volatile SingularAttribute<Capability, Long> pk;
	public static volatile SingularAttribute<Capability, String> id;
	public static volatile SingularAttribute<Capability, User> user;
	public static volatile SingularAttribute<Capability, Boolean> writePermission;

	public static final String DOWNLOAD_PERMISSION = "downloadPermission";
	public static final String UPDATED_TIME = "updatedTime";
	public static final String ALBUM = "album";
	public static final String SECRET = "secret";
	public static final String TITLE = "title";
	public static final String LAST_USED = "lastUsed";
	public static final String NOT_BEFORE_TIME = "notBeforeTime";
	public static final String ISSUED_AT_TIME = "issuedAtTime";
	public static final String SCOPE_TYPE = "scopeType";
	public static final String EXPIRATION_TIME = "expirationTime";
	public static final String REVOKED_TIME = "revokedTime";
	public static final String READ_PERMISSION = "readPermission";
	public static final String APPROPRIATE_PERMISSION = "appropriatePermission";
	public static final String PK = "pk";
	public static final String ID = "id";
	public static final String USER = "user";
	public static final String WRITE_PERMISSION = "writePermission";

}

