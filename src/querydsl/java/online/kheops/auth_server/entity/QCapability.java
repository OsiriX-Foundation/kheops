package online.kheops.auth_server.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCapability is a Querydsl query type for Capability
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCapability extends EntityPathBase<Capability> {

    private static final long serialVersionUID = -389778490L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCapability capability = new QCapability("capability");

    public final DateTimePath<java.time.LocalDateTime> createdTime = createDateTime("createdTime", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final DateTimePath<java.time.LocalDateTime> expiration = createDateTime("expiration", java.time.LocalDateTime.class);

    public final NumberPath<Long> pk = createNumber("pk", Long.class);

    public final DateTimePath<java.time.LocalDateTime> revokedTime = createDateTime("revokedTime", java.time.LocalDateTime.class);

    public final StringPath secret = createString("secret");

    public final DateTimePath<java.time.LocalDateTime> updatedTime = createDateTime("updatedTime", java.time.LocalDateTime.class);

    public final QUser user;

    public QCapability(String variable) {
        this(Capability.class, forVariable(variable), INITS);
    }

    public QCapability(Path<? extends Capability> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCapability(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCapability(PathMetadata metadata, PathInits inits) {
        this(Capability.class, metadata, inits);
    }

    public QCapability(Class<? extends Capability> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

