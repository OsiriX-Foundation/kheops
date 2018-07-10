package online.kheops.auth_server.entity;

import online.kheops.auth_server.resource.TokenResource;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Keyword;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.jooq.*;
import org.jooq.impl.DSL;

import javax.persistence.*;
import javax.persistence.Table;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Calendar.*;
import static online.kheops.auth_server.generated.tables.Series.SERIES;
import static online.kheops.auth_server.generated.tables.Studies.STUDIES;
import static online.kheops.auth_server.generated.tables.UserSeries.USER_SERIES;
import static online.kheops.auth_server.generated.tables.Users.USERS;
import static org.dcm4che3.data.Tag.DateTime;
import static org.jooq.impl.DSL.*;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity
@Table(name = "studies")
// Used a native query because Hibernate was not handling the distinct in SUM(DISTINCT s.modalityBitfield)
@NamedNativeQuery(
        name = "Study.AttributesByUserPK",
        query = "SELECT s2.study_uid, s2.study_date, s2.study_time, s2.timezone_offset_from_utc, s2.accession_number, s2.referring_physician_name, s2.patient_name, s2.patient_id, s2.patient_birth_date, s2.patient_sex, s2.study_id, COUNT(s.pk), SUM(s.number_of_series_related_instances), GROUP_CONCAT(DISTINCT s.modality SEPARATOR '\\\\') FROM users u JOIN user_series us ON u.pk = us.user_fk JOIN series s ON us.series_fk = s.pk JOIN studies s2 on s.study_fk = s2.pk WHERE :userPK = u.pk AND s.populated = TRUE AND s2.populated = TRUE GROUP BY s2.pk")
public class Study {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @Basic(optional = false)
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Basic(optional = false)
    @Column(name = "study_uid", updatable = false)
    private String studyInstanceUID;

    @Column(name = "study_date")
    private String studyDate;

    @Column(name = "study_time")
    private String studyTime;

    @Column(name = "timezone_offset_from_utc")
    private String timezoneOffsetFromUTC;

    @Column(name = "accession_number")
    private String accessionNumber;

    @Column(name = "referring_physician_name")
    private String referringPhysicianName;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "patient_id")
    private String patientID;

    @Column(name = "patient_birth_date")
    private String patientBirthDate;

    @Column(name = "patient_sex")
    private String patientSex;

    @Column(name = "study_id")
    private String studyID;

    @Basic(optional = false)
    @Column(name = "populated")
    private boolean populated = false;

    @OneToMany
    @JoinColumn (name = "study_fk", nullable=false)
    private Set<Series> series = new HashSet<>();

    @PrePersist
    public void onPrePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        createdTime = now;
        updatedTime = now;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    private static final Logger LOG = Logger.getLogger(TokenResource.class.getName());

    public static Pair findAttributesByUserPKJOOQ(long userPK, MultivaluedMap<String, String> queryParameters, Connection connection) {

        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);

        ArrayList<Condition> conditionArrayList = new ArrayList<>();

        conditionArrayList.add(createConditonStudyDate(queryParameters));
        conditionArrayList.add(createConditonStudyTime(queryParameters));

        conditionArrayList.add(createConditon(Tag.AccessionNumber, queryParameters, "AccessionNumber", STUDIES.ACCESSION_NUMBER));
        conditionArrayList.add(createConditonModality(queryParameters));
        conditionArrayList.add(createConditon(Tag.ReferringPhysicianName, queryParameters, "ReferringPhysicianName", STUDIES.REFERRING_PHYSICIAN_NAME));
        conditionArrayList.add(createConditon(Tag.PatientName, queryParameters, "PatientName", STUDIES.PATIENT_NAME));
        conditionArrayList.add(createConditon(Tag.PatientID, queryParameters, "PatientID", STUDIES.PATIENT_ID));
        conditionArrayList.add(createConditon(Tag.StudyInstanceUID, queryParameters, "StudyInstanceUID", STUDIES.STUDY_UID));
        conditionArrayList.add(createConditon(Tag.StudyID, queryParameters, "StudyID", STUDIES.STUDY_ID));

        SelectQuery query = create.selectQuery();
        query.addSelect(countDistinct(STUDIES.PK));
        query.addFrom(USERS);
        query.addJoin(USER_SERIES, USER_SERIES.USER_FK.eq(USERS.PK));
        query.addJoin(SERIES, SERIES.PK.eq(USER_SERIES.SERIES_FK));
        query.addJoin(STUDIES, STUDIES.PK.eq(SERIES.STUDY_FK));
        query.addConditions(USERS.PK.eq(userPK));
        query.addConditions(SERIES.POPULATED.isTrue());
        query.addConditions(STUDIES.POPULATED.isTrue());

        for (Condition c :  conditionArrayList) {
            if (c != null) {
                query.addConditions(c);
            }
        }

        Integer studiesTotalCount = (Integer)query.fetch().getValues("count").get(0);

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
                isnull(count(SERIES.PK), 0).as("count:"+SERIES.PK.getName()),
                sum(SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES).as("sum:"+SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES.getName()),
                isnull(groupConcatDistinct(SERIES.MODALITY), "NULL").as("modalities"));
        query.addFrom(USERS);
        query.addJoin(USER_SERIES, USER_SERIES.USER_FK.eq(USERS.PK));
        query.addJoin(SERIES, SERIES.PK.eq(USER_SERIES.SERIES_FK));
        query.addJoin(STUDIES, STUDIES.PK.eq(SERIES.STUDY_FK));
        query.addConditions(USERS.PK.eq(userPK));
        query.addConditions(SERIES.POPULATED.isTrue());
        query.addConditions(STUDIES.POPULATED.isTrue());

        for (Condition c :  conditionArrayList) {
            if (c != null) {
                query.addConditions(c);
            }
        }

        query.addGroupBy(STUDIES.STUDY_UID);
        query.addOrderBy(orderBy(queryParameters));

        if (queryParameters.containsKey("limit")) {
                Integer limit;
                try {
                    limit = Integer.parseInt(queryParameters.get("limit").get(0));
                } catch (Exception e) {
                    throw new BadRequestException("limit: " + queryParameters.get("limit").get(0));
                }
                if (limit < 1) {
                    throw new BadRequestException("limit: " + queryParameters.get("limit").get(0));
                }
                query.addLimit(limit);
        }

        if (queryParameters.containsKey("offset")) {
            Integer offset;
            try {
                offset = Integer.parseInt(queryParameters.get("offset").get(0));
            } catch (Exception e) {
                throw new BadRequestException("offset: " + queryParameters.get("offset").get(0));
            }

            if (offset < 0) {
                throw new BadRequestException("offset: " + queryParameters.get("offset").get(0));
            }
            query.addOffset(offset);
        }

        Result<Record14<String, String, String, String, String, String, String, String, String, String, String, Integer, BigDecimal, String>> result = query.fetch();

        List<Attributes> attributesList;
        attributesList = new ArrayList<>();

        for (Record r : result) {

            Attributes attributes = new Attributes();

            if(queryParameters.containsKey("ModalitiesInStudy")) {
                //get all the modalities for the STUDY_UID
                String modalities = create.select(isnull(groupConcatDistinct(SERIES.MODALITY), "NULL"))
                        .from(USERS)
                        .join(USER_SERIES)
                        .on(USER_SERIES.USER_FK.eq(USERS.PK))
                        .join(SERIES)
                        .on(SERIES.PK.eq(USER_SERIES.SERIES_FK))
                        .join(STUDIES)
                        .on(STUDIES.PK.eq(SERIES.STUDY_FK))
                        .where(USERS.PK.eq(userPK))
                        .and(SERIES.POPULATED.isTrue())
                        .and(STUDIES.POPULATED.isTrue())
                        .and(STUDIES.STUDY_UID.eq(r.getValue(0).toString()))
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
            attributes.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS, ((Integer)r.getValue("count:"+SERIES.PK.getName())));
            attributes.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS, (((BigDecimal)r.getValue("sum:"+SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES.getName()))).intValue());

            safeAttributeSetString(attributes, Tag.InstanceAvailability, VR.CS, "ONLINE");

            attributesList.add(attributes);
        }
        return new Pair(studiesTotalCount, attributesList);
    }

    private static SortField orderBy(MultivaluedMap<String, String> queryParameters) {

        if (queryParameters.containsKey("sort")) {
            TableField ord = STUDIES.STUDY_DATE;

            Boolean asc_desc = queryParameters.get("sort").get(0).startsWith("-");
            String orderByParameter = queryParameters.get("sort").get(0).replace("-", "");

            if (orderByParameter.compareTo(Keyword.valueOf(Tag.StudyDate)) == 0 || orderByParameter.compareTo(String.format("%08X",Tag.StudyDate)) == 0) ord = STUDIES.STUDY_DATE;
            else if (orderByParameter.compareTo(Keyword.valueOf(Tag.StudyTime)) == 0 || orderByParameter.compareTo(String.format("%08X",Tag.StudyTime)) == 0) ord = STUDIES.STUDY_TIME;
            else if (orderByParameter.compareTo(Keyword.valueOf(Tag.AccessionNumber)) == 0 || orderByParameter.compareTo(String.format("%08X",Tag.AccessionNumber)) == 0) ord = STUDIES.ACCESSION_NUMBER;
            //ModalitiesInStudy
            else if (orderByParameter.compareTo(Keyword.valueOf(Tag.ReferringPhysicianName)) == 0 || orderByParameter.compareTo(String.format("%08X",Tag.ReferringPhysicianName)) == 0) ord = STUDIES.REFERRING_PHYSICIAN_NAME;
            else if (orderByParameter.compareTo(Keyword.valueOf(Tag.PatientName)) == 0 || orderByParameter.compareTo(String.format("%08X",Tag.PatientName)) == 0) ord = STUDIES.PATIENT_NAME;
            else if (orderByParameter.compareTo(Keyword.valueOf(Tag.PatientID)) == 0 || orderByParameter.compareTo(String.format("%08X",Tag.PatientID)) == 0) ord = STUDIES.PATIENT_ID;
            else if (orderByParameter.compareTo(Keyword.valueOf(Tag.StudyInstanceUID)) == 0 || orderByParameter.compareTo(String.format("%08X",Tag.StudyInstanceUID)) == 0) ord = STUDIES.STUDY_UID;
            else if (orderByParameter.compareTo(Keyword.valueOf(Tag.StudyID)) == 0 || orderByParameter.compareTo(String.format("%08X",Tag.StudyID)) == 0) ord = STUDIES.STUDY_ID;
            else throw new BadRequestException("sort: " + queryParameters.get("sort").get(0));

            return asc_desc ? ord.desc() : ord.asc();
        }
        //Default sort
        return STUDIES.STUDY_DATE.desc();
    }

    private static Condition createConditonModality(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey("ModalitiesInStudy")) {
            String parameter = queryParameters.get("ModalitiesInStudy").get(0);
            if (parameter.equalsIgnoreCase("null")) {
                return SERIES.MODALITY.isNull();
            } else {
                return SERIES.MODALITY.lower().equal(parameter.toLowerCase());
            }
        } else if (queryParameters.containsKey(String.format("%08X",Tag.ModalitiesInStudy))) {
            String parameter = queryParameters.get(String.format("%08X",Tag.ModalitiesInStudy)).get(0);
            if (parameter.equalsIgnoreCase("null")) {
                return SERIES.MODALITY.isNull();
            } else {
                return SERIES.MODALITY.lower().equal(parameter.toLowerCase());
            }
        }
        return null;
    }

    private static Condition createConditonStudyDate(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey("StudyDate")) {
            return createIntervalConditon(queryParameters.get("StudyDate").get(0), "00010101", "99991231", STUDIES.STUDY_DATE, new checkDate());
        } else if (queryParameters.containsKey(String.format("%08X",Tag.StudyDate))) {
            return createIntervalConditon(queryParameters.get(String.format("%08X",Tag.StudyDate)).get(0), "00010101", "99991231", STUDIES.STUDY_DATE, new checkDate());
        } else {
            return null;
        }
    }

    private static Condition createConditonStudyTime(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey("StudyTime")) {
            return createIntervalConditon(queryParameters.get("StudyTime").get(0), "000000.000000", "235959.999999", STUDIES.STUDY_TIME, new checkTime());
        } else if (queryParameters.containsKey(String.format("%08X",Tag.StudyTime))) {
            return createIntervalConditon(queryParameters.get(String.format("%08X",Tag.StudyTime)).get(0), "000000.000000", "235959.999999", STUDIES.STUDY_TIME, new checkTime());
        } else {
            return null;
        }
    }

    private static Condition createIntervalConditon(String parameter, String intervalBegin, String intervalEnd, TableField column, CheckMethode checkMethode) {
        if (parameter.contains("-")) {
            String begin;
            String end;
            String[] parameters = parameter.split("-");
            if (parameters.length == 2) {
                begin = parameters[0];
                end = parameters[1];
                if (begin.length() == 0) {
                    begin = intervalBegin;
                }
                if (checkMethode.check(begin) && checkMethode.check(end)) {
                    return column.between(begin, end);
                } else {
                    throw new BadRequestException(column.getName() + ": " + parameter);
                }
            }else if(parameters.length == 1) {
                begin = parameters[0];
                end = intervalEnd;
                if (checkMethode.check(begin) && checkMethode.check(end)) {
                    return column.between(begin, end);
                } else {
                    throw new BadRequestException(column.getName() + ": " + parameter);
                }
            } else {
                throw new BadRequestException(column.getName() + ": " + parameter);
            }
        } else {
            if (checkMethode.check(parameter)) {
                return column.eq(parameter);
            } else {
                throw new BadRequestException(column.getName() + ": " + parameter);
            }
        }
    }

    private interface CheckMethode {
        public Boolean check(String s);
    }

    private static class checkTime implements CheckMethode {
        public Boolean check(String time) {
            return time.matches("^(2[0-3]|[01][0-9])([0-5][0-9]){2}.[0-9]{6}$");
        }
    }

    private static class checkDate implements CheckMethode {
        public Boolean check(String date) {
            if (date.matches("^([0-9]{4})(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                dateFormat.setLenient(false);
                try {
                    dateFormat.parse(date);
                } catch (Exception e) {
                    throw new BadRequestException("StudyDate :" + date);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private static Condition createConditon(Integer tag,  MultivaluedMap<String, String> queryParameters, String key, TableField column) {
        if (queryParameters.containsKey(key)) {
            return createConditon(queryParameters, key, column);
        } else if (queryParameters.containsKey(String.format("%08X",tag))) {
            return createConditon(queryParameters, String.format("%08X",tag), column);
        }
        return null;
    }

    private static Condition createConditon(MultivaluedMap<String, String> queryParameters, String key, TableField column) {
        String parameterNoStar = queryParameters.get(key).get(0).replace("*", "");
        String parameter = queryParameters.get(key).get(0);
        if (parameterNoStar.length() == 0) {
            return null;
        }

        if ("null".equalsIgnoreCase(parameterNoStar)) {
            return column.isNull();
        } else {
            if (parameter.startsWith("*") && parameter.endsWith("*")) {
                return column.lower().contains(parameterNoStar.toLowerCase());
            } else if (parameter.startsWith("*")) {
                return column.lower().endsWith(parameterNoStar.toLowerCase());
            } else if (parameter.endsWith("*")) {
                return column.lower().startsWith(parameterNoStar.toLowerCase());
            } else {
                return column.lower().equal(parameterNoStar.toLowerCase());
            }
        }
    }

    // This method does not set Tag.NumberOfStudyRelatedSeries, Tag.NumberOfStudyRelatedInstances, Tag.ModalitiesInStudy
    public Attributes getAttributes() {
        if (!isPopulated()) {
            throw new IllegalStateException();
        }

        Attributes attributes = new Attributes();

        safeAttributeSetString(attributes, Tag.StudyDate, VR.DA, getStudyDate());
        safeAttributeSetString(attributes, Tag.StudyTime, VR.TM, getStudyTime());
        safeAttributeSetString(attributes, Tag.TimezoneOffsetFromUTC, VR.SH, getTimezoneOffsetFromUTC());
        safeAttributeSetString(attributes, Tag.AccessionNumber, VR.SH, getAccessionNumber());
        safeAttributeSetString(attributes, Tag.ReferringPhysicianName, VR.PN, getReferringPhysicianName());
        safeAttributeSetString(attributes, Tag.PatientName, VR.PN, getPatientName());
        safeAttributeSetString(attributes, Tag.PatientID, VR.LO, getPatientID());
        safeAttributeSetString(attributes, Tag.PatientBirthDate, VR.DA, getPatientBirthDate());
        safeAttributeSetString(attributes, Tag.PatientSex, VR.CS, getPatientSex());
        safeAttributeSetString(attributes, Tag.StudyInstanceUID, VR.UI, getStudyInstanceUID());
        safeAttributeSetString(attributes, Tag.StudyID, VR.SH, getStudyID());
        safeAttributeSetString(attributes, Tag.InstanceAvailability, VR.CS, "ONLINE");

        return attributes;
    }

    private static void safeAttributeSetString(Attributes attributes, int tag, VR vr, String string) {
        if (string != null) {
            attributes.setString(tag, vr, string);
        }
    }

    // this method does not set populated, but the calling method will probably need to
    public void mergeAttributes(Attributes attributes) {
        setStudyDate(attributes.getString(Tag.StudyDate, getStudyDate()));
        setStudyTime(attributes.getString(Tag.StudyTime, getStudyTime()));
        setTimezoneOffsetFromUTC(attributes.getString(Tag.TimezoneOffsetFromUTC, getTimezoneOffsetFromUTC()));
        setAccessionNumber(attributes.getString(Tag.AccessionNumber, getAccessionNumber()));
        setReferringPhysicianName(attributes.getString(Tag.ReferringPhysicianName, getReferringPhysicianName()));
        setPatientName(attributes.getString(Tag.PatientName, getPatientName()));
        setPatientID(attributes.getString(Tag.PatientID, getPatientID()));
        setPatientBirthDate(attributes.getString(Tag.PatientBirthDate, getPatientBirthDate()));
        setPatientSex(attributes.getString(Tag.PatientSex, getPatientSex()));
        setStudyID(attributes.getString(Tag.StudyID, getStudyID()));
    }

    public long getPk() {
        return pk;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
    }

    public String getTimezoneOffsetFromUTC() {
        return timezoneOffsetFromUTC;
    }

    public void setTimezoneOffsetFromUTC(String timezoneOffsetFromUTC) {
        this.timezoneOffsetFromUTC = timezoneOffsetFromUTC;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getReferringPhysicianName() {
        return referringPhysicianName;
    }

    public void setReferringPhysicianName(String referringPhysicianName) {
        this.referringPhysicianName = referringPhysicianName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(String patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    public String getStudyID() {
        return studyID;
    }

    public void setStudyID(String studyID) {
        this.studyID = studyID;
    }

    public boolean isPopulated() {
        return populated;
    }

    public void setPopulated(boolean populated) {
        this.populated = populated;
    }

    public Set<Series> getSeries() {
        return series;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }
}



