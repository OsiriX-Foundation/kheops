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
import javax.ws.rs.core.MultivaluedMap;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static online.kheops.auth_server.generated.tables.Series.SERIES;
import static online.kheops.auth_server.generated.tables.Studies.STUDIES;
import static online.kheops.auth_server.generated.tables.UserSeries.USER_SERIES;
import static online.kheops.auth_server.generated.tables.Users.USERS;
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

        TableField ord = STUDIES.STUDY_DATE;
        if (queryParameters.containsKey("sort")) {
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
        }

        SortField orderBy = ord.desc();
        if (queryParameters.containsKey("sort")) {
            if (queryParameters.get("sort").get(0).startsWith("-")) {
                orderBy = ord.desc();
            } else {
                orderBy = ord.asc();
            }
        }

        Integer limit = Integer.MAX_VALUE;
        if (queryParameters.containsKey("limit")) {
            limit = Integer.parseInt(queryParameters.get("limit").get(0));
            if (limit < 1) {
                limit = 1;
            }
        }

        Integer offset = 0;
        if (queryParameters.containsKey("offset")) {
             String i = queryParameters.get("offset").get(0);
            offset = limit * Integer.parseInt(queryParameters.get("offset").get(0));
            if (offset < 0) {
                offset = 0;
            }
        }

        String studyDateBegin = "00000000";
        String studyDateEnd = "99999999";
        if (queryParameters.containsKey("StudyDate")) {
            if(queryParameters.get("StudyDate").get(0).equalsIgnoreCase("NULL")) {
                studyDateBegin = studyDateEnd = "NULL";
            } else {
                studyDateBegin = queryParameters.get("StudyDate").get(0).split("-")[0];
                studyDateEnd = queryParameters.get("StudyDate").get(0).split("-")[1];
            }
        } else if (queryParameters.containsKey(String.format("%08X",Tag.StudyDate))) {
            if(queryParameters.get(String.format("%08X",Tag.StudyDate)).get(0).equalsIgnoreCase("NULL")) {
                studyDateBegin = studyDateEnd = "NULL";
            } else {
                studyDateBegin = queryParameters.get(String.format("%08X", Tag.StudyDate)).get(0).split("-")[0];
                studyDateEnd = queryParameters.get(String.format("%08X", Tag.StudyDate)).get(0).split("-")[1];
            }
        }

        String studyTimeBegin = "000000.000000";
        String studyTimeEnd = "235959.999999";
        if (queryParameters.containsKey("StudyTime")) {
            if(queryParameters.get("StudyTime").get(0).equalsIgnoreCase("NULL")) {
                studyTimeBegin = studyTimeEnd = "NULL";
            } else {
                studyTimeBegin = queryParameters.get("StudyTime").get(0).split("-")[0];
                studyTimeEnd = queryParameters.get("StudyTime").get(0).split("-")[1];
            }
        } else if (queryParameters.containsKey(String.format("%08X",Tag.StudyTime))) {
            if(queryParameters.get(String.format("%08X",Tag.StudyTime)).get(0).equalsIgnoreCase("NULL")) {
                studyTimeBegin = studyTimeEnd = "NULL";
            } else {
                studyTimeBegin = queryParameters.get(String.format("%08X", Tag.StudyTime)).get(0).split("-")[0];
                studyTimeEnd = queryParameters.get(String.format("%08X", Tag.StudyTime)).get(0).split("-")[1];
            }
        }

        String accessionNumber = "";
        if (queryParameters.containsKey("AccessionNumber")) {
            accessionNumber = queryParameters.get("AccessionNumber").get(0).replace("*", "");
        } else if (queryParameters.containsKey(String.format("%08X",Tag.AccessionNumber))) {
            accessionNumber = queryParameters.get(String.format("%08X",Tag.AccessionNumber)).get(0).replace("*", "");
        }

        String modalitiesInStudy = "";
        if (queryParameters.containsKey("ModalitiesInStudy")) {
            modalitiesInStudy = queryParameters.get("ModalitiesInStudy").get(0);
        } else if (queryParameters.containsKey(String.format("%08X",Tag.ModalitiesInStudy))) {
            modalitiesInStudy = queryParameters.get(String.format("%08X",Tag.ModalitiesInStudy)).get(0);
        }

        String referringPhysicianName = "";
        if (queryParameters.containsKey("ReferringPhysicianName")) {
            referringPhysicianName = queryParameters.get("ReferringPhysicianName").get(0).replace("*", "");
        } else if (queryParameters.containsKey(String.format("%08X",Tag.ReferringPhysicianName))) {
            referringPhysicianName = queryParameters.get(String.format("%08X",Tag.ReferringPhysicianName)).get(0).replace("*", "");
        }

        String patientName = "";
        if (queryParameters.containsKey("PatientName")) {
            patientName = queryParameters.get("PatientName").get(0).replace("*", "");
        } else if (queryParameters.containsKey(String.format("%08X",Tag.PatientName))) {
            patientName = queryParameters.get(String.format("%08X",Tag.PatientName)).get(0).replace("*", "");
        }

        String patientID = "";
        if (queryParameters.containsKey("PatientID")) {
            patientID = queryParameters.get("PatientID").get(0).replace("*", "");
        } else if (queryParameters.containsKey(String.format("%08X",Tag.PatientID))) {
            patientID = queryParameters.get(String.format("%08X",Tag.PatientID)).get(0).replace("*", "");
        }

        String studyInstanceUID = "";
        if (queryParameters.containsKey("StudyInstanceUID")) {
            studyInstanceUID = queryParameters.get("StudyInstanceUID").get(0).replace("*", "");
        } else if (queryParameters.containsKey(String.format("%08X",Tag.StudyInstanceUID))) {
            studyInstanceUID = queryParameters.get(String.format("%08X",Tag.StudyInstanceUID)).get(0).replace("*", "");
        }

        String studyID = "";
        if (queryParameters.containsKey("StudyID")) {
            studyID = queryParameters.get("StudyID").get(0).replace("*", "");
        } else if (queryParameters.containsKey(String.format("%08X",Tag.StudyID))) {
            studyID = queryParameters.get(String.format("%08X",Tag.PatientName)).get(0).replace("*", "");
        }

        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);

        Integer studiesTotalCount = (Integer)create
                .select(countDistinct(STUDIES.PK))
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
                    .and(STUDIES.STUDY_DATE.between(studyDateBegin, studyDateEnd).or(STUDIES.STUDY_DATE.isNull().and(studyDateBegin.equalsIgnoreCase("00000000")).and(studyDateEnd.equalsIgnoreCase("99999999"))).or(STUDIES.STUDY_DATE.isNull().and("NULL".startsWith(studyDateBegin.toUpperCase())).and("NULL".startsWith(studyDateEnd.toUpperCase()))))
                    .and(STUDIES.STUDY_TIME.between(studyTimeBegin, studyTimeEnd).or(STUDIES.STUDY_TIME.isNull().and(studyTimeBegin.equalsIgnoreCase("000000.000000")).and(studyTimeEnd.equalsIgnoreCase("235959.999999"))).or(STUDIES.STUDY_TIME.isNull().and("NULL".startsWith(studyTimeBegin.toUpperCase())).and("NULL".startsWith(studyTimeEnd.toUpperCase()))))
                    .and(STUDIES.ACCESSION_NUMBER.startsWith(accessionNumber).or(STUDIES.ACCESSION_NUMBER.isNull().and(accessionNumber.equalsIgnoreCase(""))).or(STUDIES.ACCESSION_NUMBER.isNull().and("NULL".startsWith(accessionNumber.toUpperCase()))))
                    .and(SERIES.MODALITY.lower().startsWith(modalitiesInStudy.toLowerCase()).or(SERIES.MODALITY.isNull().and(modalitiesInStudy.equalsIgnoreCase(""))).or(SERIES.MODALITY.isNull().and("NULL".startsWith(modalitiesInStudy.toUpperCase()))))
                    .and(STUDIES.REFERRING_PHYSICIAN_NAME.lower().startsWith(referringPhysicianName.toLowerCase()).or(STUDIES.REFERRING_PHYSICIAN_NAME.isNull().and(referringPhysicianName.equalsIgnoreCase(""))).or(STUDIES.REFERRING_PHYSICIAN_NAME.isNull().and("NULL".startsWith(referringPhysicianName.toUpperCase()))))
                    .and(STUDIES.PATIENT_NAME.lower().startsWith(patientName.toLowerCase()).or(STUDIES.PATIENT_NAME.isNull().and(patientName.equalsIgnoreCase(""))).or(STUDIES.PATIENT_NAME.isNull().and("NULL".startsWith(patientName.toUpperCase()))))
                    .and(STUDIES.PATIENT_ID.lower().startsWith(patientID.toLowerCase()).or(STUDIES.PATIENT_ID.isNull().and(patientID.equalsIgnoreCase(""))).or(STUDIES.PATIENT_ID.isNull().and("NULL".startsWith(patientID.toUpperCase()))))
                    .and(STUDIES.STUDY_UID.startsWith(studyInstanceUID))
                    .and(STUDIES.STUDY_ID.startsWith(studyID).or(STUDIES.STUDY_ID.isNull().and(studyID.equalsIgnoreCase(""))).or(STUDIES.STUDY_ID.isNull().and("NULL".startsWith(studyID.toUpperCase()))))
                .fetch().get(0).get(0);

        Boolean b = modalitiesInStudy.equalsIgnoreCase("");
        Integer lastPage =  (int) Math.ceil( (double)studiesTotalCount / limit);

        if (offset > lastPage) {
            offset = lastPage;
        }

        @SuppressWarnings("unchecked") Result<Record14<String, String, String, String, String, String, String, String, String, String, String, Integer, BigDecimal, String>> result = create
                .select(isnull(STUDIES.STUDY_UID, "NULL").as(STUDIES.STUDY_UID.getName()),
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
                        isnull(groupConcatDistinct(SERIES.MODALITY), "NULL").as("modalities"))
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
                    .and(STUDIES.STUDY_DATE.between(studyDateBegin, studyDateEnd).or(STUDIES.STUDY_DATE.isNull().and(studyDateBegin.equalsIgnoreCase("00000000")).and(studyDateEnd.equalsIgnoreCase("99999999"))).or(STUDIES.STUDY_DATE.isNull().and("NULL".startsWith(studyDateBegin.toUpperCase())).and("NULL".startsWith(studyDateEnd.toUpperCase()))))
                    .and(STUDIES.STUDY_TIME.between(studyTimeBegin, studyTimeEnd).or(STUDIES.STUDY_TIME.isNull().and(studyTimeBegin.equalsIgnoreCase("000000.000000")).and(studyTimeEnd.equalsIgnoreCase("235959.999999"))).or(STUDIES.STUDY_TIME.isNull().and("NULL".startsWith(studyTimeBegin.toUpperCase())).and("NULL".startsWith(studyTimeEnd.toUpperCase()))))
                    .and(STUDIES.ACCESSION_NUMBER.startsWith(accessionNumber).or(STUDIES.ACCESSION_NUMBER.isNull().and(accessionNumber.equalsIgnoreCase(""))).or(STUDIES.ACCESSION_NUMBER.isNull().and("NULL".startsWith(accessionNumber.toUpperCase()))))
                    .and(SERIES.MODALITY.lower().startsWith(modalitiesInStudy.toLowerCase()).or(SERIES.MODALITY.isNull().and(modalitiesInStudy.equalsIgnoreCase(""))).or(SERIES.MODALITY.isNull().and("NULL".startsWith(modalitiesInStudy.toUpperCase()))))
                    .and(STUDIES.REFERRING_PHYSICIAN_NAME.lower().startsWith(referringPhysicianName.toLowerCase()).or(STUDIES.REFERRING_PHYSICIAN_NAME.isNull().and(referringPhysicianName.equalsIgnoreCase(""))).or(STUDIES.REFERRING_PHYSICIAN_NAME.isNull().and("NULL".startsWith(referringPhysicianName.toUpperCase()))))
                    .and(STUDIES.PATIENT_NAME.lower().startsWith(patientName.toLowerCase()).or(STUDIES.PATIENT_NAME.isNull().and(patientName.equalsIgnoreCase(""))).or(STUDIES.PATIENT_NAME.isNull().and("NULL".startsWith(patientName.toUpperCase()))))
                    .and(STUDIES.PATIENT_ID.lower().startsWith(patientID.toLowerCase()).or(STUDIES.PATIENT_ID.isNull().and(patientID.equalsIgnoreCase(""))).or(STUDIES.PATIENT_ID.isNull().and("NULL".startsWith(patientID.toUpperCase()))))
                    .and(STUDIES.STUDY_UID.startsWith(studyInstanceUID))
                    .and(STUDIES.STUDY_ID.startsWith(studyID).or(STUDIES.STUDY_ID.isNull().and(studyID.equalsIgnoreCase(""))).or(STUDIES.STUDY_ID.isNull().and("NULL".startsWith(studyID.toUpperCase()))))
                .groupBy(STUDIES.STUDY_UID)
                .orderBy(orderBy)
                .offset(offset).limit(limit)
                .fetch();

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
