package online.kheops.auth_server.entity;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity
@Table(name = "studies")
// Used a native query because Hibernate was not handling the distinct in SUM(DISTINCT s.modalityBitfield)
@NamedNativeQuery(
        name = "Study.AttributesByUserPK",
        query = "SELECT s2.study_uid, s2.study_date, s2.study_time, s2.timezone_offset_from_utc, s2.accession_number, s2.referring_physician_name, s2.patient_name, s2.patient_id, s2.patient_birth_date, s2.patient_sex, s2.study_id, COUNT(s.pk), SUM(s.number_of_series_related_instances), GROUP_CONCAT(DISTINCT s.modality SEPARATOR '\\\\') " +
                "FROM users u JOIN user_series us ON u.pk = us.user_fk JOIN series s ON us.series_fk = s.pk JOIN studies s2 on s.study_fk = s2.pk WHERE :userPK = u.pk AND s.populated = TRUE AND s2.populated = TRUE GROUP BY s2.pk")
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

    public static List<Attributes> findAttributesByUserPK(long userPK, EntityManager em) {
        List<Attributes> attributesList = new ArrayList<>();

        Query query = em.createNamedQuery("Study.AttributesByUserPK");
        query.setParameter("userPK", userPK);
        //noinspection unchecked
        List<Object[]> resultsList = (List<Object[]>) query.getResultList();

        // query = "SELECT s2.study_uid, s2.study_date, s2.study_time, s2.timezone_offset_from_utc, s2.accession_number, s2.referring_physician_name, s2.patient_name, s2.patient_id, s2.patient_birth_date, s2.patient_sex, s2.study_id, COUNT(s.pk), SUM(s.number_of_series_related_instances), SUM(DISTINCT s.modality_bitfield) FROM users u JOIN user_series us ON u.pk = us.user_fk JOIN series s ON us.series_fk = s.pk JOIN studies s2 on s.study_fk = s2.pk WHERE :userPK = u.pk AND s.populated = TRUE AND s2.populated = TRUE GROUP BY s2.pk")
        for (Object[] results: resultsList) {
            Attributes attributes = new Attributes();

            safeAttributeSetString(attributes, Tag.StudyInstanceUID, VR.UI, (String)results[0]);
            safeAttributeSetString(attributes, Tag.StudyDate, VR.DA, (String)results[1]);
            safeAttributeSetString(attributes, Tag.StudyTime, VR.TM, (String)results[2]);
            safeAttributeSetString(attributes, Tag.TimezoneOffsetFromUTC, VR.SH, (String)results[3]);
            safeAttributeSetString(attributes, Tag.AccessionNumber, VR.SH, (String)results[4]);
            safeAttributeSetString(attributes, Tag.ReferringPhysicianName, VR.PN, (String)results[5]);
            safeAttributeSetString(attributes, Tag.PatientName, VR.PN, (String)results[6]);
            safeAttributeSetString(attributes, Tag.PatientID, VR.LO, (String)results[7]);
            safeAttributeSetString(attributes, Tag.PatientBirthDate, VR.DA, (String)results[8]);
            safeAttributeSetString(attributes, Tag.PatientSex, VR.CS, (String)results[9]);
            safeAttributeSetString(attributes, Tag.StudyID, VR.SH, (String)results[10]);
            attributes.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS, ((BigInteger)results[11]).intValue());
            attributes.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS, ((BigDecimal)results[12]).intValue());
            attributes.setString(Tag.ModalitiesInStudy, VR.CS, (String)results[13]);

            safeAttributeSetString(attributes, Tag.InstanceAvailability, VR.CS, "ONLINE");

            attributesList.add(attributes);
        }

        return attributesList;
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
