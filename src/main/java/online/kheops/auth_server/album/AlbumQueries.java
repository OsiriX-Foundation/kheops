package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.entity.Comment;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.JPANamedQueryConstants;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;
import static online.kheops.auth_server.util.JOOQTools.checkDate;
import static online.kheops.auth_server.util.JPANamedQueryConstants.*;
import static online.kheops.auth_server.util.JooqConstances.*;

public class AlbumQueries {

    private AlbumQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static Album findAlbumById(String albumId, EntityManager em)
            throws AlbumNotFoundException {

        try {
            return em.createNamedQuery("Albums.findById", Album.class)
                    .setParameter(JPANamedQueryConstants.ALBUM_ID, albumId)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new AlbumNotFoundException();
        }
    }

    public static AlbumUser findAlbumUserByUserAndAlbum(User user, Album album, EntityManager em )
            throws UserNotMemberException {

        try {
            return em.createNamedQuery("AlbumUser.findByAlbumIdAndUser", AlbumUser.class)
                    .setParameter(USER, user)
                    .setParameter(ALBUM, album)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new UserNotMemberException();
        }
    }

    public static List<Album> findAlbumsWithEnabledNewSeriesWebhooks(String studyInstanceUID, EntityManager em ) {
        return em.createNamedQuery("Albums.findWithEnabledNewSeriesWebhooks", Album.class)
                .setParameter(STUDY_UID, studyInstanceUID)
                .getResultList();
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

    public static PairListXTotalCount<AlbumResponse> findAlbumsByUserPk(AlbumQueryParams albumQueryParams, EntityManager em)
            throws BadQueryParametersException {

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<AlbumResponseBuilder> c = cb.createQuery(AlbumResponseBuilder.class);
        final Root<Album> al = c.from(Album.class);

        final Join<Album, AlbumSeries> alS = al.join("albumSeries", javax.persistence.criteria.JoinType.LEFT);
        final Join<AlbumSeries, Series> se = alS.join("series", javax.persistence.criteria.JoinType.LEFT);
        se.on(cb.isTrue(se.get("populated")));
        final Join<Series, Study> st = se.join("study", javax.persistence.criteria.JoinType.LEFT);
        st.on(cb.isTrue(st.get("populated")));
        final Join<Album, AlbumUser> alU = al.join("albumUser");
        final Join<AlbumUser, User> u = alU.join("user", javax.persistence.criteria.JoinType.LEFT);
        final Join<Album, Event> com = al.join("events", javax.persistence.criteria.JoinType.LEFT);

        final Predicate privateMessage = cb.or(com.get("privateTargetUser").isNull(), cb.equal(com.get("privateTargetUser"), albumQueryParams.getUser()));
        final Predicate author = cb.equal(com.get("user"), albumQueryParams.getUser());
        com.on(cb.and(cb.equal(com.type(), Comment.class), cb.or(privateMessage, author)));

        final Subquery<Long> subqueryNbUser = c.subquery(Long.class);
        final Root <AlbumUser> subqueryRoot = subqueryNbUser.from(AlbumUser.class);
        subqueryNbUser.where(cb.equal(al, subqueryRoot.get("album")));
        subqueryNbUser.select(cb.countDistinct(subqueryRoot.get("pk")));

        c.select(cb.construct(AlbumResponseBuilder.class, al, alU, cb.countDistinct(st.get("pk")), cb.countDistinct(se.get("pk")),
                cb.sum(cb.<Long>selectCase().when(se.get("numberOfSeriesRelatedInstances").isNull(), 0L).otherwise(se.get("numberOfSeriesRelatedInstances"))),
                subqueryNbUser.getSelection(), cb.countDistinct(com.get("pk")), cb.function("array_agg", String.class ,se.get("modality"))));

        final List<Predicate> criteria = new ArrayList<>();
        albumQueryParams.getName().ifPresent(name -> createConditon(name, criteria, al, cb, albumQueryParams.isFuzzyMatching()));
        applyIfPresent(albumQueryParams::getCreatedTime, time -> createDateConditon(time, criteria, al.get("createdTime"), cb));
        applyIfPresent(albumQueryParams::getLastEventTime, time -> createDateConditon(time, criteria, al.get("lastEventTime"), cb));

        criteria.add(cb.equal(alU.get("user"), albumQueryParams.getUser()));
        criteria.add(cb.notEqual(u.get("inbox"), al));
        if (albumQueryParams.canAddSeries()) {
            criteria.add(cb.or(cb.isTrue(alU.get("admin")), cb.isTrue(al.get("userPermission").get("addSeries"))));
        }

        if (albumQueryParams.canCreateCapabilityToken()) {
            criteria.add(cb.isTrue(alU.get("admin")));
        }

        if (albumQueryParams.isFavorite()) {
            criteria.add(cb.isTrue(alU.get("favorite")));
        }

        if (criteria.size() == 0) {

        } else if (criteria.size() == 1) {
            c.where(cb.and(criteria.get(0)));
        } else {
            c.where(cb.and(criteria.toArray(new Predicate[0])));
        }
        c.groupBy(al, alU);
        setOrderBy(cb, c, albumQueryParams.getOrderBy(), al, albumQueryParams.isDescending());

        final TypedQuery<AlbumResponseBuilder> q = em.createQuery(c);
        albumQueryParams.getOffset().ifPresent(q::setFirstResult);
        albumQueryParams.getLimit().ifPresent(q::setMaxResults);

        final List<AlbumResponseBuilder> res = q.getResultList();
        final List<AlbumResponse> albumResponses = new ArrayList<>();
        for (AlbumResponseBuilder albumResponseBuilder : res) {
            albumResponses.add(albumResponseBuilder.build());
        }

        final int albumTotalCount = getAlbumTotalCount(albumQueryParams, em);

        return new PairListXTotalCount<>(albumTotalCount, albumResponses);
    }


    public static AlbumResponse findAlbumByUserAndAlbumId(String albumId, User user, EntityManager em) {

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<AlbumResponseBuilder> c = cb.createQuery(AlbumResponseBuilder.class);
        final Root<Album> al = c.from(Album.class);

        final Join<Album, AlbumSeries> alS = al.join("albumSeries", javax.persistence.criteria.JoinType.LEFT);
        final Join<AlbumSeries, Series> se = alS.join("series", javax.persistence.criteria.JoinType.LEFT);
        se.on(cb.isTrue(se.get("populated")));
        final Join<Series, Study> st = se.join("study", javax.persistence.criteria.JoinType.LEFT);
        st.on(cb.isTrue(st.get("populated")));
        final Join<Album, AlbumUser> alU = al.join("albumUser");
        final Join<AlbumUser, User> u = alU.join("user", javax.persistence.criteria.JoinType.LEFT);
        final Join<Album, Event> com = al.join("events", javax.persistence.criteria.JoinType.LEFT);

        final Predicate privateMessage = cb.or(com.get("privateTargetUser").isNull(), cb.equal(com.get("privateTargetUser"), user));
        final Predicate author = cb.equal(com.get("user"), user);
        com.on(cb.and(cb.equal(com.type(), Comment.class), cb.or(privateMessage, author)));

        final Subquery<Long> subqueryNbUser = c.subquery(Long.class);
        final Root <AlbumUser> subqueryRoot = subqueryNbUser.from(AlbumUser.class);
        subqueryNbUser.where(cb.equal(al, subqueryRoot.get("album")));
        subqueryNbUser.select(cb.countDistinct(subqueryRoot.get("pk")));

        c.select(cb.construct(AlbumResponseBuilder.class, al, alU, cb.countDistinct(st.get("pk")), cb.countDistinct(se.get("pk")),
                cb.sum(cb.<Long>selectCase().when(se.get("numberOfSeriesRelatedInstances").isNull(), 0L).otherwise(se.get("numberOfSeriesRelatedInstances"))),
                subqueryNbUser.getSelection(), cb.countDistinct(com.get("pk")), cb.function("array_agg", String.class ,se.get("modality"))));

        c.where(cb.and(cb.equal(u, user), cb.equal(al.get("id"), albumId)));
        c.groupBy(al, alU);

        final TypedQuery<AlbumResponseBuilder> q = em.createQuery(c);
        final AlbumResponseBuilder albumResponseBuilder = q.getSingleResult();
        return albumResponseBuilder.build();
    }

    public static AlbumResponse findAlbumByAlbumId(String albumId, EntityManager em) {
        TypedQuery<AlbumResponseBuilder> query = em.createNamedQuery("Albums.getAlbumInfoByAlbumId", AlbumResponseBuilder.class);
        query.setParameter(JPANamedQueryConstants.ALBUM_ID, albumId);
        AlbumResponseBuilder albumResponseBuilder = query.getSingleResult();

        return albumResponseBuilder.build();
    }

    private static int getAlbumTotalCount(AlbumQueryParams albumQueryParams, EntityManager em)
            throws BadQueryParametersException {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> c = cb.createQuery(Long.class);
        final Root<Album> al = c.from(Album.class);

        final Join<Album, AlbumUser> alU = al.join("albumUser");
        final Join<AlbumUser, User> u = alU.join("user", javax.persistence.criteria.JoinType.LEFT);

        c.select(cb.countDistinct(al.get("pk")));

        final List<Predicate> criteria = new ArrayList<>();
        albumQueryParams.getName().ifPresent(name -> createConditon(name, criteria, al, cb, albumQueryParams.isFuzzyMatching()));
        applyIfPresent(albumQueryParams::getCreatedTime, time -> createDateConditon(time, criteria, al.get("createdTime"), cb));
        applyIfPresent(albumQueryParams::getLastEventTime, time -> createDateConditon(time, criteria, al.get("lastEventTime"), cb));

        criteria.add(cb.equal(alU.get("user"), albumQueryParams.getUser()));
        criteria.add(cb.notEqual(u.get("inbox"), al));
        if (albumQueryParams.canAddSeries()) {
            criteria.add(cb.or(cb.isTrue(alU.get("admin")), cb.isTrue(al.get("userPermission").get("addSeries"))));
        }

        if (albumQueryParams.canCreateCapabilityToken()) {
            criteria.add(cb.isTrue(alU.get("admin")));
        }

        if (albumQueryParams.isFavorite()) {
            criteria.add(cb.isTrue(alU.get("favorite")));
        }

        if (criteria.size() == 0) {

        } else if (criteria.size() == 1) {
            c.where(cb.and(criteria.get(0)));
        } else {
            c.where(cb.and(criteria.toArray(new Predicate[0])));
        }

        final TypedQuery<Long> q = em.createQuery(c);
        final  Long res = q.getSingleResult();
        return res.intValue();
    }

    private static void setOrderBy(CriteriaBuilder cb, CriteriaQuery c, String orderByParameter, Path al, boolean descending)
            throws  BadQueryParametersException{

        Expression orderByColumn = null;
        if (orderByParameter.equals("created_time")) {
            orderByColumn = al.get("createdTime");
        }
        else if (orderByParameter.equals("last_event_time")) {
            orderByColumn = al.get("lastEventTime");
        }
        else if (orderByParameter.equals("name")) {
            orderByColumn = al.get("name");
        }
        else if (orderByParameter.equals(NUMBER_OF_USERS)) {
            orderByColumn = cb.literal(6);
        }
        else if (orderByParameter.equals(NUMBER_OF_STUDIES)) {
            orderByColumn = cb.literal(3);
        }
        else if (orderByParameter.equals(NUMBER_OF_COMMENTS)) {
            orderByColumn = cb.literal(7);
        }
        else {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("'sort' query parameter is bad")
                    .build();
            throw new BadQueryParametersException(errorResponse);
        }

        if (orderByColumn != null) {
            if (descending) {
                c.orderBy(cb.desc(orderByColumn), cb.desc(al.get("lastEventTime")));
            } else {
                c.orderBy(cb.asc(orderByColumn), cb.desc(al.get("lastEventTime")));
            }
        }

    }

    private static void createConditon(String name, List<Predicate> criteria, Path al, CriteriaBuilder cb,  boolean fuzzyMatching) {

        final String name2 = name.toLowerCase().replace("_", "\\_").replace("%", "\\%").replace("*", "%");
        final Predicate p1 = cb.like(cb.lower(al.get("name")), name2, '\\');

        if (fuzzyMatching) {
            Predicate p2 = cb.equal(cb.function("SOUNDEX", Long.class, cb.literal(name.replace("*", ""))), cb.function("SOUNDEX", Long.class, al.get("name")));
            //Condition fuzzyCondition = condition("SOUNDEX('"+parameterNoStar+"') = SOUNDEX("+column.getTable()+"."+column.getName()+")");
            criteria.add(cb.or(p1, p2));
            //criteria.add(p1);
        } else {
            criteria.add(p1);
        }
    }

    private static void createDateConditon(String parameter, List<Predicate> criteria, Expression expression, CriteriaBuilder cb)
            throws BadQueryParametersException {

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .message(BAD_QUERY_PARAMETER)
                .detail("Bad date format : yyyyMMdd")
                .build();

        if (parameter.contains("-")) {
            String[] parameters = parameter.split("-");
            if (parameters.length == 2) { //case yyyyMMdd-yyyyMMdd
                if(parameters[0].length() == 0) {//case -yyyyMMdd = all before yyyyMMdd
                    parameters[0] = "00010101";
                }
                checkDate(parameters[0]);
                checkDate(parameters[1]);

                final String strDateBegin = parameters[0]+"000000";
                final LocalDateTime begin = LocalDateTime.parse(strDateBegin, formatter);
                final String strDateEnd = parameters[1] + "235959";
                final LocalDateTime end = LocalDateTime.parse(strDateEnd, formatter);
                criteria.add(cb.between(expression, begin, end));
            } else if(parameters.length == 1) { //case yyyyMMdd- = all after yyyyMMdd
                checkDate(parameters[0]);

                final String strDateBegin = parameters[0]+"000000";
                final LocalDateTime begin = LocalDateTime.parse(strDateBegin, formatter);
                final String strDateEnd = "99991231235959";
                final LocalDateTime end = LocalDateTime.parse(strDateEnd, formatter);
                criteria.add(cb.between(expression, begin, end));
            } else {
                throw new BadQueryParametersException(errorResponse);
            }
        } else {  //case only at this date yyyyMMdd
            checkDate(parameter);
            final String strDateBegin = parameter+"000000";
            final LocalDateTime begin = LocalDateTime.parse(strDateBegin, formatter);
            final String strDateEnd = parameter+"235959";
            final LocalDateTime end = LocalDateTime.parse(strDateEnd, formatter);
            criteria.add(cb.between(expression, begin, end));

        }
    }
}
