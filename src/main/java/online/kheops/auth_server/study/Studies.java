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

    private static void setOrderBy(CriteriaBuilder cb, CriteriaQuery c, Path study, int orderBy, boolean isDescending) {

        Expression orderByColumn = null;

        if (orderBy == Tag.StudyDate)
            orderByColumn = study.get("studyDate");
        else if (orderBy == Tag.StudyTime)
            orderByColumn = study.get("studyTime");
        else if (orderBy == Tag.AccessionNumber)
            orderByColumn = study.get("accessionNumber");
        else if (orderBy == Tag.ReferringPhysicianName)
            orderByColumn = study.get("referringPhysicianName");
        else if (orderBy == Tag.PatientName)
            orderByColumn = study.get("patientName");
        else if (orderBy == Tag.PatientID)
            orderByColumn = study.get("patientID");
        else if (orderBy == Tag.StudyInstanceUID)
            orderByColumn = study.get("studyInstanceUID");
        else if (orderBy == Tag.StudyID)
            orderByColumn = study.get("studyID");
        else
            orderByColumn = study.get("studyDate");

        if (isDescending) {
            if (orderBy == Tag.StudyDate) {
                c.orderBy(cb.desc(cb.coalesce(orderByColumn, "00010101")), cb.desc(study.get("studyInstanceUID")));
            } else {
                c.orderBy(cb.desc(orderByColumn), cb.desc(study.get("studyInstanceUID")));
            }
        } else {
            if (orderBy == Tag.StudyDate) {
                c.orderBy(cb.asc(cb.coalesce(orderByColumn, "99993112")), cb.asc(study.get("studyInstanceUID")));
            } else {
                c.orderBy(cb.asc(orderByColumn), cb.asc(study.get("studyInstanceUID")));
            }
        }
    }

    public static PairListXTotalCount<Attributes> findAttributesByUserPKJPA(long callingUserPK, StudyQIDOParams qidoParams)
            throws BadQueryParametersException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final List<Predicate> criteria = new ArrayList<>();

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<StudyResponseDICOM> c = cb.createQuery(StudyResponseDICOM.class);
        final Root<User> u = c.from(User.class);

        final Join<User, AlbumUser> alU = u.join("albumUser");
        final Join<AlbumUser, Album> a = alU.join("album");
        final Join<Album, AlbumSeries> alS = a.join("albumSeries");
        final Join<AlbumSeries, Series> se = alS.join("series");
        se.on(cb.isTrue(se.get("populated")));
        final Join<Series, Study> st = se.join("study");
        st.on(cb.isTrue(st.get("populated")));

        final Join<Studies, Event> com = st.join("events", javax.persistence.criteria.JoinType.LEFT);
        final Predicate privateMessage = cb.or(com.get("privateTargetUser").isNull(), cb.equal(com.get("privateTargetUser").get("pk"), callingUserPK));
        final Predicate author = cb.equal(com.get("user").get("pk"), callingUserPK);
        com.on(cb.and(cb.equal(com.type(), Comment.class), cb.or(privateMessage, author)));

        c.select(cb.construct(StudyResponseDICOM.class, st, cb.countDistinct(se.get("pk")),
                cb.sum(cb.<Long>selectCase().when(se.get("numberOfSeriesRelatedInstances").isNull(), 0L).otherwise(se.get("numberOfSeriesRelatedInstances"))),
                cb.function("array_agg", String.class ,se.get("modality")),
                cb.sum(cb.<Long>selectCase().when(cb.isTrue(alS.get("favorite")), 1L).otherwise(0L)),
                cb.countDistinct(com.get("pk"))));

        //filtre
        applyIfPresent(qidoParams::getStudyDateFilter, filter -> createConditionStudyDate(filter, criteria, cb, st.get("studyDate")));
        applyIfPresent(qidoParams::getStudyTimeFilter, filter -> createConditionStudyTime(filter, criteria, cb, st.get("studyTime")));

        applyIfPresent(qidoParams::getAccessionNumberFilter, filter -> createCondition(filter, criteria, cb, st.get("acessionNumber"), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getReferringPhysicianNameFilter, filter -> createCondition(filter, criteria, cb, st.get("referringPhysicianName"), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getPatientNameFilter, filter -> createCondition(filter, criteria, cb, st.get("patientName"), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getPatientIDFilter, filter -> createCondition(filter, criteria, cb, st.get("patientID"), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getStudyIDFilter, filter -> createCondition(filter, criteria, cb, st.get("studyID"), qidoParams.isFuzzyMatching()));
        applyIfPresent(qidoParams::getStudyDescriptionFilter, filter -> createCondition(filter, criteria, cb, st.get("studyDescription"), qidoParams.isFuzzyMatching()));

        applyIfPresent(qidoParams::getFavoriteFilter, filter -> criteria.add(cb.equal(alS.get("favorite"), filter)));

        if (!qidoParams.getStudyInstanceUIDFilter().isEmpty()) {
            Predicate p = cb.or();//always false
            for (String studyInstanceUID: qidoParams.getStudyInstanceUIDFilter()) {
                if (p!= null) {
                    p = cb.or(p, cb.equal(st.get("studyInstanceUID"), studyInstanceUID));
                } else {
                    p = cb.or(cb.equal(st.get("studyInstanceUID"), studyInstanceUID));
                }
            }
            criteria.add(p);
        }

        if (qidoParams.isFromInbox()) {
            criteria.add(cb.equal(a, u.get("inbox")));
        }

        qidoParams.getAlbumID().ifPresent(albumId -> criteria.add(cb.equal(a.get("id"), albumId)));

        if (criteria.size() == 0) {

        } else if (criteria.size() == 1) {
            c.where(cb.and(criteria.get(0)));
        } else {
            c.where(cb.and(criteria.toArray(new Predicate[0])));
        }

        //order by
        setOrderBy(cb, c, st, qidoParams.getOrderByTag(), qidoParams.isDescending());

        //modalities (si filter) regarder group by =>  having
        if (qidoParams.getModalityFilter().isPresent()) {
            final Subquery<Study> subqueryModality = c.subquery(Study.class);
            final Root<Series> subqueryRoot = subqueryModality.from(Series.class);
            final Join<Series, Study> stsub = subqueryRoot.join("study");
            subqueryModality.where(cb.and(cb.equal(stsub, st), cb.equal(cb.lower(subqueryRoot.get("modality")), qidoParams.getModalityFilter().get().toLowerCase())));
            subqueryModality.select(stsub);

            qidoParams.getModalityFilter().ifPresent(filter -> c.having(cb.equal(st, cb.any(subqueryModality))));
        }

        //x-total-count

        c.groupBy(st);

        final TypedQuery<StudyResponseDICOM> q = em.createQuery(c);
        qidoParams.getOffset().ifPresent(q::setFirstResult);
        qidoParams.getLimit().ifPresent(q::setMaxResults);

        final List<StudyResponseDICOM> res = q.getResultList();

        List<Attributes> attributesList;
        attributesList = new ArrayList<>();
        res.forEach(re -> attributesList.add(re.getAttribute(qidoParams)));

        return new PairListXTotalCount<>(123, attributesList);
    }

    private static void createConditionStudyDate(String parameter, List<Predicate> criteria, CriteriaBuilder cb, Path study)
            throws BadQueryParametersException {
        createIntervalCondition(parameter, criteria, cb, new CheckDate(study));
    }

    private static void createConditionStudyTime(String parameter, List<Predicate> criteria, CriteriaBuilder cb, Path study)
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
        Path getPath();
    }

    private static class CheckTime implements CheckMethod {
        public String intervalBegin() {return "000000.000000";}
        public String intervalEnd() {return "235959.999999";}
        private Path study;

        public CheckTime(Path study) {
            this.study = study;
        }

        public ErrorResponse getErrorResponse() {
            return new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Error with query parameter 'studyTime'")
                    .build();
        }

        public Path getPath() {
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
        private Path study;

        public CheckDate(Path study) {
            this.study = study;
        }

        public ErrorResponse getErrorResponse() {
            return new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Error with query parameter 'studyDate'")
                    .build();
        }

        public Path getPath() {
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

    private static void createCondition(String filter, List<Predicate> criteria, CriteriaBuilder cb, Expression field, boolean isFuzzyMatching) {

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
