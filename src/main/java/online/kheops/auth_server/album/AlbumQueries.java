package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumSeries;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.util.PairListXTotalCount;
import org.jooq.*;
import org.jooq.impl.DSL;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static online.kheops.auth_server.generated.tables.AlbumSeries.ALBUM_SERIES;
import static online.kheops.auth_server.generated.tables.AlbumUser.ALBUM_USER;
import static online.kheops.auth_server.generated.tables.Albums.ALBUMS;
import static online.kheops.auth_server.generated.tables.Events.EVENTS;
import static online.kheops.auth_server.generated.tables.Series.SERIES;
import static online.kheops.auth_server.generated.tables.Studies.STUDIES;
import static online.kheops.auth_server.generated.tables.Users.USERS;
import static online.kheops.auth_server.util.JOOQTools.createDateCondition;
import static online.kheops.auth_server.util.JOOQTools.getDataSource;
import static org.jooq.impl.DSL.*;

public class AlbumQueries {

    private AlbumQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static Album findAlbumById(String albumId, EntityManager em)
            throws AlbumNotFoundException {

        try {
            return em.createQuery("SELECT a from Album a where :albumId = a.id", Album.class)
                    .setParameter("albumId", albumId)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new AlbumNotFoundException("Album id:" + albumId + " not found", e);
        }
    }

    public static AlbumUser findAlbumUserByUserAndAlbum(User user, Album album, EntityManager em )
            throws UserNotMemberException {

        try {
            return em.createQuery("SELECT au from AlbumUser au where :targetUser = au.user and :targetAlbum = au.album and au.user.inbox <> album", AlbumUser.class)
                    .setParameter("targetUser", user)
                    .setParameter("targetAlbum", album)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new UserNotMemberException("User : " + user.getKeycloakId() + " is not a member of the album :"+album.getId(), e);
        }
    }

    public static AlbumSeries findAlbumSeriesByAlbumIDAndSeriesUID(String seriesUID, String albumID, EntityManager em)
            throws SeriesNotFoundException {
        try {
            return em.createQuery("SELECT aSeries from Album a join a.albumSeries aSeries join aSeries.series s where :seriesUID = s.seriesInstanceUID and :albumID = a.id", AlbumSeries.class)
                    .setParameter("seriesUID", seriesUID)
                    .setParameter("albumID", albumID)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new SeriesNotFoundException("SeriesInstanceUID : " + seriesUID + " not found in the album : " + albumID, e);
        }
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T t) throws BadQueryParametersException;
    }

    private static <T> void applyIfPresent(Supplier<Optional<T>> supplier, ThrowingConsumer<T> consumer) throws BadQueryParametersException {
        Optional<T> optional = supplier.get();
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        }
    }

    public static PairListXTotalCount<AlbumResponse> findAlbumsByUserPk(AlbumQueryParams albumQueryParams)
            throws JOOQException, BadQueryParametersException {
        try (Connection connection = getDataSource().getConnection()) {

            DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);
            SelectQuery<Record> query = create.selectQuery();

            ArrayList<Condition> conditionArrayList = new ArrayList<>();

            Field<Object> nbUsers = create.select(countDistinct(ALBUM_USER.PK))
                    .from(ALBUM_USER)
                    .where(ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK))
                    .asField();

            query.addSelect(ALBUMS.PK.as("album_pk"),
                    ALBUMS.ID.as("album_id"),
                    ALBUMS.NAME.as("album_name"),
                    isnull(ALBUMS.DESCRIPTION,"NULL").as("album_description"),
                    ALBUMS.CREATED_TIME.as("album_created_time"),
                    ALBUMS.LAST_EVENT_TIME.as("album_last_event_time"),
                    nbUsers.as("number_of_users"),
                    countDistinct(EVENTS.PK).as("number_of_comments"),
                    countDistinct(SERIES.STUDY_FK).as("number_of_studies"),
                    ALBUMS.ADD_USER_PERMISSION.as("add_user_permission"),
                    ALBUMS.DOWNLOAD_SERIES_PERMISSION.as("download_user_permission"),
                    ALBUMS.SEND_SERIES_PERMISSION.as("send_series_permission"),
                    ALBUMS.DELETE_SERIES_PERMISSION.as("delete_series_permision"),
                    ALBUMS.ADD_SERIES_PERMISSION.as("add_series_permission"),
                    ALBUMS.WRITE_COMMENTS_PERMISSION.as("write_comment_permission"),
                    ALBUM_USER.FAVORITE.as("favorite"),
                    ALBUM_USER.NEW_COMMENT_NOTIFICATIONS.as("new_comment_notifications"),
                    ALBUM_USER.NEW_SERIES_NOTIFICATIONS.as("new_series_notifications"),
                    ALBUM_USER.ADMIN.as("admin"),
                    groupConcatDistinct(SERIES.MODALITY).as("modalities"));

            query.addFrom(ALBUMS);
            query.addJoin(ALBUM_SERIES,JoinType.LEFT_OUTER_JOIN, ALBUM_SERIES.ALBUM_FK.eq(ALBUMS.PK));
            query.addJoin(SERIES,JoinType.LEFT_OUTER_JOIN, SERIES.PK.eq(ALBUM_SERIES.SERIES_FK));
            query.addJoin(STUDIES,JoinType.LEFT_OUTER_JOIN, STUDIES.PK.eq(SERIES.STUDY_FK));
            query.addJoin(ALBUM_USER, ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK));
            query.addJoin(USERS, ALBUM_USER.USER_FK.eq(USERS.PK));

            query.addJoin(EVENTS, JoinType.LEFT_OUTER_JOIN, EVENTS.ALBUM_FK.eq(ALBUMS.PK)
                    .and(EVENTS.EVENT_TYPE.eq("Comment"))
                    .and(EVENTS.PRIVATE_TARGET_USER_FK.isNull()
                            .or(EVENTS.PRIVATE_TARGET_USER_FK.eq(albumQueryParams.getUser().getPk()))
                            .or(EVENTS.USER_FK.eq(albumQueryParams.getUser().getPk()))));

            conditionArrayList.add(ALBUM_USER.FAVORITE.isNotNull());
            query.addConditions(SERIES.POPULATED.isTrue().or(SERIES.POPULATED.isNull()));
            query.addConditions(STUDIES.POPULATED.isTrue().or(STUDIES.POPULATED.isNull()));

            applyIfPresent(albumQueryParams::getName, filter -> conditionArrayList.add(createConditon(filter, ALBUMS.NAME, albumQueryParams.isFuzzyMatching())));
            applyIfPresent(albumQueryParams::getCreatedTime, filter -> conditionArrayList.add(createDateCondition(filter, ALBUMS.CREATED_TIME)));
            applyIfPresent(albumQueryParams::getLastEventTime, filter -> conditionArrayList.add(createDateCondition(filter, ALBUMS.LAST_EVENT_TIME)));

            if(albumQueryParams.canAddSeries()) {
                conditionArrayList.add(ALBUM_USER.ADMIN.isTrue().or(ALBUMS.ADD_SERIES_PERMISSION.isTrue()));
            }

            if(albumQueryParams.canCreateCapabilityToken()) {
                conditionArrayList.add(ALBUM_USER.ADMIN.isTrue());
            }

            if (albumQueryParams.isFavorite()) {
                conditionArrayList.add(ALBUM_USER.FAVORITE.isTrue());
            }

            conditionArrayList.add(ALBUMS.PK.notEqual(USERS.INBOX_FK));
            query.addConditions(ALBUM_USER.USER_FK.eq(albumQueryParams.getUser().getPk()).and(ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK)));

            for (Condition c : conditionArrayList) {
                if (c != null) {
                    query.addConditions(c);
                }
            }

            albumQueryParams.getLimit().ifPresent(query::addLimit);
            albumQueryParams.getOffset().ifPresent(query::addOffset);

            query.addOrderBy(getOrderBy(albumQueryParams.getOrderBy(), albumQueryParams.isDescending(), create));


            query.addGroupBy(ALBUMS.PK, ALBUM_USER.PK);

            final Result<? extends Record> result;

            final String modalityFilter = albumQueryParams.getModality().orElse(null);

            if(modalityFilter != null) {
                final Table<Record> t = query.asTable();
                final Field<String> f = (Field<String>) t.field("modalities");

                final SelectQuery<Record> q = create.selectQuery();
                q.addFrom(t);
                q.addConditions(f.containsIgnoreCase(modalityFilter));
                result = q.fetch();
            } else {
                result = query.fetch();
            }

            final List<AlbumResponse> albumResponses = new ArrayList<>();

            for(Record r : result) {
                albumResponses.add(new AlbumResponseBuilder().setAlbumFromUser(r).build());
            }

            final int albumTotalCount = getAlbumTotalCount(albumQueryParams.getUser().getPk(), conditionArrayList, connection);

            return new PairListXTotalCount<>(albumTotalCount, albumResponses);
        } catch (BadQueryParametersException e) {
            throw new BadQueryParametersException(e.getMessage());
        } catch (SQLException e) {
            throw new JOOQException("Error during request", e);
        }
    }

    public static AlbumResponse findAlbumByUserPkAndAlbumId(String albumId, long userPK)
            throws JOOQException {
        try (Connection connection = getDataSource().getConnection()) {

            final DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);
            final SelectQuery<Record> query = create.selectQuery();

            Field<Object> nbUsers = create.select(countDistinct(ALBUM_USER.PK))
                    .from(ALBUM_USER)
                    .where(ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK))
                    .asField();

            query.addSelect(ALBUMS.PK.as("album_pk"),
                    ALBUMS.ID.as("album_id"),
                    ALBUMS.NAME.as("album_name"),
                    isnull(ALBUMS.DESCRIPTION,"NULL").as("album_description"),
                    ALBUMS.CREATED_TIME.as("album_created_time"),
                    ALBUMS.LAST_EVENT_TIME.as("album_last_event_time"),
                    nbUsers.as("number_of_users"),
                    countDistinct(EVENTS.PK).as("number_of_comments"),
                    countDistinct(SERIES.STUDY_FK).as("number_of_studies"),
                    ALBUMS.ADD_USER_PERMISSION.as("add_user_permission"),
                    ALBUMS.DOWNLOAD_SERIES_PERMISSION.as("download_user_permission"),
                    ALBUMS.SEND_SERIES_PERMISSION.as("send_series_permission"),
                    ALBUMS.DELETE_SERIES_PERMISSION.as("delete_series_permision"),
                    ALBUMS.ADD_SERIES_PERMISSION.as("add_series_permission"),
                    ALBUMS.WRITE_COMMENTS_PERMISSION.as("write_comment_permission"),
                    ALBUM_USER.FAVORITE.as("favorite"),
                    ALBUM_USER.NEW_COMMENT_NOTIFICATIONS.as("new_comment_notifications"),
                    ALBUM_USER.NEW_SERIES_NOTIFICATIONS.as("new_series_notifications"),
                    ALBUM_USER.ADMIN.as("admin"),
                    groupConcatDistinct(SERIES.MODALITY).as("modalities"));

            query.addFrom(ALBUMS);
            query.addJoin(ALBUM_SERIES,JoinType.LEFT_OUTER_JOIN, ALBUM_SERIES.ALBUM_FK.eq(ALBUMS.PK));
            query.addJoin(SERIES,JoinType.LEFT_OUTER_JOIN, SERIES.PK.eq(ALBUM_SERIES.SERIES_FK));
            query.addJoin(STUDIES,JoinType.LEFT_OUTER_JOIN, STUDIES.PK.eq(SERIES.STUDY_FK));
            query.addJoin(ALBUM_USER, ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK));

            query.addJoin(EVENTS, JoinType.LEFT_OUTER_JOIN, EVENTS.ALBUM_FK.eq(ALBUMS.PK)
                    .and(EVENTS.EVENT_TYPE.eq("Comment"))
                    .and(EVENTS.PRIVATE_TARGET_USER_FK.isNull()
                            .or(EVENTS.PRIVATE_TARGET_USER_FK.eq(userPK))
                            .or(EVENTS.USER_FK.eq(userPK))));

            query.addConditions(ALBUMS.ID.eq(albumId));
            query.addConditions(ALBUM_USER.FAVORITE.isNotNull());
            query.addConditions(ALBUM_USER.USER_FK.eq(userPK));
            query.addConditions(SERIES.POPULATED.isTrue().or(SERIES.POPULATED.isNull()));
            query.addConditions(STUDIES.POPULATED.isTrue().or(STUDIES.POPULATED.isNull()));

            query.addGroupBy(ALBUMS.PK, ALBUM_USER.PK);

            Record result = query.fetchOne();

            return new AlbumResponseBuilder().setAlbumFromUser(result).build();
        } catch (SQLException e) {
            throw new JOOQException("Error during request", e);
        }
    }

    public static AlbumResponse findAlbumByAlbumId(String albumId)
            throws JOOQException {
        try (Connection connection = getDataSource().getConnection()) {

            final DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);
            final SelectQuery<Record> query = create.selectQuery();

            query.addSelect(ALBUMS.PK.as("album_pk"),
                    ALBUMS.ID.as("album_id"),
                    ALBUMS.NAME.as("album_name"),
                    isnull(ALBUMS.DESCRIPTION,"NULL").as("album_description"),
                    countDistinct(EVENTS.PK).as("number_of_comments"),
                    countDistinct(SERIES.STUDY_FK).as("number_of_studies"),
                    groupConcatDistinct(SERIES.MODALITY).as("modalities"));

            query.addFrom(ALBUMS);
            query.addJoin(ALBUM_SERIES,JoinType.LEFT_OUTER_JOIN, ALBUM_SERIES.ALBUM_FK.eq(ALBUMS.PK));
            query.addJoin(SERIES,JoinType.LEFT_OUTER_JOIN, SERIES.PK.eq(ALBUM_SERIES.SERIES_FK));
            query.addJoin(STUDIES,JoinType.LEFT_OUTER_JOIN, STUDIES.PK.eq(SERIES.STUDY_FK));
            query.addJoin(ALBUM_USER, ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK));

            query.addJoin(EVENTS, JoinType.LEFT_OUTER_JOIN, EVENTS.ALBUM_FK.eq(ALBUMS.PK)
                    .and(EVENTS.EVENT_TYPE.eq("Comment"))
                    .and(EVENTS.PRIVATE_TARGET_USER_FK.isNull()));

            query.addConditions(ALBUMS.ID.eq(albumId));
            query.addConditions(SERIES.POPULATED.isTrue());
            query.addConditions(STUDIES.POPULATED.isTrue());

            query.addGroupBy(ALBUMS.PK, ALBUM_USER.PK);

            Record result = query.fetchOne();

            return new AlbumResponseBuilder().setAlbumFromCapabilityToken(result).build();
        } catch (Exception e) {
            throw new JOOQException("Error during request");
        }
    }

    private static int getAlbumTotalCount(long userPk, ArrayList<Condition> conditionArrayList, Connection connection) {
        final DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);
        final SelectQuery<Record> query = create.selectQuery();
        query.addSelect(countDistinct(ALBUMS.PK));
        query.addFrom(ALBUMS);
        query.addJoin(ALBUM_USER, ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK).and(ALBUM_USER.USER_FK.eq(userPk)));
        query.addJoin(USERS, USERS.PK.eq(ALBUM_USER.USER_FK));
        for (Condition c : conditionArrayList) {
            if (c != null) {
                query.addConditions(c);
            }
        }

        return (int) query.fetchOne().getValue(0);
    }

    private static SortField getOrderBy(String orderByParameter, boolean descending, DSLContext create) throws  BadQueryParametersException{

            Field ord;

            if (orderByParameter.equals("created_time")) ord = ALBUMS.CREATED_TIME;
            else if (orderByParameter.equals("last_event_time")) ord = ALBUMS.LAST_EVENT_TIME;
            else if (orderByParameter.equals("name")) ord = ALBUMS.NAME;
            else if (orderByParameter.equals("number_of_users")) {
                ord = field("number_of_users");
            }
            else if (orderByParameter.equals("number_of_studies")) {
                ord = field("number_of_studies");
            }
            else if (orderByParameter.equals("number_of_comments")) {
                ord = field("number_of_comments");
            }
            else throw new BadQueryParametersException("sort: " + orderByParameter);

            return descending ? ord.desc() : ord.asc();

    }


    private static Condition createConditon(String parameter, TableField<? extends Record, String> column, boolean fuzzyMatching) {

        String parameterNoStar = parameter.replace("*", "");

        if (parameterNoStar.length() == 0) {
            return null;
        }
        if ("null".equalsIgnoreCase(parameterNoStar)) {
            return column.isNull();
        } else {
            Condition condition;
            if (parameter.startsWith("*") && parameter.endsWith("*")) {
                condition = column.containsIgnoreCase(parameterNoStar);
            } else if (parameter.startsWith("*")) {
                condition = column.lower().endsWith(parameterNoStar.toLowerCase());
            } else if (parameter.endsWith("*")) {
                condition = column.lower().startsWith(parameterNoStar.toLowerCase());
            } else {
                condition = column.equalIgnoreCase(parameterNoStar);
            }

            if (fuzzyMatching) {
                Condition fuzzyCondition = condition("SOUNDEX('"+parameterNoStar+"') = SOUNDEX("+column.getName()+")");
                return condition.or(fuzzyCondition);
            }
            return condition;
        }
    }
}
