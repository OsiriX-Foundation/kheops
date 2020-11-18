package online.kheops.auth_server.study;

import online.kheops.auth_server.KheopsInstance;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.series.SeriesResponse;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;


public class StudyResponse {

    @XmlElement(name = "patient_name")
    private String patientName;
    @XmlElement(name = "patient_ID")
    private String patientID;
    @XmlElement(name = "study_uid")
    private String studyInstanceUID;
    @XmlElement(name = "study_date")
    private String studyDate;
    @XmlElement(name = "study_time")
    private String studyTime;
    @XmlElement(name = "timezone_offset_from_utc")
    private String timezoneOffsetFromUtc;
    @XmlElement(name = "study_description")
    private String studyDescription;
    @XmlElement(name = "study_id")
    private String studyId;
    @XmlElement(name = "accession_number")
    private String accessionNumber;
    @XmlElement(name = "referring_physician_name")
    private String referringPhysicianName;
    @XmlElement(name = "patient_birth_date")
    private String patientBirthDate;
    @XmlElement(name = "patient_sex")
    private String patientSex;

    @XmlElement(name = "retrieve_url")
    private String retrieveUrl;

    @XmlElement(name = "series")
    private List<SeriesResponse> series;

    private String kheopsInstance = null;

    private StudyResponse() { /*empty*/ }

    public StudyResponse(Study study, String kheopsInstance, boolean uidOnly) {
        studyInstanceUID = study.getStudyInstanceUID();
        if (uidOnly) { return; }
        patientName = study.getPatientName();
        studyDate = study.getStudyDate();
        studyDescription = study.getStudyDescription();
        patientID = study.getPatientID();
        patientBirthDate = study.getPatientBirthDate();
        patientSex = study.getPatientSex();
        referringPhysicianName = study.getReferringPhysicianName();
        accessionNumber = study.getAccessionNumber();
        studyId = study.getStudyID();
        timezoneOffsetFromUtc = study.getTimezoneOffsetFromUTC();
        studyTime = study.getStudyTime();
        if(kheopsInstance != null) {
            retrieveUrl = kheopsInstance + "/api/studies/" + studyInstanceUID;
            this.kheopsInstance = kheopsInstance;
        }
    }

    public void addSeries(Series series, Integer numberOfNewInstances) {
        if(this.series == null) {
            this.series = new ArrayList<>();
        }
        final SeriesResponse seriesResponse = new SeriesResponse(series, kheopsInstance, numberOfNewInstances, false);
        if(retrieveUrl == null) {
            seriesResponse.hideRetrieveUrl();
        }
        this.series.add(seriesResponse);
    }

    public void addSeries(Series series) {
        this.addSeries(series, 0);
    }


    public boolean containSeries() {
        if (series == null) {
            return false;
        } else {
            return !series.isEmpty();
        }
    }

    public void hideRetrieveUrl() {
        if (series != null) {
            series.forEach(seriesResponse -> hideRetrieveUrl());
        }
        retrieveUrl = null;
    }
}