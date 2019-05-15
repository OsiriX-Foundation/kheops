package online.kheops.auth_server.study;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.PairListXTotalCount;
import online.kheops.auth_server.util.StudyQIDOParams;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.jooq.*;
import org.jooq.impl.DSL;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.BadRequestException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.generated.Tables.*;
import static online.kheops.auth_server.generated.tables.AlbumUser.ALBUM_USER;
import static online.kheops.auth_server.generated.tables.Series.SERIES;
import static online.kheops.auth_server.generated.tables.Studies.STUDIES;
import static online.kheops.auth_server.generated.tables.Users.USERS;
import static online.kheops.auth_server.series.Series.editSeriesFavorites;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesListByStudyUIDFromAlbum;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesListByStudyUIDFromInbox;
import static online.kheops.auth_server.study.StudyQueries.findStudyByStudyUID;
import static online.kheops.auth_server.user.UserQueries.findUserByUserId;
import static org.jooq.impl.DSL.*;

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

    public static PairListXTotalCount<Attributes> findAttributesByUserPKJOOQ(long callingUserPK, StudyQIDOParams qidoParams, Connection connection)
            throws BadQueryParametersException {

        ArrayList<Condition> conditionArrayList = new ArrayList<>();

        if (qidoParams.isFromInbox()) {
            conditionArrayList.add(ALBUMS.PK.eq(USERS.INBOX_FK));
        }
        qidoParams.getAlbumID().ifPresent(albumId -> conditionArrayList.add(ALBUMS.ID.eq(albumId)));

        final Condition fromCondition;
        if (conditionArrayList.isEmpty()) {
            fromCondition = trueCondition();
        } else {
            fromCondition = conditionArrayList.get(0);
        }

        DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);

        applyIfPresent(qidoParams::getStudyDateFilter, filter -> conditionArrayList.add(createConditionStudyDate(filter)));
        applyIfPresent(qidoParams::getStudyTimeFilter, filter -> conditionArrayList.add(createConditionStudyTime(filter)));
        applyIfPresent(qidoParams::getAccessionNumberFilter, filter -> conditionArrayList.add(createCondition(filter, STUDIES.ACCESSION_NUMBER, qidoParams.isFuzzyMatching())));
        applyIfPresent(qidoParams::getReferringPhysicianNameFilter, filter -> conditionArrayList.add(createCondition(filter, STUDIES.REFERRING_PHYSICIAN_NAME, qidoParams.isFuzzyMatching())));
        applyIfPresent(qidoParams::getPatientNameFilter, filter -> conditionArrayList.add(createCondition(filter, STUDIES.PATIENT_NAME, qidoParams.isFuzzyMatching())));
        applyIfPresent(qidoParams::getPatientIDFilter, filter -> conditionArrayList.add(createCondition(filter, STUDIES.PATIENT_ID, qidoParams.isFuzzyMatching())));
        applyIfPresent(qidoParams::getStudyIDFilter, filter -> conditionArrayList.add(createCondition(filter, STUDIES.STUDY_ID, qidoParams.isFuzzyMatching())));
        applyIfPresent(qidoParams::getFavoriteFilter, filter -> conditionArrayList.add(ALBUM_SERIES.FAVORITE.eq(filter)));

        if (!qidoParams.getStudyInstanceUIDFilter().isEmpty()) {
            Condition condition = trueCondition();
            boolean conditionIsInitialised = false;
            for (String studyInstanceUID: qidoParams.getStudyInstanceUIDFilter()) {
                if(!conditionIsInitialised) {
                    condition = STUDIES.STUDY_UID.eq(studyInstanceUID);
                    conditionIsInitialised = true;
                } else {
                    condition = condition.or(STUDIES.STUDY_UID.eq(studyInstanceUID));
                }
            }
            conditionArrayList.add(condition);
        }

        qidoParams.getModalityFilter().ifPresent(filter -> conditionArrayList.add(createConditionModality(filter)));

        final SelectQuery<Record> countQuery = create.selectQuery();
        countQuery.addSelect(countDistinct(STUDIES.PK));
        countQuery.addFrom(USERS);
        countQuery.addJoin(ALBUM_USER, ALBUM_USER.USER_FK.eq(USERS.PK));
        countQuery.addJoin(ALBUMS, ALBUMS.PK.eq(ALBUM_USER.ALBUM_FK));
        countQuery.addJoin(ALBUM_SERIES, ALBUM_SERIES.ALBUM_FK.eq(ALBUMS.PK));
        countQuery.addJoin(SERIES, SERIES.PK.eq(ALBUM_SERIES.SERIES_FK));
        countQuery.addJoin(STUDIES, STUDIES.PK.eq(SERIES.STUDY_FK));


        conditionArrayList.add(USERS.PK.eq(callingUserPK));
        conditionArrayList.add(SERIES.POPULATED.isTrue());
        conditionArrayList.add(STUDIES.POPULATED.isTrue());

        for (Condition c : conditionArrayList) {
            if (c != null) {
                countQuery.addConditions(c);
            }
        }

        Integer studiesTotalCount = (Integer) countQuery.fetch().getValues("count").get(0);

        final SelectQuery<? extends Record> selectQuery = create.selectQuery();
        selectQuery.addSelect(STUDIES.STUDY_UID.as(STUDIES.STUDY_UID.getName()),
                isnull(STUDIES.STUDY_DATE, "NULL").as(STUDIES.STUDY_DATE.getName()),
                isnull(STUDIES.STUDY_TIME, "NULL").as(STUDIES.STUDY_TIME.getName()),
                isnull(STUDIES.TIMEZONE_OFFSET_FROM_UTC, "NULL").as(STUDIES.TIMEZONE_OFFSET_FROM_UTC.getName()),
                isnull(STUDIES.ACCESSION_NUMBER, "NULL").as(STUDIES.ACCESSION_NUMBER.getName()),
                isnull(STUDIES.REFERRING_PHYSICIAN_NAME, "NULL").as(STUDIES.REFERRING_PHYSICIAN_NAME.getName()),
                isnull(STUDIES.PATIENT_NAME, "NULL").as(STUDIES.PATIENT_NAME.getName()),
                isnull(STUDIES.PATIENT_ID, "NULL").as(STUDIES.PATIENT_ID.getName()),
                isnull(STUDIES.PATIENT_BIRTH_DATE, "NULL").as(STUDIES.PATIENT_BIRTH_DATE.getName()),
                isnull(STUDIES.PATIENT_SEX, "NULL").as(STUDIES.PATIENT_SEX.getName()),
                isnull(STUDIES.STUDY_ID, "NULL").as(STUDIES.STUDY_ID.getName()),
                countDistinct(SERIES.PK).as("count:" + SERIES.PK.getName()),
                sum(SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES).as("sum:" + SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES.getName()),
                isnull(groupConcatDistinct(SERIES.MODALITY), "NULL").as("modalities"),
                countDistinct(when(ALBUM_SERIES.FAVORITE.eq(true), ALBUM_SERIES.PK)).as("sum_fav"),
                countDistinct(EVENTS).as("sum_comments"));

        if(qidoParams.includeStudyDescriptionField()) {
            selectQuery.addSelect(isnull(STUDIES.STUDY_DESCRIPTION, "NULL").as(STUDIES.STUDY_DESCRIPTION.getName()));
        }

        selectQuery.addFrom(USERS);
        selectQuery.addJoin(ALBUM_USER, ALBUM_USER.USER_FK.eq(USERS.PK));
        selectQuery.addJoin(ALBUMS, ALBUMS.PK.eq(ALBUM_USER.ALBUM_FK));
        selectQuery.addJoin(ALBUM_SERIES, ALBUM_SERIES.ALBUM_FK.eq(ALBUMS.PK));
        selectQuery.addJoin(SERIES, SERIES.PK.eq(ALBUM_SERIES.SERIES_FK));
        selectQuery.addJoin(STUDIES, STUDIES.PK.eq(SERIES.STUDY_FK));
        selectQuery.addJoin(EVENTS,JoinType.LEFT_OUTER_JOIN, EVENTS.EVENT_TYPE.eq("Comment").and(EVENTS.STUDY_FK.eq(STUDIES.PK)).and(EVENTS.PRIVATE_TARGET_USER_FK.isNull().or(EVENTS.USER_FK.eq(USERS.PK)).or(EVENTS.PRIVATE_TARGET_USER_FK.eq(USERS.PK))));

        for (Condition c : conditionArrayList) {
            if (c != null) {
                selectQuery.addConditions(c);
            }
        }

        selectQuery.addGroupBy(STUDIES.STUDY_UID, STUDIES.PK);

        selectQuery.addOrderBy(orderBy(qidoParams.getOrderByTag(), qidoParams.isDescending()));

        qidoParams.getLimit().ifPresent(selectQuery::addLimit);
        qidoParams.getOffset().ifPresent(selectQuery::addOffset);

        Result<? extends Record> result = selectQuery.fetch();

        List<Attributes> attributesList;
        attributesList = new ArrayList<>();

        for (Record r : result) {

            Attributes attributes = new Attributes();

            qidoParams.getModalityFilter().ifPresent(filter -> {
                //get all the modalities for the STUDY_UID
                String modalities = create.select(isnull(groupConcatDistinct(SERIES.MODALITY), "NULL"))
                        .from(USERS)
                        .join(ALBUM_USER).on(ALBUM_USER.USER_FK.eq(USERS.PK))
                        .join(ALBUMS).on(ALBUMS.PK.eq(ALBUM_USER.ALBUM_FK))
                        .join(ALBUM_SERIES).on(ALBUM_SERIES.ALBUM_FK.eq(ALBUMS.PK))
                        .join(SERIES).on(SERIES.PK.eq(ALBUM_SERIES.SERIES_FK))
                        .join(STUDIES).on(STUDIES.PK.eq(SERIES.STUDY_FK))
                        .where(USERS.PK.eq(callingUserPK))
                        .and(SERIES.POPULATED.isTrue())
                        .and(STUDIES.POPULATED.isTrue())
                        .and(STUDIES.STUDY_UID.eq(r.getValue(0).toString()))
                        .and(fromCondition)
                        .groupBy(STUDIES.STUDY_UID)
                        .fetch().get(0).getValue(0).toString();
                attributes.setString(Tag.ModalitiesInStudy, VR.CS, modalities);
            });
            if (!qidoParams.getModalityFilter().isPresent()) {
                attributes.setString(Tag.ModalitiesInStudy, VR.CS, r.getValue("modalities").toString());
            }

            safeAttributeSetString(attributes, Tag.StudyInstanceUID, VR.UI, r.getValue(STUDIES.STUDY_UID.getName()).toString());
            safeAttributeSetString(attributes, Tag.StudyDate, VR.DA, r.getValue(STUDIES.STUDY_DATE.getName()).toString());
            safeAttributeSetString(attributes, Tag.StudyTime, VR.TM, r.getValue(STUDIES.STUDY_TIME.getName()).toString());
            safeAttributeSetString(attributes, Tag.TimezoneOffsetFromUTC, VR.SH, r.getValue(STUDIES.TIMEZONE_OFFSET_FROM_UTC.getName()).toString());
            safeAttributeSetString(attributes, Tag.AccessionNumber, VR.SH, r.getValue(STUDIES.ACCESSION_NUMBER.getName()).toString());
            safeAttributeSetString(attributes, Tag.ReferringPhysicianName, VR.PN, r.getValue(STUDIES.REFERRING_PHYSICIAN_NAME.getName()).toString());
            safeAttributeSetString(attributes, Tag.PatientName, VR.PN, r.getValue(STUDIES.PATIENT_NAME.getName()).toString());
            safeAttributeSetString(attributes, Tag.PatientID, VR.LO, r.getValue(STUDIES.PATIENT_ID.getName()).toString());
            safeAttributeSetString(attributes, Tag.PatientBirthDate, VR.DA, r.getValue(STUDIES.PATIENT_BIRTH_DATE.getName()).toString());
            safeAttributeSetString(attributes, Tag.PatientSex, VR.CS, r.getValue(STUDIES.PATIENT_SEX.getName()).toString());
            safeAttributeSetString(attributes, Tag.StudyID, VR.SH, r.getValue(STUDIES.STUDY_ID.getName()).toString());
            attributes.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS, ((Integer) r.getValue("count:" + SERIES.PK.getName())));
            attributes.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS, ((BigDecimal) r.getValue("sum:" + SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES.getName())).intValue());

            if(qidoParams.includeFavoriteField()) {
                attributes.setInt(0x00012345, VR.IS, ((Integer)r.getValue("sum_fav")));
            }
            if(qidoParams.includeCommentField()) {
                attributes.setInt(0x00012346, VR.IS, ((Integer)r.getValue("sum_comments")));
            }
            if(qidoParams.includeStudyDescriptionField()) {
                safeAttributeSetString(attributes, Tag.StudyDescription, VR.CS, r.getValue(STUDIES.STUDY_DESCRIPTION.getName()).toString());
            }

            safeAttributeSetString(attributes, Tag.InstanceAvailability, VR.CS, "ONLINE");

            attributesList.add(attributes);
        }
        return new PairListXTotalCount<>(studiesTotalCount, attributesList);
    }

    private static SortField orderBy(int orderBy, boolean descending) {
        final TableField ord;

        if (orderBy == Tag.StudyDate)
            ord = STUDIES.STUDY_DATE;
        else if (orderBy == Tag.StudyTime)
            ord = STUDIES.STUDY_TIME;
        else if (orderBy == Tag.AccessionNumber)
            ord = STUDIES.ACCESSION_NUMBER;
        else if (orderBy == Tag.ReferringPhysicianName)
            ord = STUDIES.REFERRING_PHYSICIAN_NAME;
        else if (orderBy == Tag.PatientName)
            ord = STUDIES.PATIENT_NAME;
        else if (orderBy == Tag.PatientID)
            ord = STUDIES.PATIENT_ID;
        else if (orderBy == Tag.StudyInstanceUID)
            ord = STUDIES.STUDY_UID;
        else if (orderBy == Tag.StudyID)
            ord = STUDIES.STUDY_ID;
        else
            ord = STUDIES.STUDY_DATE;

        return descending ? ord.desc() : ord.asc();
    }

    private static Condition createConditionModality(String filter) {
        if (filter.equalsIgnoreCase("null")) {
            return SERIES.MODALITY.isNull();
        } else {
            return SERIES.MODALITY.equalIgnoreCase(filter);
        }
    }

    private static Condition createConditionStudyDate(String filter)
            throws BadQueryParametersException {
        return createIntervalCondition(filter, new CheckDate());
    }

    private static Condition createConditionStudyTime(String filter)
            throws BadQueryParametersException {
            return createIntervalCondition(filter, new CheckTime());
    }

    private static Condition createIntervalCondition(String parameter, CheckMethod checkMethod)
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
                return checkMethod.getColumn().between(begin, end);

            }else if(parameters.length == 1) {
                begin = parameters[0];
                end = checkMethod.intervalEnd();
                checkMethod.check(begin);
                return checkMethod.getColumn().between(begin, end);
            } else {
                throw new BadRequestException(checkMethod.getColumn().getName() + ": " + parameter);
            }
        } else {
            checkMethod.check(parameter);
            return checkMethod.getColumn().eq(parameter);
        }
    }

    private interface CheckMethod {
        String intervalBegin();
        String intervalEnd();
        void check(String s) throws BadQueryParametersException;
        TableField<? extends Record, String> getColumn();
    }

    private static class CheckTime implements CheckMethod {
        public String intervalBegin() {return "000000.000000";}
        public String intervalEnd() {return "235959.999999";}
        public TableField<? extends Record, String> getColumn() {return STUDIES.STUDY_TIME;}

        public void check(String time) throws BadQueryParametersException {
            if (! time.matches("^(2[0-3]|[01][0-9])([0-5][0-9]){2}.[0-9]{6}$") ) {
                throw new BadQueryParametersException(org.dcm4che3.data.Keyword.valueOf(Tag.StudyTime) + " :" + time);
            }
        }
    }

    private static class CheckDate implements CheckMethod {
        public String intervalBegin() {return "00010101";}
        public String intervalEnd() {return "99991231";}
        public TableField<? extends Record, String> getColumn() {return STUDIES.STUDY_DATE;}

        public void check(String date) throws BadQueryParametersException {
            if (date.matches("^([0-9]{4})(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                dateFormat.setLenient(false);
                try {
                    dateFormat.parse(date);
                } catch (ParseException e) {
                    throw new BadQueryParametersException(org.dcm4che3.data.Keyword.valueOf(Tag.StudyDate) + " :" + date);
                }
            } else {
                throw new BadQueryParametersException(org.dcm4che3.data.Keyword.valueOf(Tag.StudyDate) + " :" + date);
            }
        }
    }

    private static Condition createCondition(String filter, TableField<? extends Record, String> column, Boolean isFuzzyMatching) {
        String parameterNoStar = filter.replace("*", "")
                .replace(", ", "^")
                .replace(" ","^")
                .replace(",", "^");

        if (parameterNoStar.length() == 0) {
            return null;
        }
        if ("null".equalsIgnoreCase(parameterNoStar)) {
            return column.isNull();
        } else {
            Condition condition;
            if (filter.startsWith("*") && filter.endsWith("*")) {
                condition = column.containsIgnoreCase(parameterNoStar);
            } else if (filter.startsWith("*")) {
                condition = column.lower().endsWith(parameterNoStar.toLowerCase());
            } else if (filter.endsWith("*")) {
                condition = column.lower().startsWith(parameterNoStar.toLowerCase());
            } else {
                condition = column.equalIgnoreCase(parameterNoStar);
            }

            if (isFuzzyMatching) {
                Condition fuzzyCondition = condition("SOUNDEX('"+parameterNoStar+"') = SOUNDEX("+column.getName()+")");
                return condition.or(fuzzyCondition);
            }
            return condition;
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
            study = new Study();
            study.setStudyInstanceUID(studyInstanceUID);
            em.persist(study);
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

    public static boolean canAccessStudyInbox(User user, Study study, EntityManager em) {
        try {
            StudyQueries.findStudyByStudyandUserInbox(study, user, em);
            return true;
        } catch (StudyNotFoundException e) {
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

    public static boolean canAccessStudy(Album album, Study study) {

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            StudyQueries.findStudyByStudyandAlbum(study, album, em);
            return true;

        } catch (StudyNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
    }

    public static boolean canAccessStudy(Album album, String studyUID) {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            StudyQueries.findStudyByStudyandAlbum(studyUID, album, em);
            return true;

        } catch (StudyNotFoundException e) {
            return false;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void editFavorites(User callingUser, String studyInstanceUID, String fromAlbumId, boolean favorite)
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
            } else {
                album = getAlbum(fromAlbumId, em);
                seriesList = findSeriesListByStudyUIDFromAlbum(callingUser,album, studyInstanceUID, em);
            }
            if(seriesList.isEmpty()) {
                throw new StudyNotFoundException("Study not found");
            }

            for(Series s: seriesList) {
                editSeriesFavorites(s, album, favorite, em);
            }
            final Study study = getStudy(studyInstanceUID, em);
            final Events.MutationType mutation;
            if (favorite) {
                mutation = Events.MutationType.ADD_FAV;
            } else {
                mutation = Events.MutationType.REMOVE_FAV;
            }
            final Mutation newAlbumMutation = Events.albumPostStudyMutation(callingUser, album, mutation, study);
            em.persist(newAlbumMutation);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}
