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
    private String timezoneOffsetFromUTC;
    private String accessionNumber;
    private String referringPhysicianName;
    private String patientName;
    private String patientID;
    private String patientBirthDate;
    private String patientSex;
    private String studyID;

    public String[] getModalities() {
        String[] modalities = new String[getSeries().size()];

        for (int i = 0; i < getSeries().size(); i++) {
            modalities[i] = getSeries().get(i).getModality();
        }

        return modalities;
    }

    public int getNumberOfInstances() {
        int instances = 0;

        for (SeriesDTO seriesDTO: getSeries()) {
            instances += seriesDTO.getNumberOfSeriesRelatedInstances();
        }

        return instances;
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
}
