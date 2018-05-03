package online.kheops.auth_server.entity;

import online.kheops.auth_server.StudyDTO;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "studies")
public class Study {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

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
    private Set<Series> series = new HashSet<Series>();

    // only merges values pertaining to the study, not the series list
    public void mergeStudyDTO(StudyDTO studyDTO) {
        setStudyDate(studyDTO.getStudyDate());
        setStudyTime(studyDTO.getStudyTime());
        setAccessionNumber(studyDTO.getAccessionNumber());
        setReferringPhysicianName(studyDTO.getReferringPhysicianName());
        setPatientName(studyDTO.getPatientName());
        setPatientID(studyDTO.getPatientID());
        setStudyID(studyDTO.getStudyID());

        setPopulated(true);
    }

    public StudyDTO createStudyDTO() {
        StudyDTO studyDTO = new StudyDTO();

        studyDTO.setStudyInstanceUID(this.getStudyInstanceUID());
        studyDTO.setStudyDate(this.getStudyDate());
        studyDTO.setStudyTime(this.getStudyTime());
        studyDTO.setAccessionNumber(this.getAccessionNumber());
        studyDTO.setReferringPhysicianName(this.getReferringPhysicianName());
        studyDTO.setPatientName(this.getPatientName());
        studyDTO.setPatientID(this.getPatientID());
        studyDTO.setStudyID(this.getStudyID());

        return studyDTO;
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

    private void safeAttributeSetString(Attributes attributes, int tag, VR vr, String string) {
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
}
