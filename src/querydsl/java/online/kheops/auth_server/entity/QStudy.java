package online.kheops.auth_server.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStudy is a Querydsl query type for Study
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStudy extends EntityPathBase<Study> {

    private static final long serialVersionUID = -1240872357L;

    public static final QStudy study = new QStudy("study");

    public final StringPath accessionNumber = createString("accessionNumber");

    public final DateTimePath<java.time.LocalDateTime> createdTime = createDateTime("createdTime", java.time.LocalDateTime.class);

    public final StringPath patientBirthDate = createString("patientBirthDate");

    public final StringPath patientID = createString("patientID");

    public final StringPath patientName = createString("patientName");

    public final StringPath patientSex = createString("patientSex");

    public final NumberPath<Long> pk = createNumber("pk", Long.class);

    public final BooleanPath populated = createBoolean("populated");

    public final StringPath referringPhysicianName = createString("referringPhysicianName");

    public final SetPath<Series, QSeries> series = this.<Series, QSeries>createSet("series", Series.class, QSeries.class, PathInits.DIRECT2);

    public final StringPath studyDate = createString("studyDate");

    public final StringPath studyID = createString("studyID");

    public final StringPath studyInstanceUID = createString("studyInstanceUID");

    public final StringPath studyTime = createString("studyTime");

    public final StringPath timezoneOffsetFromUTC = createString("timezoneOffsetFromUTC");

    public final DateTimePath<java.time.LocalDateTime> updatedTime = createDateTime("updatedTime", java.time.LocalDateTime.class);

    public QStudy(String variable) {
        super(Study.class, forVariable(variable));
    }

    public QStudy(Path<? extends Study> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStudy(PathMetadata metadata) {
        super(Study.class, metadata);
    }

}

