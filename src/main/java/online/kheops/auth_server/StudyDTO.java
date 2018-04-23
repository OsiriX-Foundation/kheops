package online.kheops.auth_server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class StudyDTO {
    public List<SeriesDTO> getSeries() {
        return series;
    }

    private String studyDate;
    private String studyInstanceUID;
    private String studyTime;
    private String accessionNumber;
    private String referringPhysicianName;
    private String patientName;
    private String patientID;
    private String studyID;

    public String[] getModalities() {
        String[] modalities = new String[getSeries().size()];

        for (int i = 0; i < getSeries().size(); i++) {
            modalities[i] = getSeries().get(i).getModality();
        }

        return modalities;
    }

    private List<SeriesDTO> series = new ArrayList<>();

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public String getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
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

    public String getStudyID() {
        return studyID;
    }

    public void setStudyID(String studyID) {
        this.studyID = studyID;
    }
}
