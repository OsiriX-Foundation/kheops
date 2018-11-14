package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.PairListXTotalCount;
import org.jooq.*;
import org.jooq.impl.DSL;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.core.MultivaluedMap;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static online.kheops.auth_server.album.AlbumResponses.recordToAlbumResponseForCapabilityToken;
import static online.kheops.auth_server.generated.tables.Users.USERS;
import static online.kheops.auth_server.util.JOOQTools.*;
import static online.kheops.auth_server.album.AlbumResponses.recordToAlbumResponse;
import static online.kheops.auth_server.generated.tables.Albums.ALBUMS;
import static online.kheops.auth_server.generated.tables.AlbumSeries.ALBUM_SERIES;
import static online.kheops.auth_server.generated.tables.AlbumUser.ALBUM_USER;
import static online.kheops.auth_server.generated.tables.Events.EVENTS;
import static online.kheops.auth_server.generated.tables.Series.SERIES;
import static org.jooq.impl.DSL.*;

public class AlbumQueries {

    private static final Logger LOG = Logger.getLogger(AlbumQueries.class.getName());

    private AlbumQueries() {
        throw new IllegalStateException("Utility class");
    }


    public static Album findAlbumByPk(long albumPk, EntityManager em) {
        return em.find(Album.class, albumPk);
    }

    public static AlbumUser findAlbumUserByUserAndAlbum(User user, Album album, EntityManager em ) throws NoResultException {
        return em.createQuery("SELECT au from AlbumUser au where :targetUser = au.user and :targetAlbum = au.album", AlbumUser.class)
                .setParameter("targetUser", user)
                .setParameter("targetAlbum", album)
                .getSingleResult();
    }

    public static PairListXTotalCount<AlbumResponses.AlbumResponse> findAlbumsByUserPk(AlbumParams albumParams)
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
            query.addJoin(ALBUM_USER, ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK));
            query.addJoin(USERS, ALBUM_USER.USER_FK.eq(USERS.PK));

            query.addJoin(EVENTS, JoinType.LEFT_OUTER_JOIN, EVENTS.ALBUM_FK.eq(ALBUMS.PK)
                    .and(EVENTS.EVENT_TYPE.eq("Comment"))
                    .and(EVENTS.PRIVATE_TARGET_USER_FK.isNull()
                            .or(EVENTS.PRIVATE_TARGET_USER_FK.eq(albumParams.getDBID()))
                            .or(EVENTS.USER_FK.eq(albumParams.getDBID()))));

            conditionArrayList.add(ALBUM_USER.FAVORITE.isNotNull());

            if (albumParams.getName().isPresent()) {
                conditionArrayList.add(createConditon(albumParams.getName().get(), ALBUMS.NAME, albumParams.isFuzzyMatching()));
            }

            if (albumParams.isFavorite()) {
                conditionArrayList.add(ALBUM_USER.FAVORITE.isTrue());
            }

            if (albumParams.getCreatedTime().isPresent()) {
                conditionArrayList.add(createDateCondition(albumParams.getCreatedTime().get(), ALBUMS.CREATED_TIME));
            }

            if (albumParams.getLastEventTime().isPresent()) {
                conditionArrayList.add(createDateCondition(albumParams.getLastEventTime().get(), ALBUMS.LAST_EVENT_TIME));
            }

            conditionArrayList.add(ALBUMS.PK.notEqual(USERS.INBOX_FK));
            query.addConditions(ALBUM_USER.USER_FK.eq(albumParams.getDBID()).and(ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK)));

            for (Condition c : conditionArrayList) {
                if (c != null) {
                    query.addConditions(c);
                }
            }

            if (albumParams.getLimit().isPresent()) {
                query.addLimit(albumParams.getLimit().get());
            }

            if (albumParams.getOffset().isPresent()) {
                query.addOffset(albumParams.getOffset().get());
            }

            query.addOrderBy(getOrderBy(albumParams.getOrderBy(), albumParams.isDescending(), create));

            query.addGroupBy(ALBUMS.PK, ALBUM_USER.PK);

            final Result<? extends Record> result = query.fetch();

            final List<AlbumResponses.AlbumResponse> albumResponses = new ArrayList<>();

            for(Record r : result) {
                albumResponses.add(recordToAlbumResponse(r));
            }

            final int albumTotalCount = getAlbumTotalCount(albumParams.getDBID(), conditionArrayList, connection);

            return new PairListXTotalCount<>(albumTotalCount, albumResponses);
        } catch (BadQueryParametersException e) {
            throw new BadQueryParametersException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new JOOQException("Error during request");
        }
    }

    public static AlbumResponses.AlbumResponse findAlbumByUserPkAndAlbumPk(long albumPk, long userPK)
            throws JOOQException {
        try (Connection connection = getDataSource().getConnection()) {

            final DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);
            final SelectQuery query = create.selectQuery();

            Field<Object> nbUsers = create.select(countDistinct(ALBUM_USER.PK))
                    .from(ALBUM_USER)
                    .where(ALBUM_USER.ALBUM_FK.eq(albumPk))
                    .asField();

            query.addSelect(ALBUMS.PK.as("album_pk"),
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
            query.addJoin(ALBUM_USER, ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK));

            query.addJoin(EVENTS, JoinType.LEFT_OUTER_JOIN, EVENTS.ALBUM_FK.eq(ALBUMS.PK)
                    .and(EVENTS.EVENT_TYPE.eq("Comment"))
                    .and(EVENTS.PRIVATE_TARGET_USER_FK.isNull()
                            .or(EVENTS.PRIVATE_TARGET_USER_FK.eq(userPK))
                            .or(EVENTS.USER_FK.eq(userPK))));

            query.addConditions(ALBUMS.PK.eq(albumPk));
            query.addConditions(ALBUM_USER.FAVORITE.isNotNull());
            query.addConditions(ALBUM_USER.USER_FK.eq(userPK));

            query.addGroupBy(ALBUMS.PK, ALBUM_USER.PK);

            Record18<BigDecimal, String, String, String, String, Long, Long, Long, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, String> result = (Record18<BigDecimal, String, String, String, String, Long, Long, Long, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, String>) query.fetchOne();

            return recordToAlbumResponse(result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JOOQException("Error during request");
        }
    }

    public static AlbumResponses.AlbumResponse findAlbumByAlbumPk(long albumPk)
            throws JOOQException {
        try (Connection connection = getDataSource().getConnection()) {

            final DSLContext create = DSL.using(connection, SQLDialect.MYSQL);
            final SelectQuery query = create.selectQuery();

            Field<Object> nbUsers = create.select(countDistinct(ALBUM_USER.PK))
                    .from(ALBUM_USER)
                    .where(ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK))
                    .asField();

            query.addSelect(isnull(ALBUMS.PK,"NULL").as("album_pk"),
                    isnull(ALBUMS.NAME,"NULL").as("album_name"),
                    isnull(ALBUMS.DESCRIPTION,"NULL").as("album_description"),
                    isnull(countDistinct(EVENTS.PK),"NULL").as("number_of_comments"),
                    isnull(countDistinct(SERIES.STUDY_FK),"NULL").as("number_of_studies"),
                    groupConcatDistinct(SERIES.MODALITY).as("modalities"));

            query.addFrom(ALBUMS);
            query.addJoin(ALBUM_SERIES,JoinType.LEFT_OUTER_JOIN, ALBUM_SERIES.ALBUM_FK.eq(ALBUMS.PK));
            query.addJoin(SERIES,JoinType.LEFT_OUTER_JOIN, SERIES.PK.eq(ALBUM_SERIES.SERIES_FK));
            query.addJoin(ALBUM_USER, ALBUM_USER.ALBUM_FK.eq(ALBUMS.PK));

            query.addJoin(EVENTS, JoinType.LEFT_OUTER_JOIN, EVENTS.ALBUM_FK.eq(ALBUMS.PK)
                    .and(EVENTS.EVENT_TYPE.eq("Comment"))
                    .and(EVENTS.PRIVATE_TARGET_USER_FK.isNull()));

            query.addConditions(ALBUMS.PK.eq(albumPk));;

            query.addGroupBy(ALBUMS.PK);
            Record6<BigDecimal, String, String, Long, Long, String> result = (Record6<BigDecimal, String, String, Long, Long, String>) query.fetchOne();

            return recordToAlbumResponseForCapabilityToken(result);
        } catch (Exception e) {
            throw new JOOQException("Error during request");
        }
    }

    private static int getAlbumTotalCount(long userPk, ArrayList<Condition> conditionArrayList, Connection connection) {
        final DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);
        final SelectQuery query = create.selectQuery();
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

            if (orderByParameter.compareTo("created_time") == 0) ord = ALBUMS.CREATED_TIME;
            else if (orderByParameter.compareTo("last_event_time") == 0) ord = ALBUMS.LAST_EVENT_TIME;
            else if (orderByParameter.compareTo("name") == 0) ord = ALBUMS.NAME;
            else if (orderByParameter.compareTo("number_of_users") == 0) {
                final Field<Object> fieldNumberOfUsers = create.select(countDistinct(ALBUM_USER.PK)).asField();
                ord = fieldNumberOfUsers;
            }
            else if (orderByParameter.compareTo("number_of_studies") == 0) {
                final Field<Object> fieldNumberOfStudies = create.select(countDistinct(EVENTS.PK)).asField();
                ord = fieldNumberOfStudies;
            }
            else if (orderByParameter.compareTo("number_of_comments") == 0) {
                final Field<Object> fieldNumberOfComments = create.select(countDistinct(SERIES.STUDY_FK)).asField();
                ord = fieldNumberOfComments;
            }
            else throw new BadQueryParametersException("sort: " + orderByParameter);

            return descending ? ord.desc() : ord.asc();

    }


    private static Condition createConditon(String parameter, TableField column, boolean fuzzyMatching) {

        String parameterNoStar = parameter.replace("*", "");

        if (parameterNoStar.length() == 0) {
            return null;
        }
        if ("null".equalsIgnoreCase(parameterNoStar)) {
            return column.isNull();
        } else {
            Condition condition;
            if (parameter.startsWith("*") && parameter.endsWith("*")) {
                condition = column.lower().contains(parameterNoStar.toLowerCase());
            } else if (parameter.startsWith("*")) {
                condition = column.lower().endsWith(parameterNoStar.toLowerCase());
            } else if (parameter.endsWith("*")) {
                condition = column.lower().startsWith(parameterNoStar.toLowerCase());
            } else {
                condition = column.lower().equal(parameterNoStar.toLowerCase());
            }

            if (fuzzyMatching) {
                Condition fuzzyCondition = condition("SOUNDEX('"+parameterNoStar+"') = SOUNDEX("+column.getName()+")");
                return condition.or(fuzzyCondition);
            }
            return condition;
        }
    }

    private static Condition favoriteCondition(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("favorite")) {
            if (queryParameters.get("favorite").get(0).compareTo("true") == 0) {
                return ALBUM_USER.FAVORITE.isTrue();
            } else if (queryParameters.get("favorite").get(0).compareTo("false") == 0) {
                return ALBUM_USER.FAVORITE.isFalse();
            } else {
                throw new BadQueryParametersException("favorite: " + queryParameters.get("favorite").get(0));
            }
        }
        return null;
    }
}
