package online.kheops.auth_server.study;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.entity.Comment;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.event.MutationType;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.KheopsLogBuilder.*;
import online.kheops.auth_server.util.PairListXTotalCount;
import online.kheops.auth_server.util.StudyQIDOParams;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.series.Series.editSeriesFavorites;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesListByStudyUIDFromAlbum;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesListByStudyUIDFromInbox;
import static online.kheops.auth_server.study.StudyQueries.findStudyByStudyUID;
import static online.kheops.auth_server.user.UserQueries.findUserByUserId;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;
import static online.kheops.auth_server.util.ErrorResponse.Message.STUDY_NOT_FOUND;

public class Studies {

    private Studies() {
        throw new IllegalStateException("Utility class");
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

    private static void setOrderBy(CriteriaBuilder cb, CriteriaQuery<StudyResponseDICOM> c, Path<Study> study, int orderBy, boolean isDescending) {

        Expression orderByColumn = null;

        if (orderBy == Tag.StudyDate)
            orderByColumn = study.get(Study_.studyDate);
        else if (orderBy == Tag.StudyTime)
            orderByColumn = study.get(Study_.studyTime);
        else if (orderBy == Tag.AccessionNumber)
            orderByColumn = study.get(Study_.accessionNumber);
        else if (orderBy == Tag.ReferringPhysicianName)
            orderByColumn = study.get(Study_.referringPhysicianName);
        else if (orderBy == Tag.PatientName)
            orderByColumn = study.get(Study_.patientName);
        else if (orderBy == Tag.PatientID)
            orderByColumn = study.get(Study_.patientID);
        else if (orderBy == Tag.StudyInstanceUID)
            orderByColumn = study.get(Study_.studyInstanceUID);
        else if (orderBy == Tag.StudyID)
            orderByColumn = study.get(Study_.studyID);
        else
            orderByColumn = study.get(Study_.studyDate);

        if (isDescending) {
            if (orderBy == Tag.StudyDate) {
                c.orderBy(cb.desc(cb.coalesce(orderByColumn, "00010101")), cb.desc(study.get(Study_.studyInstanceUID)));
            } else {
                c.orderBy(cb.desc(orderByColumn), cb.desc(study.get(Study_.studyInstanceUID)));
            }
        } else {
            if (orderBy == Tag.StudyDate) {
                c.orderBy(cb.asc(cb.coalesce(orderByColumn, "99993112")), cb.asc(study.get(Study_.studyInstanceUID)));
            } else {
                c.orderBy(cb.asc(orderByColumn), cb.asc(study.get(Study_.studyInstanceUID)));
            }
        }
    }

    public static PairListXTotalCount<Attributes> findAttributesByUserPK(long callingUserPK, StudyQIDOParams qidoParams)
            throws BadQueryParametersException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final List<Predicate> criteria = new ArrayList<>();

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<StudyResponseDICOM> c = cb.createQuery(StudyResponseDICOM.class);
        final Root<User> u = c.from(User.class);

        final Join<User, AlbumUser> alU = u.join(User_.albumUser);
        final Join<AlbumUser, Album> a = alU.join(AlbumUser_.album);
        final Join<Album, AlbumSeries> alS = a.join(Album_.albumSeries);
        final Join<AlbumSeries, Series> se = alS.join(AlbumSeries_.series);
        se.on(cb.isTrue(se.get(Series_.populated)));
        final Join<Series, Study> st = se.join(Series_.study);
        st.on(cb.isTrue(st.get(Study_.populated)));

        final Join<Study, Event> com = st.join(Study_.events, javax.persistence.criteria.JoinType.LEFT);
        final Predicate privateMessage = cb.or(com.get(Comment_.privateTargetUser).isNull(), cb.equal(com.get(Comment_.privateTargetUser).get(User_.pk), callingUserPK));
        final Predicate author = cb.equal(com.get(Comment_.user).get(User_.pk), callingUserPK);
        com.on(cb.and(cb.equal(com.type(), Comment.class), cb.or(privateMessage, author)));

        c.select(cb.construct(StudyResponseDICOM.class, st, cb.countDistinct(se.get(Series_.pk)),
                cb.sum(cb.<Long>selectCase().when(se.get(Series_.numberOfSeriesRelatedInstances).isNull(), 0L).otherwise(se.get(Series_.NUMBER_OF_SERIES_RELATED_INSTANCES))),
                cb.function("array_agg", String.class ,se.get(Series_.modality)),
                cb.sum(cb.<Long>selectCase().when(cb.isTrue(alS.get(AlbumSeries_.favorite)), 1L).otherwise(0L)),
                cb.countDistinct(com.get(Comment_.pk))));

        //filtre
        applyIfPresent(qidoParams::getStudyDateFilter, filter -> createConditionStudyDate(filter, criteria, cb, st.get(Study_.studyDate)));
        applyIfPresent(qidoParams::getStudyTimeFilter, filter -> createConditionStudyTime(filter, criteria, cb, st.get(Study_.studyTime)));

        applyIfPresent(qidoParams::getAccessionNumberFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.accessionNumber), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getReferringPhysicianNameFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.referringPhysicianName), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getPatientNameFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.patientName), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getPatientIDFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.patientID), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getStudyIDFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.studyID), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getStudyDescriptionFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.studyDescription), qidoParams.isFuzzyMatching()));

        applyIfPresent(qidoParams::getFavoriteFilter, filter -> criteria.add(cb.equal(alS.get(AlbumSeries_.favorite), filter)));

        if (!qidoParams.getStudyInstanceUIDFilter().isEmpty()) {
            Predicate p = cb.or();//always false
            for (String studyInstanceUID: qidoParams.getStudyInstanceUIDFilter()) {
                p = cb.or(p, cb.equal(st.get(Study_.studyInstanceUID), studyInstanceUID));
            }
            criteria.add(p);
        }

        if (qidoParams.isFromInbox()) {
            criteria.add(cb.equal(a, u.get(User_.inbox)));
        }

        qidoParams.getAlbumID().ifPresent(albumId -> criteria.add(cb.equal(a.get(Album_.id), albumId)));

        criteria.add(cb.equal(u.get(User_.pk), callingUserPK));

        if (criteria.size() == 1) {
            c.where(cb.and(criteria.get(0)));
        } else if (criteria.size() > 1) {
            c.where(cb.and(criteria.toArray(new Predicate[0])));
        }

        //order by
        setOrderBy(cb, c, st, qidoParams.getOrderByTag(), qidoParams.isDescending());

        //modalities subqueries
        qidoParams.getModalityFilter().ifPresent(filter -> {
            final Subquery<Study> subqueryModality = c.subquery(Study.class);
            final Root<Series> subqueryRoot = subqueryModality.from(Series.class);
            final Join<Series, Study> stsub = subqueryRoot.join(Series_.study);
            subqueryModality.where(cb.and(cb.equal(stsub, st), cb.equal(cb.lower(subqueryRoot.get(Series_.modality)), filter.toLowerCase())));
            subqueryModality.select(stsub);

            c.having(cb.equal(st, cb.any(subqueryModality)));
        });

        c.groupBy(st);

        final TypedQuery<StudyResponseDICOM> q = em.createQuery(c);
        qidoParams.getOffset().ifPresent(q::setFirstResult);
        qidoParams.getLimit().ifPresent(q::setMaxResults);

        final List<StudyResponseDICOM> res = q.getResultList();

        final List<Attributes> attributesList;
        attributesList = new ArrayList<>();

        res.forEach(re -> attributesList.add(re.getAttribute(qidoParams)));

        return new PairListXTotalCount<>(getTotalCount(callingUserPK, qidoParams, em), attributesList);
    }

    private static int getTotalCount(long callingUserPK, StudyQIDOParams qidoParams, EntityManager em)
            throws BadQueryParametersException {

        final List<Predicate> criteria = new ArrayList<>();

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Long> c = cb.createQuery(Long.class);
        final Root<User> u = c.from(User.class);

        final Join<User, AlbumUser> alU = u.join(User_.albumUser);
        final Join<AlbumUser, Album> a = alU.join(AlbumUser_.album);
        final Join<Album, AlbumSeries> alS = a.join(Album_.albumSeries);
        final Join<AlbumSeries, Series> se = alS.join(AlbumSeries_.series);
        se.on(cb.isTrue(se.get(Series_.populated)));
        final Join<Series, Study> st = se.join(Series_.study);
        st.on(cb.isTrue(st.get(Study_.populated)));

        c.select(cb.countDistinct(st.get(Study_.pk)));

        //filtre
        applyIfPresent(qidoParams::getStudyDateFilter, filter -> createConditionStudyDate(filter, criteria, cb, st.get(Study_.studyDate)));
        applyIfPresent(qidoParams::getStudyTimeFilter, filter -> createConditionStudyTime(filter, criteria, cb, st.get(Study_.studyTime)));

        applyIfPresent(qidoParams::getAccessionNumberFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.accessionNumber), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getReferringPhysicianNameFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.referringPhysicianName), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getPatientNameFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.patientName), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getPatientIDFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.patientID), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getStudyIDFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.studyID), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getStudyDescriptionFilter, filter -> createCondition(filter, criteria, cb, st.get(Study_.studyDescription), qidoParams.isFuzzyMatching()));

        applyIfPresent(qidoParams::getFavoriteFilter, filter -> criteria.add(cb.equal(alS.get(AlbumSeries_.favorite), filter)));

        if (!qidoParams.getStudyInstanceUIDFilter().isEmpty()) {
            Predicate p = cb.or();//always false
            for (String studyInstanceUID: qidoParams.getStudyInstanceUIDFilter()) {
                if (p!= null) {
                    p = cb.or(p, cb.equal(st.get(Study_.studyInstanceUID), studyInstanceUID));
                } else {
                    p = cb.or(cb.equal(st.get(Study_.studyInstanceUID), studyInstanceUID));
                }
            }
            criteria.add(p);
        }

        if (qidoParams.isFromInbox()) {
            criteria.add(cb.equal(a, u.get(User_.inbox)));
        }

        criteria.add(cb.equal(u.get(User_.pk), callingUserPK));

        qidoParams.getAlbumID().ifPresent(albumId -> criteria.add(cb.equal(a.get(Album_.id), albumId)));
        qidoParams.getModalityFilter().ifPresent(filter -> criteria.add(cb.equal(cb.lower(se.get(Series_.modality)), filter.toLowerCase())));

        if (criteria.size() == 1) {
            c.where(cb.and(criteria.get(0)));
        } else if (criteria.size() > 1) {
            c.where(cb.and(criteria.toArray(new Predicate[0])));
        }

        final TypedQuery<Long> q = em.createQuery(c);
        return q.getSingleResult().intValue();
    }

    private static void createConditionStudyDate(String parameter, List<Predicate> criteria, CriteriaBuilder cb, Path<String> study)
            throws BadQueryParametersException {
        createIntervalCondition(parameter, criteria, cb, new CheckDate(study));
    }

    private static void createConditionStudyTime(String parameter, List<Predicate> criteria, CriteriaBuilder cb, Path<String> study)
            throws BadQueryParametersException {
        createIntervalCondition(parameter, criteria, cb, new CheckTime(study));
    }

    private static void createIntervalCondition(String parameter, List<Predicate> criteria, CriteriaBuilder cb, CheckMethod checkMethod)
            throws BadQueryParametersException {
        if (parameter.contains("-")) {
            String begin;
            String end;
            String[] parameters = parameter.split("-");
            if (parameters.length == 2) {
                begin = parameters[0];
                end = parameters[1];
                if (begin.length() == 0) {
                    begin = checkMethod.intervalBegin();
                }
                checkMethod.check(begin);
                checkMethod.check(end);
                criteria.add(cb.between(checkMethod.getPath(), begin, end));

            }else if(parameters.length == 1) {
                begin = parameters[0];
                end = checkMethod.intervalEnd();
                checkMethod.check(begin);
                criteria.add(cb.between(checkMethod.getPath(), begin, end));

            } else {
                throw new BadQueryParametersException(checkMethod.getErrorResponse());
            }
        } else {
            checkMethod.check(parameter);
            criteria.add(cb.equal(checkMethod.getPath(), parameter));
        }
    }

    private interface CheckMethod {
        String intervalBegin();
        String intervalEnd();
        ErrorResponse getErrorResponse();
        void check(String s) throws BadQueryParametersException;
        Path<String> getPath();
    }

    private static class CheckTime implements CheckMethod {
        public String intervalBegin() {return "000000.000000";}
        public String intervalEnd() {return "235959.999999";}
        private Path<String> study;

        public CheckTime(Path<String> study) {
            this.study = study;
        }

        public ErrorResponse getErrorResponse() {
            return new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Error with query parameter 'studyTime'")
                    .build();
        }

        public Path<String> getPath() {
            return study;
        }

        public void check(String time) throws BadQueryParametersException {
            if (! time.matches("^(2[0-3]|[01][0-9])([0-5][0-9]){2}.[0-9]{6}$") ) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("A time must be hhmmss.SSSSSS")
                        .build();
                throw new BadQueryParametersException(errorResponse);
            }
        }
    }

    private static class CheckDate implements CheckMethod {
        public String intervalBegin() {return "00010101";}
        public String intervalEnd() {return "99991231";}
        private Path<String> study;

        public CheckDate(Path<String> study) {
            this.study = study;
        }

        public ErrorResponse getErrorResponse() {
            return new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Error with query parameter 'studyDate'")
                    .build();
        }

        public Path<String> getPath() {
            return study;
        }
        public void check(String date) throws BadQueryParametersException {

            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("A date must be yyyyMMdd")
                    .build();

            if (date.matches("^([0-9]{4})(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                dateFormat.setLenient(false);
                try {
                    dateFormat.parse(date);
                } catch (ParseException e) {

                    throw new BadQueryParametersException(errorResponse);
                }
            } else {
                throw new BadQueryParametersException(errorResponse);
            }
        }
    }

    private static void createCondition(String filter, List<Predicate> criteria, CriteriaBuilder cb, Path<String> field, boolean isFuzzyMatching) {

        final String filter2 = filter.toLowerCase().replace("_", "\\_").replace("%", "\\%").replace("*", "%");
        final Predicate p1 = cb.like(cb.lower(field), filter2, '\\');

        if (isFuzzyMatching) {
            Predicate p2 = cb.equal(cb.function("SOUNDEX", Long.class, cb.literal(filter.replace("*", ""))), cb.function("SOUNDEX", Long.class, field));
            criteria.add(cb.or(p1, p2));
        } else {
            criteria.add(p1);
        }
    }

    public static void safeAttributeSetString(Attributes attributes, int tag, VR vr, String string) {
        if (string != null) {
            attributes.setString(tag, vr, string);
        }
    }

    public static Study getStudy(String studyInstanceUID, EntityManager em)
            throws StudyNotFoundException
    {
        return findStudyByStudyUID(studyInstanceUID, em);
    }

    public static Study getOrCreateStudy(String studyInstanceUID, EntityManager em) {

        Study study;
        try {
            study = getStudy(studyInstanceUID, em);
        } catch (StudyNotFoundException ignored) {
            // the study doesn't exist, we need to create it
            final EntityManager localEm = EntityManagerListener.createEntityManager();
            final EntityTransaction tx = localEm.getTransaction();

            try {
                tx.begin();
                study = new Study(studyInstanceUID);
                localEm.persist(study);
                tx.commit();
                study = em.merge(study);
            } catch (PersistenceException ignored2) {
                try {
                    tx.rollback();
                    study = getStudy(studyInstanceUID, em);
                } catch (StudyNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                localEm.close();
            }
        }
        return study;
    }

    public static boolean canAccessStudy(User user, Study study, EntityManager em) {
        try {
            StudyQueries.findStudyByStudyandUser(study, user, em);
            return true;
        } catch (StudyNotFoundException e) {
            return false;
        }
    }

    public static boolean canAccessStudy(String sub, String studyUID) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            final User user = findUserByUserId(sub, em);
            final Study study = getStudy(studyUID, em);
            StudyQueries.findStudyByStudyandUser(study, user, em);
            return true;
        } catch (StudyNotFoundException | UserNotFoundException e) {
            return false;
        }
    }

    public static boolean canAccessStudy(Album album, Study study, EntityManager em) {
        try {
            StudyQueries.findStudyByStudyandAlbum(study, album, em);
            return true;
        } catch (StudyNotFoundException e) {
            return false;
        }
    }

    public static boolean canAccessStudy(Album album, String studyUID) {

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            StudyQueries.findStudyByStudyandAlbum(studyUID, album, em);
            return true;

        } catch (StudyNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
    }

    public static void editFavorites(User callingUser, String studyInstanceUID, String fromAlbumId, boolean favorite, KheopsLogBuilder kheopsLogBuilder)
            throws AlbumNotFoundException, StudyNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            List<Series> seriesList;
            final Album album;
            if (fromAlbumId == null) {
                seriesList = findSeriesListByStudyUIDFromInbox(callingUser, studyInstanceUID, em);
                album = callingUser.getInbox();
                kheopsLogBuilder.album("inbox");

            } else {
                album = getAlbum(fromAlbumId, em);
                seriesList = findSeriesListByStudyUIDFromAlbum(album, studyInstanceUID, em);
                kheopsLogBuilder.album(fromAlbumId);
            }
            if(seriesList.isEmpty()) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(STUDY_NOT_FOUND)
                        .detail("Study does not exist or you don't have access")
                        .build();
                throw new StudyNotFoundException(errorResponse);
            }

            for(Series s: seriesList) {
                editSeriesFavorites(s, album, favorite, em);
                kheopsLogBuilder.series(s.getSeriesInstanceUID());
            }
            final Study study = getStudy(studyInstanceUID, em);
            final MutationType mutation;
            if (favorite) {
                mutation = MutationType.ADD_FAV;
                kheopsLogBuilder.action(ActionType.ADD_FAVORITE_STUDY);
            } else {
                mutation = MutationType.REMOVE_FAV;
                kheopsLogBuilder.action(ActionType.REMOVE_FAVORITE_STUDY);
            }
            final Mutation favAlbumMutation = Events.albumPostStudyMutation(callingUser, album, mutation, study, seriesList);
            em.persist(favAlbumMutation);
            album.updateLastEventTime();
            tx.commit();
            kheopsLogBuilder.study(studyInstanceUID)
                    .log();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}
