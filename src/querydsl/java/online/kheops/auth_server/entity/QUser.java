package online.kheops.auth_server.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -1702537991L;

    public static final QUser user = new QUser("user");

    public final SetPath<Capability, QCapability> capabilities = this.<Capability, QCapability>createSet("capabilities", Capability.class, QCapability.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> createdTime = createDateTime("createdTime", java.time.LocalDateTime.class);

    public final StringPath googleEmail = createString("googleEmail");

    public final StringPath googleId = createString("googleId");

    public final NumberPath<Long> pk = createNumber("pk", Long.class);

    public final SetPath<Series, QSeries> series = this.<Series, QSeries>createSet("series", Series.class, QSeries.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updatedTime = createDateTime("updatedTime", java.time.LocalDateTime.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

