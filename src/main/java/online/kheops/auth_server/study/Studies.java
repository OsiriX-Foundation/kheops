package online.kheops.auth_server.study;

import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.PairListXTotalCount;
import online.kheops.auth_server.util.QIDOParams;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Keyword;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.jooq.*;
import org.jooq.impl.DSL;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MultivaluedMap;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static online.kheops.auth_server.study.StudyQueries.findStudyByStudyandAlbum;
import static online.kheops.auth_server.util.JOOQTools.*;
import static online.kheops.auth_server.generated.Tables.ALBUM;
import static online.kheops.auth_server.generated.Tables.ALBUM_SERIES;
import static online.kheops.auth_server.generated.tables.AlbumUser.ALBUM_USER;
import static online.kheops.auth_server.generated.tables.Series.SERIES;
import static online.kheops.auth_server.generated.tables.Studies.STUDIES;
import static online.kheops.auth_server.generated.tables.Users.USERS;
import static online.kheops.auth_server.study.StudyQueries.findStudyByStudyUID;
import static org.jooq.impl.DSL.*;

public class Studies {

    private Studies() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger LOG = Logger.getLogger(Studies.class.getName());

    public static PairListXTotalCount<Attributes> findAttributesByUserPKJOOQ(long callingUserPK, QIDOParams qidoParams, Connection connection)
            throws BadQueryParametersException {

        //queryParameters = ConvertDICOMKeyWordToDICOMTag(queryParameters);
        ArrayList<Condition> conditionArrayList = new ArrayList<>();

        Condition fromCondition = trueCondition();
        if (qidoParams.getAlbum_id().isPresent()) {
            fromCondition = ALBUM.PK.eq(qidoParams.getAlbum_id().get());
            conditionArrayList.add(fromCondition);
        }

        if (qidoParams.isFromInbox()) {
            fromCondition = ALBUM.PK.eq(USERS.INBOX_FK);
            conditionArrayList.add(fromCondition);
        }

        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);

        if (qidoParams.getStudyDateFilter().isPresent()) {
            conditionArrayList.add(createConditonStudyDate(qidoParams.getStudyDateFilter().get()));
        }
        if (qidoParams.getStudyTimeFilter().isPresent()) {
            conditionArrayList.add(createConditonStudyTime(qidoParams.getStudyTimeFilter().get()));
        }

        if (qidoParams.getAccessionNumberFilter().isPresent()) {
            conditionArrayList.add(createConditon(Tag.AccessionNumber, qidoParams.getAccessionNumberFilter().get(), STUDIES.ACCESSION_NUMBER, qidoParams.isFuzzyMatching()));
        }
        if (qidoParams.getReferringPhysicianNameFilter().isPresent()) {
            conditionArrayList.add(createConditon(Tag.ReferringPhysicianName, qidoParams.getReferringPhysicianNameFilter().get(), STUDIES.REFERRING_PHYSICIAN_NAME, qidoParams.isFuzzyMatching()));
        }
        if (qidoParams.getPatientNameFilter().isPresent()) {
            conditionArrayList.add(createConditon(Tag.PatientName, qidoParams.getPatientNameFilter().get(), STUDIES.PATIENT_NAME, qidoParams.isFuzzyMatching()));
        }
        if (qidoParams.getPatientIDFilter().isPresent()) {
            conditionArrayList.add(createConditon(Tag.PatientID, qidoParams.getPatientIDFilter().get(), STUDIES.PATIENT_ID, qidoParams.isFuzzyMatching()));
        }
        if (qidoParams.getStudyInstanceUIDFilter().isPresent()) {
            conditionArrayList.add(createConditon(Tag.StudyInstanceUID, qidoParams.getStudyInstanceUIDFilter().get(), STUDIES.STUDY_UID, qidoParams.isFuzzyMatching()));
        }
        if (qidoParams.getStudyIDFilter().isPresent()) {
            conditionArrayList.add(createConditon(Tag.StudyID, qidoParams.getStudyIDFilter().get(), STUDIES.STUDY_ID, qidoParams.isFuzzyMatching()));
        }

        if (qidoParams.getModalityFilter().isPresent()) {
            conditionArrayList.add(createConditonModality(qidoParams.getModalityFilter().get()));
        }


        SelectQuery query = create.selectQuery();
        query.addSelect(countDistinct(STUDIES.PK));
        query.addFrom(USERS);
        query.addJoin(ALBUM_USER, ALBUM_USER.USER_FK.eq(USERS.PK));
        query.addJoin(ALBUM, ALBUM.PK.eq(ALBUM_USER.ALBUM_FK));
        query.addJoin(ALBUM_SERIES, ALBUM_SERIES.ALBUM_FK.eq(ALBUM.PK));
        query.addJoin(SERIES, SERIES.PK.eq(ALBUM_SERIES.SERIES_FK));
        query.addJoin(STUDIES, STUDIES.PK.eq(SERIES.STUDY_FK));


        conditionArrayList.add(USERS.PK.eq(callingUserPK));
        conditionArrayList.add(SERIES.POPULATED.isTrue());
        conditionArrayList.add(STUDIES.POPULATED.isTrue());

        for (Condition c : conditionArrayList) {
            if (c != null) {
                query.addConditions(c);
            }
        }

        Integer studiesTotalCount = (Integer) query.fetch().getValues("count").get(0);

        query = create.selectQuery();
        query.addSelect(isnull(STUDIES.STUDY_UID, "NULL").as(STUDIES.STUDY_UID.getName()),
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
                isnull(count(SERIES.PK), 0).as("count:" + SERIES.PK.getName()),
                sum(SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES).as("sum:" + SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES.getName()),
                isnull(groupConcatDistinct(SERIES.MODALITY), "NULL").as("modalities"));
        query.addFrom(USERS);
        query.addJoin(ALBUM_USER, ALBUM_USER.USER_FK.eq(USERS.PK));
        query.addJoin(ALBUM, ALBUM.PK.eq(ALBUM_USER.ALBUM_FK));
        query.addJoin(ALBUM_SERIES, ALBUM_SERIES.ALBUM_FK.eq(ALBUM.PK));
        query.addJoin(SERIES, SERIES.PK.eq(ALBUM_SERIES.SERIES_FK));
        query.addJoin(STUDIES, STUDIES.PK.eq(SERIES.STUDY_FK));

        for (Condition c : conditionArrayList) {
            if (c != null) {
                query.addConditions(c);
            }
        }

        query.addGroupBy(STUDIES.STUDY_UID);


        query.addOrderBy(orderBy(qidoParams.getOrderByTag(), qidoParams.isDescending()));

        if (qidoParams.getLimit().isPresent()) {
            query.addLimit(qidoParams.getLimit().get());
        }

        if (qidoParams.getOffset().isPresent()) {
            query.addOffset(qidoParams.getOffset().get());
        }

        Result<Record14<String, String, String, String, String, String, String, String, String, String, String, Integer, BigDecimal, String>> result = query.fetch();

        List<Attributes> attributesList;
        attributesList = new ArrayList<>();

        for (Record r : result) {

            Attributes attributes = new Attributes();

            if (qidoParams.getModalityFilter().isPresent()) {
                //get all the modalities for the STUDY_UID
                String modalities = create.select(isnull(groupConcatDistinct(SERIES.MODALITY), "NULL"))
                        .from(USERS)
                        .join(ALBUM_USER).on(ALBUM_USER.USER_FK.eq(USERS.PK))
                        .join(ALBUM).on(ALBUM.PK.eq(ALBUM_USER.ALBUM_FK))
                        .join(ALBUM_SERIES).on(ALBUM_SERIES.ALBUM_FK.eq(ALBUM.PK))
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
            } else {
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

            safeAttributeSetString(attributes, Tag.InstanceAvailability, VR.CS, "ONLINE");

            attributesList.add(attributes);
        }
        return new PairListXTotalCount<>(studiesTotalCount, attributesList);
    }

    private static MultivaluedMap<String, String> convertDICOMKeyWordToDICOMTag(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.StudyDate))) {
            queryParameters.add(String.format("%08X", Tag.StudyDate), queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.StudyDate)).get(0));
        }
        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.StudyTime))) {
            queryParameters.add(String.format("%08X", Tag.StudyTime), queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.StudyTime)).get(0));
        }
        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.AccessionNumber))) {
            queryParameters.add(String.format("%08X", Tag.AccessionNumber), queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.AccessionNumber)).get(0));
        }
        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.ModalitiesInStudy))) {
            queryParameters.add(String.format("%08X", Tag.ModalitiesInStudy), queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.ModalitiesInStudy)).get(0));
        }
        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.ReferringPhysicianName))) {
            queryParameters.add(String.format("%08X", Tag.ReferringPhysicianName), queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.ReferringPhysicianName)).get(0));
        }
        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.PatientName))) {
            queryParameters.add(String.format("%08X", Tag.PatientName), queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.PatientName)).get(0));
        }
        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.PatientID))) {
            queryParameters.add(String.format("%08X", Tag.PatientID), queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.PatientID)).get(0));
        }
        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.StudyInstanceUID))) {
            queryParameters.add(String.format("%08X", Tag.StudyInstanceUID), queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.StudyInstanceUID)).get(0));
        }
        if (queryParameters.containsKey(org.dcm4che3.data.Keyword.valueOf(Tag.StudyID))) {
            queryParameters.add(String.format("%08X", Tag.StudyID), queryParameters.get(org.dcm4che3.data.Keyword.valueOf(Tag.StudyID)).get(0));
        }

        return queryParameters;
    }

    private static SortField orderBy(int orderBy, boolean descending) {
        TableField ord =  STUDIES.STUDY_DATE;

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

        return descending ? ord.desc() : ord.asc();
    }

    private static Condition createConditonModality(String filter) {
        if (filter.equalsIgnoreCase("null")) {
            return SERIES.MODALITY.isNull();
        } else {
            return SERIES.MODALITY.lower().equal(filter.toLowerCase());
        }
    }

    private static Condition createConditonStudyDate(String filter)
            throws BadQueryParametersException {
        return createIntervalConditon(filter, new CheckDate());
    }

    private static Condition createConditonStudyTime(String filter)
            throws BadQueryParametersException {
            return createIntervalConditon(filter, new CheckTime());
    }

    private static Condition createIntervalConditon(String parameter, CheckMethode checkMethode)
            throws BadQueryParametersException {
        if (parameter.contains("-")) {
            String begin;
            String end;
            String[] parameters = parameter.split("-");
            if (parameters.length == 2) {
                begin = parameters[0];
                end = parameters[1];
                if (begin.length() == 0) {
                    begin = checkMethode.intervalBegin();
                }
                checkMethode.check(begin);
                checkMethode.check(end);
                return checkMethode.getColumn().between(begin, end);

            }else if(parameters.length == 1) {
                begin = parameters[0];
                end = checkMethode.intervalEnd();
                checkMethode.check(begin);
                return checkMethode.getColumn().between(begin, end);
            } else {
                throw new BadRequestException(checkMethode.getColumn().getName() + ": " + parameter);
            }
        } else {
            checkMethode.check(parameter);
            return checkMethode.getColumn().eq(parameter);
        }
    }

    private interface CheckMethode {
        String intervalBegin();
        String intervalEnd();
        void check(String s) throws BadQueryParametersException;
        TableField getColumn();
    }

    private static class CheckTime implements CheckMethode {
        public String intervalBegin() {return "000000.000000";}
        public String intervalEnd() {return "235959.999999";}
        public TableField getColumn() {return STUDIES.STUDY_TIME;}

        public void check(String time) throws BadQueryParametersException {
            if (! time.matches("^(2[0-3]|[01][0-9])([0-5][0-9]){2}.[0-9]{6}$") ) {
                throw new BadQueryParametersException(org.dcm4che3.data.Keyword.valueOf(Tag.StudyTime) + " :" + time);
            }
        }
    }

    private static class CheckDate implements CheckMethode {
        public String intervalBegin() {return "00010101";}
        public String intervalEnd() {return "99991231";}
        public TableField getColumn() {return STUDIES.STUDY_DATE;}

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

    private static Condition createConditon(Integer tag,  String filter, TableField column, Boolean isFuzzyMatching)
            throws BadQueryParametersException {
        return createConditon(filter, Keyword.valueOf(tag), column, isFuzzyMatching);

    }

    private static Condition createConditon(String filter, String key, TableField column, Boolean isFuzzyMatching)
            throws BadQueryParametersException {
        String parameterNoStar = filter.replace("*", "")
                .replace(", ", "^")
                .replace(" ","^")
                .replace(",", "^");

        String parameter = filter;
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

            if (isFuzzyMatching) {
                Condition fuzzyCondition = condition("SOUNDEX(\""+parameterNoStar+"\") = SOUNDEX("+column.getName()+")");
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

    public static Study getStudy(String studyInstanceUID, EntityManager em) throws StudyNotFoundException{
        try {
            return findStudyByStudyUID(studyInstanceUID, em);
        } catch (NoResultException e) {
            throw new StudyNotFoundException("StudyInstanceUID : "+studyInstanceUID+" not found");
        }
    }

    public static boolean canAccessStudy(User user, Study study, EntityManager em) {
        try {
            StudyQueries.findStudyByStudyandUser(study, user, em);
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    public static boolean canAccessStudy(Album album, Study study, EntityManager em) {
        try {
            StudyQueries.findStudyByStudyandAlbum(study, album, em);
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }
}
