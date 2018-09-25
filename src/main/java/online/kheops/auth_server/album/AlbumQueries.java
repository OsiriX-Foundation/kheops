package online.kheops.auth_server.album;

import com.mchange.v2.c3p0.C3P0Registry;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.util.Consts;
import org.jooq.*;
import org.jooq.impl.DSL;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;
import javax.ws.rs.core.MultivaluedMap;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.generated.tables.Users.USERS;
import static online.kheops.auth_server.util.JOOQTools.*;
import static online.kheops.auth_server.album.AlbumResponses.recordToAlbumResponse;
import static online.kheops.auth_server.generated.tables.Album.ALBUM;
import static online.kheops.auth_server.generated.tables.AlbumSeries.ALBUM_SERIES;
import static online.kheops.auth_server.generated.tables.AlbumUser.ALBUM_USER;
import static online.kheops.auth_server.generated.tables.Event.EVENT;
import static online.kheops.auth_server.generated.tables.Series.SERIES;
import static org.jooq.impl.DSL.*;

public class AlbumQueries {

    private static final Logger LOG = Logger.getLogger(AlbumQueries.class.getName());

    private AlbumQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static List<AlbumUser> findAlbumsUserByUser(User targetUser, EntityManager em) {
        TypedQuery<AlbumUser> query = em.createQuery("SELECT a from AlbumUser a where :targetUser = a.user", AlbumUser.class);
        query.setParameter("targetUser", targetUser);
        return query.getResultList();
    }

    public static Album findAlbumByPk(long albumPk, EntityManager em) throws NoResultException{
        TypedQuery<Album> query = em.createQuery("SELECT a from Album a where :albumPk = a.pk", Album.class);
        query.setParameter("albumPk", albumPk);
        return query.getSingleResult();
    }

    public static AlbumUser findAlbumUserByUserAndAlbum(User user, Album album, EntityManager em ) throws NoResultException {
        TypedQuery<AlbumUser> query = em.createQuery("SELECT a from AlbumUser a where :targetUser = a.user and :targetAlbum = a.album", AlbumUser.class);
        query.setParameter("targetUser", user);
        query.setParameter("targetAlbum", album);
        return query.getSingleResult();
    }

    private static DataSource getDataSource() {
        Iterator iterator = C3P0Registry.getPooledDataSources().iterator();

        if (!iterator.hasNext()) {
            throw new RuntimeException("No C3P0 DataSource available");
        }
        DataSource dataSource = (DataSource) iterator.next();
        if (iterator.hasNext()) {
            LOG.log(Level.SEVERE, "More than one C3P0 Datasource present, picked the first one");
        }

        return dataSource;
    }

    public static PairAlbumsTotalAlbum findAlbumsByUserPk(long userPK, MultivaluedMap<String, String> queryParameters)
            throws JOOQException, BadQueryParametersException {
        try (Connection connection = getDataSource().getConnection()) {

            DSLContext create = DSL.using(connection, SQLDialect.MYSQL);
            SelectQuery query = create.selectQuery();

            ArrayList<Condition> conditionArrayList = new ArrayList<>();

            query.addSelect(isnull(ALBUM.PK,"NULL").as("album_pk"),
                    isnull(ALBUM.NAME,"NULL").as("album_name"),
                    isnull(ALBUM.DESCRIPTION,"NULL").as("album_description"),
                    isnull(ALBUM.CREATED_TIME,"NULL").as("album_created_time"),
                    isnull(ALBUM.LAST_EVENT_TIME,"NULL").as("album_last_event_time"),
                    isnull(countDistinct(ALBUM_USER.PK),"NULL").as("number_of_users"),
                    isnull(countDistinct(EVENT.PK),"NULL").as("number_of_comments"),
                    isnull(countDistinct(SERIES.STUDY_FK),"NULL").as("number_of_studies"),
                    isnull(ALBUM.ADD_USER_PERMISSION,"NULL").as("add_user_permission"),
                    isnull(ALBUM.DOWNLOAD_SERIES_PERMISSION,"NULL").as("download_user_permission"),
                    isnull(ALBUM.SEND_SERIES_PERMISSION,"NULL").as("send_series_permission"),
                    isnull(ALBUM.DELETE_SERIES_PERMISSION,"NULL").as("delete_series_permision"),
                    isnull(ALBUM.ADD_SERIES_PERMISSION,"NULL").as("add_series_permission"),
                    isnull(ALBUM.WRITE_COMMENTS_PERMISSION,"NULL").as("write_comment_permission"),
                    isnull(ALBUM_USER.FAVORITE,"NULL").as("favorite"),
                    isnull(ALBUM_USER.NEW_COMMENT_NOTIFICATIONS,"NULL").as("new_comment_notifications"),
                    isnull(ALBUM_USER.NEW_SERIES_NOTIFICATIONS,"NULL").as("new_series_notifications"),
                    groupConcatDistinct(SERIES.MODALITY).as("modalities"));

            query.addFrom(ALBUM);
            query.addJoin(ALBUM_SERIES,JoinType.LEFT_OUTER_JOIN, ALBUM_SERIES.ALBUM_FK.eq(ALBUM.PK));
            query.addJoin(SERIES,JoinType.LEFT_OUTER_JOIN, SERIES.PK.eq(ALBUM_SERIES.SERIES_FK));
            query.addJoin(ALBUM_USER, ALBUM_USER.ALBUM_FK.eq(ALBUM.PK));
            query.addJoin(USERS, ALBUM_USER.USER_FK.eq(USERS.PK));

            query.addJoin(EVENT, JoinType.LEFT_OUTER_JOIN, EVENT.ALBUM_FK.eq(ALBUM.PK)
                    .and(EVENT.EVENT_TYPE.eq("Comment"))
                    .and(EVENT.PRIVATE_TARGET_USER_FK.isNull()
                            .or(EVENT.PRIVATE_TARGET_USER_FK.eq(userPK))
                            .or(EVENT.USER_FK.eq(userPK))));

            conditionArrayList.add(ALBUM_USER.FAVORITE.isNotNull());
            conditionArrayList.add(createConditon(queryParameters, "name", ALBUM.NAME));
            conditionArrayList.add(favoriteCondition(queryParameters));
            conditionArrayList.add(createDateCondition(queryParameters, "created_time", ALBUM.CREATED_TIME));
            conditionArrayList.add(createDateCondition(queryParameters, "last_event_time", ALBUM.LAST_EVENT_TIME));
            conditionArrayList.add(ALBUM.PK.notEqual(USERS.INBOX_FK));
            query.addConditions(ALBUM_USER.USER_FK.eq(userPK).and(ALBUM_USER.ALBUM_FK.eq(ALBUM.PK)));

            for (Condition c : conditionArrayList) {
                if (c != null) {
                    query.addConditions(c);
                }
            }

            if (queryParameters.containsKey(Consts.QUERY_PARAMETER_LIMIT)) {
                query.addLimit(getLimit(queryParameters));
            }

            if (queryParameters.containsKey(Consts.QUERY_PARAMETER_OFFSET)) {
                query.addOffset(getOffset(queryParameters));
            }

            query.addOrderBy(getOrderBy(queryParameters, create));

            query.addGroupBy(ALBUM.PK);

            Result<Record18<BigDecimal, String, String, String, String, Long, Long, Long, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, String>> result = query.fetch();

            final List<AlbumResponses.AlbumResponse> albumResponses = new ArrayList<>();

            for(Record r : result) {
                albumResponses.add(recordToAlbumResponse(r));
            }

            final int albumTotalCount = getAlbumTotalCount(userPK, conditionArrayList, connection);

            return new PairAlbumsTotalAlbum(albumTotalCount, albumResponses);
        } catch (BadQueryParametersException e) {
            throw new BadQueryParametersException(e.getMessage());
        } catch (Exception e) {
            throw new JOOQException("Error during request");
        }
    }

    public static AlbumResponses.AlbumResponse findAlbumByUserPkAndAlbumPk(long albumPk, long userPK)
            throws JOOQException {
        try (Connection connection = getDataSource().getConnection()) {

            final DSLContext create = DSL.using(connection, SQLDialect.MYSQL);
            final SelectQuery query = create.selectQuery();

            query.addSelect(isnull(ALBUM.PK,"NULL").as("album_pk"),
                    isnull(ALBUM.NAME,"NULL").as("album_name"),
                    isnull(ALBUM.DESCRIPTION,"NULL").as("album_description"),
                    isnull(ALBUM.CREATED_TIME,"NULL").as("album_created_time"),
                    isnull(ALBUM.LAST_EVENT_TIME,"NULL").as("album_last_event_time"),
                    isnull(countDistinct(ALBUM_USER.PK),"NULL").as("number_of_users"),
                    isnull(countDistinct(EVENT.PK),"NULL").as("number_of_comments"),
                    isnull(countDistinct(SERIES.STUDY_FK),"NULL").as("number_of_studies"),
                    isnull(ALBUM.ADD_USER_PERMISSION,"NULL").as("add_user_permission"),
                    isnull(ALBUM.DOWNLOAD_SERIES_PERMISSION,"NULL").as("download_user_permission"),
                    isnull(ALBUM.SEND_SERIES_PERMISSION,"NULL").as("send_series_permission"),
                    isnull(ALBUM.DELETE_SERIES_PERMISSION,"NULL").as("delete_series_permision"),
                    isnull(ALBUM.ADD_SERIES_PERMISSION,"NULL").as("add_series_permission"),
                    isnull(ALBUM.WRITE_COMMENTS_PERMISSION,"NULL").as("write_comment_permission"),
                    isnull(ALBUM_USER.FAVORITE,"NULL").as("favorite"),
                    isnull(ALBUM_USER.NEW_COMMENT_NOTIFICATIONS,"NULL").as("new_comment_notifications"),
                    isnull(ALBUM_USER.NEW_SERIES_NOTIFICATIONS,"NULL").as("new_series_notifications"),
                    isnull(ALBUM_USER.ADMIN,"NULL").as("admin"),
                    groupConcatDistinct(SERIES.MODALITY).as("modalities"));

            query.addFrom(ALBUM);
            query.addJoin(ALBUM_SERIES,JoinType.LEFT_OUTER_JOIN, ALBUM_SERIES.ALBUM_FK.eq(ALBUM.PK));
            query.addJoin(SERIES,JoinType.LEFT_OUTER_JOIN, SERIES.PK.eq(ALBUM_SERIES.SERIES_FK));
            query.addJoin(ALBUM_USER, ALBUM_USER.ALBUM_FK.eq(ALBUM.PK));

            query.addJoin(EVENT, JoinType.LEFT_OUTER_JOIN, EVENT.ALBUM_FK.eq(ALBUM.PK)
                    .and(EVENT.EVENT_TYPE.eq("Comment"))
                    .and(EVENT.PRIVATE_TARGET_USER_FK.isNull()
                            .or(EVENT.PRIVATE_TARGET_USER_FK.eq(userPK))
                            .or(EVENT.USER_FK.eq(userPK))));

            query.addConditions(ALBUM.PK.eq(albumPk));
            query.addConditions(ALBUM_USER.FAVORITE.isNotNull());
            query.addConditions(ALBUM_USER.USER_FK.eq(userPK).and(ALBUM_USER.ALBUM_FK.eq(ALBUM.PK)));

            query.addGroupBy(ALBUM.PK);
            Record18<BigDecimal, String, String, String, String, Long, Long, Long, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, String> result = (Record18<BigDecimal, String, String, String, String, Long, Long, Long, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, String>) query.fetchOne();

            return recordToAlbumResponse(result);
        } catch (Exception e) {
            throw new JOOQException("Error during request");
        }
    }

    private static int getAlbumTotalCount(long userPk, ArrayList<Condition> conditionArrayList, Connection connection) {
        final DSLContext create = DSL.using(connection, SQLDialect.MYSQL);
        final SelectQuery query = create.selectQuery();
        query.addSelect(countDistinct(ALBUM.PK));
        query.addFrom(ALBUM);
        query.addJoin(ALBUM_USER, ALBUM_USER.ALBUM_FK.eq(ALBUM.PK).and(ALBUM_USER.USER_FK.eq(userPk)));
        query.addJoin(USERS, USERS.PK.eq(ALBUM_USER.USER_FK));
        for (Condition c : conditionArrayList) {
            if (c != null) {
                query.addConditions(c);
            }
        }

        return (int) query.fetchOne().getValue(0);
    }

    private static SortField getOrderBy(MultivaluedMap<String, String> queryParameters, DSLContext create) throws  BadQueryParametersException{
        if (queryParameters.containsKey("sort")) {
            Field ord;

            final Boolean ascDesc = queryParameters.get("sort").get(0).startsWith("-");
            final String orderByParameter = queryParameters.get("sort").get(0).replace("-", "");

            if (orderByParameter.compareTo("created_time") == 0) ord = ALBUM.CREATED_TIME;
            else if (orderByParameter.compareTo("last_event_time") == 0) ord = ALBUM.LAST_EVENT_TIME;
            else if (orderByParameter.compareTo("name") == 0) ord = ALBUM.NAME;
            else if (orderByParameter.compareTo("number_of_users") == 0) {
                final Field<Object> fieldNumberOfUsers = create.select(countDistinct(ALBUM_USER.PK)).asField();
                ord = fieldNumberOfUsers;
            }
            else if (orderByParameter.compareTo("number_of_studies") == 0) {
                final Field<Object> fieldNumberOfStudies = create.select(countDistinct(EVENT.PK)).asField();
                ord = fieldNumberOfStudies;
            }
            else if (orderByParameter.compareTo("number_of_comments") == 0) {
                final Field<Object> fieldNumberOfComments = create.select(countDistinct(SERIES.STUDY_FK)).asField();
                ord = fieldNumberOfComments;
            }
            else throw new BadQueryParametersException("sort: " + queryParameters.get("sort").get(0));

            return ascDesc ? ord.desc() : ord.asc();
        }
        //Default sort
        return ALBUM.CREATED_TIME.desc();
    }


    private static Condition createConditon(MultivaluedMap<String, String> queryParameters, String key, TableField column)
    throws BadQueryParametersException {
        if (queryParameters.containsKey(key)) {
            String parameterNoStar = queryParameters.get(key).get(0).replace("*", "");

            String parameter = queryParameters.get(key).get(0);
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

                if (isFuzzyMatching(queryParameters)) {
                    Condition fuzzyCondition = condition("SOUNDEX(\""+parameterNoStar+"\") = SOUNDEX("+column.getName()+")");
                    return condition.or(fuzzyCondition);
                }
                return condition;
            }
        } else {
            return null;
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
