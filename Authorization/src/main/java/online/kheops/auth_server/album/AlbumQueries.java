package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.*;
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

        final Join<Album, AlbumSeries> alS = al.join(Album_.albumSeries, javax.persistence.criteria.JoinType.LEFT);
        final Join<AlbumSeries, Series> se = alS.join(AlbumSeries_.series, javax.persistence.criteria.JoinType.LEFT);
        se.on(cb.isTrue(se.get(Series_.populated)));
        final Join<Series, Study> st = se.join(Series_.study, javax.persistence.criteria.JoinType.LEFT);
        st.on(cb.isTrue(st.get(Study_.populated)));
        final Join<Album, AlbumUser> alU = al.join(Album_.albumUser);
        final Join<AlbumUser, User> u = alU.join(AlbumUser_.user, javax.persistence.criteria.JoinType.LEFT);

        final Subquery<Long> subqueryNbComments = c.subquery(Long.class);
        final Root <Comment> subqueryCommentRoot = subqueryNbComments.from(Comment.class);
        final Predicate privateMessage = cb.or(subqueryCommentRoot.get(Comment_.privateTargetUser).isNull(), cb.equal(subqueryCommentRoot.get(Comment_.privateTargetUser).get(User_.pk), albumQueryParams.getUser().getPk()));
        final Predicate author = cb.equal(subqueryCommentRoot.get(Comment_.user).get(User_.pk), albumQueryParams.getUser().getPk());
        subqueryNbComments.where(cb.and(cb.and(cb.equal(subqueryCommentRoot.type(), Comment.class), cb.or(privateMessage, author))), cb.equal(subqueryCommentRoot.get(Comment_.album), al));
        subqueryNbComments.select(cb.countDistinct(subqueryCommentRoot.get(Comment_.pk)));

        final Subquery<Long> subqueryNbUser = c.subquery(Long.class);
        final Root <AlbumUser> subqueryRoot = subqueryNbUser.from(AlbumUser.class);
        subqueryNbUser.where(cb.equal(al, subqueryRoot.get(AlbumUser_.album)));
        subqueryNbUser.select(cb.countDistinct(subqueryRoot.get(AlbumUser_.pk)));

        c.select(cb.construct(AlbumResponseBuilder.class, al.get(Album_.id), al.get(Album_.name), al.get(Album_.description), al.get(Album_.createdTime), al.get(Album_.lastEventTime), al.get(Album_.userPermission),
                alU.get(AlbumUser_.admin), alU.get(AlbumUser_.favorite), alU.get(AlbumUser_.newCommentNotifications), alU.get(AlbumUser_.newSeriesNotifications),
                cb.countDistinct(st.get(Study_.pk)), cb.countDistinct(se.get(Series_.pk)),
                cb.sum(cb.<Long>selectCase().when(se.get(Series_.numberOfSeriesRelatedInstances).isNull(), 0L).otherwise(se.get(Series_.NUMBER_OF_SERIES_RELATED_INSTANCES))),
                subqueryNbUser.getSelection(), subqueryNbComments.getSelection(), cb.function("array_agg", String.class ,se.get(Series_.modality))));

        final List<Predicate> criteria = new ArrayList<>();
        albumQueryParams.getName().ifPresent(name -> createCondition(name, criteria, al, cb, albumQueryParams.isFuzzyMatching()));
        applyIfPresent(albumQueryParams::getCreatedTime, time -> createDateCondition(time, criteria, al.get(Album_.createdTime), cb));
        applyIfPresent(albumQueryParams::getLastEventTime, time -> createDateCondition(time, criteria, al.get(Album_.lastEventTime), cb));

        criteria.add(cb.equal(alU.get(AlbumUser_.user), albumQueryParams.getUser()));
        criteria.add(cb.notEqual(u.get(User_.inbox), al));
        if (albumQueryParams.canAddSeries()) {
            criteria.add(cb.or(cb.isTrue(alU.get(AlbumUser_.admin)), cb.isTrue(al.get(Album_.userPermission).get(UserPermission_.addSeries))));
        }

        if (albumQueryParams.canCreateCapabilityToken()) {
            criteria.add(cb.isTrue(alU.get(AlbumUser_.admin)));
        }

        if (albumQueryParams.isFavorite()) {
            criteria.add(cb.isTrue(alU.get(AlbumUser_.favorite)));
        }

        albumQueryParams.getModality().ifPresent(filter -> {
            final Subquery<Album> subqueryModality = c.subquery(Album.class);
            final Root<Album> subqueryRoot2 = subqueryModality.from(Album.class);
            final Join<Album, AlbumSeries> alSSub = subqueryRoot2.join(Album_.albumSeries);
            final Join<AlbumSeries, Series> seSub = alSSub.join(AlbumSeries_.series);
            final Join<Series, Study> stSub = seSub.join(Series_.study);

            final List<Predicate> criteriaSub = new ArrayList<>();
            criteriaSub.add(cb.equal(subqueryRoot2, al));
            criteriaSub.add(cb.isTrue(seSub.get(Series_.populated)));
            criteriaSub.add(cb.isTrue(stSub.get(Study_.populated)));
            criteriaSub.add(cb.equal(cb.lower(seSub.get(Series_.modality)), filter.toLowerCase()));

            subqueryModality.where(cb.and(criteriaSub.toArray(new Predicate[0])));

            subqueryModality.groupBy(subqueryRoot2);
            subqueryModality.select(subqueryRoot2);

            c.having(cb.equal(al, cb.any(subqueryModality)));
        });

        if (criteria.size() == 1) {
            c.where(cb.and(criteria.get(0)));
        } else if (criteria.size() > 1) {
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

        final Join<Album, AlbumSeries> alS = al.join(Album_.albumSeries, javax.persistence.criteria.JoinType.LEFT);
        final Join<AlbumSeries, Series> se = alS.join(AlbumSeries_.series, javax.persistence.criteria.JoinType.LEFT);
        se.on(cb.isTrue(se.get(Series_.populated)));
        final Join<Series, Study> st = se.join(Series_.study, javax.persistence.criteria.JoinType.LEFT);
        st.on(cb.isTrue(st.get(Study_.populated)));
        final Join<Album, AlbumUser> alU = al.join(Album_.albumUser);
        final Join<AlbumUser, User> u = alU.join(AlbumUser_.user, javax.persistence.criteria.JoinType.LEFT);

        final Subquery<Long> subqueryNbComments = c.subquery(Long.class);
        final Root <Comment> subqueryCommentRoot = subqueryNbComments.from(Comment.class);
        final Predicate privateMessage = cb.or(subqueryCommentRoot.get(Comment_.privateTargetUser).isNull(), cb.equal(subqueryCommentRoot.get(Comment_.privateTargetUser).get(User_.pk), user.getPk()));
        final Predicate author = cb.equal(subqueryCommentRoot.get(Comment_.user).get(User_.pk), user.getPk());
        subqueryNbComments.where(cb.and(cb.and(cb.equal(subqueryCommentRoot.type(), Comment.class), cb.or(privateMessage, author))), cb.equal(subqueryCommentRoot.get(Comment_.album), al));
        subqueryNbComments.select(cb.countDistinct(subqueryCommentRoot.get(Comment_.pk)));

        final Subquery<Long> subqueryNbUser = c.subquery(Long.class);
        final Root <AlbumUser> subqueryRoot = subqueryNbUser.from(AlbumUser.class);
        subqueryNbUser.where(cb.equal(al, subqueryRoot.get(AlbumUser_.album)));
        subqueryNbUser.select(cb.countDistinct(subqueryRoot.get(AlbumUser_.pk)));

        c.select(cb.construct(AlbumResponseBuilder.class,  al.get(Album_.id), al.get(Album_.name), al.get(Album_.description), al.get(Album_.createdTime), al.get(Album_.lastEventTime), al.get(Album_.userPermission),
                alU.get(AlbumUser_.admin), alU.get(AlbumUser_.favorite), alU.get(AlbumUser_.newCommentNotifications), alU.get(AlbumUser_.newSeriesNotifications),
                cb.countDistinct(st.get(Study_.pk)), cb.countDistinct(se.get(Series_.pk)),
                cb.sum(cb.<Long>selectCase().when(se.get(Series_.numberOfSeriesRelatedInstances).isNull(), 0L).otherwise(se.get(Series_.NUMBER_OF_SERIES_RELATED_INSTANCES))),
                subqueryNbUser.getSelection(), subqueryNbComments.getSelection(), cb.function("array_agg", String.class ,se.get(Series_.modality))));

        c.where(cb.and(cb.equal(u, user), cb.equal(al.get(Album_.id), albumId)));
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

        final Join<Album, AlbumUser> alU = al.join(Album_.albumUser);
        final Join<AlbumUser, User> u = alU.join(AlbumUser_.user, javax.persistence.criteria.JoinType.LEFT);
        final Join<Album, AlbumSeries> alS = al.join(Album_.albumSeries, javax.persistence.criteria.JoinType.LEFT);
        final Join<AlbumSeries, Series> se = alS.join(AlbumSeries_.series, javax.persistence.criteria.JoinType.LEFT);
        se.on(cb.isTrue(se.get(Series_.populated)));
        final Join<Series, Study> st = se.join(Series_.study, javax.persistence.criteria.JoinType.LEFT);
        st.on(cb.isTrue(st.get(Study_.populated)));

        c.select(cb.countDistinct(al.get(Album_.pk)));

        final List<Predicate> criteria = new ArrayList<>();
        albumQueryParams.getName().ifPresent(name -> createCondition(name, criteria, al, cb, albumQueryParams.isFuzzyMatching()));
        applyIfPresent(albumQueryParams::getCreatedTime, time -> createDateCondition(time, criteria, al.get(Album_.createdTime), cb));
        applyIfPresent(albumQueryParams::getLastEventTime, time -> createDateCondition(time, criteria, al.get(Album_.lastEventTime), cb));

        criteria.add(cb.equal(alU.get(AlbumUser_.user), albumQueryParams.getUser()));
        criteria.add(cb.notEqual(u.get(User_.inbox), al));
        if (albumQueryParams.canAddSeries()) {
            criteria.add(cb.or(cb.isTrue(alU.get(AlbumUser_.admin)), cb.isTrue(al.get(Album_.userPermission).get(UserPermission_.addSeries))));
        }

        if (albumQueryParams.canCreateCapabilityToken()) {
            criteria.add(cb.isTrue(alU.get(AlbumUser_.admin)));
        }

        if (albumQueryParams.isFavorite()) {
            criteria.add(cb.isTrue(alU.get(AlbumUser_.favorite)));
        }

        albumQueryParams.getModality().ifPresent(filter -> criteria.add(cb.isTrue(cb.equal(cb.lower(se.get(Series_.modality)), filter.toLowerCase()))));

        if (criteria.size() == 1) {
            c.where(cb.and(criteria.get(0)));
        } else if (criteria.size() > 1) {
            c.where(cb.and(criteria.toArray(new Predicate[0])));
        }

        final TypedQuery<Long> q = em.createQuery(c);
        final  Long res = q.getSingleResult();
        return res.intValue();
    }

    private static void setOrderBy(CriteriaBuilder cb, CriteriaQuery<AlbumResponseBuilder> c, String orderByParameter, Path<Album> al, boolean descending)
            throws  BadQueryParametersException {

        Expression orderByColumn = null;
        if (orderByParameter.equals("created_time")) {
            orderByColumn = al.get(Album_.createdTime);
        }
        else if (orderByParameter.equals("last_event_time")) {
            orderByColumn = al.get(Album_.lastEventTime);
        }
        else if (orderByParameter.equals("name")) {
            orderByColumn = al.get(Album_.name);
        }
        else if (orderByParameter.equals("number_of_users")) {
            orderByColumn = cb.literal(6);
        }
        else if (orderByParameter.equals("number_of_studies")) {
            orderByColumn = cb.literal(3);
        }
        else if (orderByParameter.equals("number_of_comments")) {
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
                c.orderBy(cb.desc(orderByColumn), cb.desc(al.get(Album_.lastEventTime)));
            } else {
                c.orderBy(cb.asc(orderByColumn), cb.desc(al.get(Album_.lastEventTime)));
            }
        }

    }

    private static void createCondition(String name, List<Predicate> criteria, Path<Album> al, CriteriaBuilder cb, boolean fuzzyMatching) {

        final String name2 = name.toLowerCase().replace("_", "\\_").replace("%", "\\%").replace("*", "%");
        final Predicate p1 = cb.like(cb.lower(al.get(Album_.name)), name2, '\\');

        if (fuzzyMatching) {
            Predicate p2 = cb.equal(cb.function("SOUNDEX", Long.class, cb.literal(name.replace("*", ""))), cb.function("SOUNDEX", Long.class, al.get(Album_.name)));
            criteria.add(cb.or(p1, p2));
        } else {
            criteria.add(p1);
        }
    }

    private static void createDateCondition(String parameter, List<Predicate> criteria, Path<LocalDateTime> expression, CriteriaBuilder cb)
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

    public static void deleteAllAlbumUserByAlbum (Album album, EntityManager em) {
        em.createNamedQuery("AlbumUser.deleteAllByAlbum")
                .setParameter(ALBUM, album)
                .executeUpdate();

    }

    public static void deleteAllAlbumSeriesByAlbum (Album album, EntityManager em) {
        em.createNamedQuery("AlbumSeries.deleteAllByAlbum")
                .setParameter(ALBUM, album)
                .executeUpdate();

    }
}
