package online.kheops.auth_server.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSeries is a Querydsl query type for Series
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QSeries extends EntityPathBase<Series> {

    private static final long serialVersionUID = 173724709L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSeries series = new QSeries("series");

    public final DateTimePath<java.time.LocalDateTime> createdTime = createDateTime("createdTime", java.time.LocalDateTime.class);

    public final StringPath modality = createString("modality");

    public final NumberPath<Integer> numberOfSeriesRelatedInstances = createNumber("numberOfSeriesRelatedInstances", Integer.class);

    public final NumberPath<Long> pk = createNumber("pk", Long.class);

    public final BooleanPath populated = createBoolean("populated");

    public final StringPath seriesDescription = createString("seriesDescription");

    public final StringPath seriesInstanceUID = createString("seriesInstanceUID");

    public final NumberPath<Integer> seriesNumber = createNumber("seriesNumber", Integer.class);

    public final QStudy study;

    public final StringPath timezoneOffsetFromUTC = createString("timezoneOffsetFromUTC");

    public final DateTimePath<java.time.LocalDateTime> updatedTime = createDateTime("updatedTime", java.time.LocalDateTime.class);

    public final SetPath<User, QUser> users = this.<User, QUser>createSet("users", User.class, QUser.class, PathInits.DIRECT2);

    public QSeries(String variable) {
        this(Series.class, forVariable(variable), INITS);
    }

    public QSeries(Path<? extends Series> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSeries(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSeries(PathMetadata metadata, PathInits inits) {
        this(Series.class, metadata, inits);
    }

    public QSeries(Class<? extends Series> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.study = inits.isInitialized("study") ? new QStudy(forProperty("study")) : null;
    }

}

