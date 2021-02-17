package online.kheops.auth_server.inbox;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.JOOQException;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

import static online.kheops.auth_server.generated.Tables.*;
import static online.kheops.auth_server.generated.tables.AlbumSeries.ALBUM_SERIES;
import static online.kheops.auth_server.generated.tables.Albums.ALBUMS;
import static online.kheops.auth_server.generated.tables.Series.SERIES;
import static online.kheops.auth_server.generated.tables.Studies.STUDIES;
import static online.kheops.auth_server.util.JooqConstances.*;
import static org.jooq.impl.DSL.*;

public class InboxQueries {

    private InboxQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static InboxInfoResponse getInboxInfo(long userPk) throws JOOQException {

        try (Connection connection = EntityManagerListener.getConnection()) {

            final DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);
            final SelectQuery<Record> query = create.selectQuery();

            query.addSelect(countDistinct(SERIES.STUDY_FK).as(NUMBER_OF_STUDIES),
                    countDistinct(SERIES.PK).as(NUMBER_OF_SERIES),
                    sum(SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES).as(NUMBER_OF_INSTANCES),
                    groupConcatDistinct(SERIES.MODALITY).as(MODALITIES));

            query.addFrom(USERS);
            query.addJoin(ALBUMS, ALBUMS.PK.eq(USERS.INBOX_FK));
            query.addJoin(ALBUM_SERIES,JoinType.LEFT_OUTER_JOIN, ALBUM_SERIES.ALBUM_FK.eq(ALBUMS.PK));
            query.addJoin(SERIES,JoinType.LEFT_OUTER_JOIN, SERIES.PK.eq(ALBUM_SERIES.SERIES_FK));
            query.addJoin(STUDIES,JoinType.LEFT_OUTER_JOIN, STUDIES.PK.eq(SERIES.STUDY_FK));

            query.addConditions(USERS.PK.eq(userPk));
            query.addConditions(SERIES.POPULATED.isTrue().or(SERIES.POPULATED.isNull()));
            query.addConditions(STUDIES.POPULATED.isTrue().or(STUDIES.POPULATED.isNull()));

            final Record result = query.fetchOne();

            return new InboxInfoResponse(result);
        } catch (SQLException e) {
            throw new JOOQException("Error during request", e);
        }
    }



}
