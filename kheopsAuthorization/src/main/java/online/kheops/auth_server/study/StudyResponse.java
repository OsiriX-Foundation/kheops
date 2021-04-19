package online.kheops.auth_server.study;

import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.series.SeriesResponse;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    private StudyResponse() { /*empty*/ }

    public static class Builder {

        private Study study;
        private boolean showRetrieveUrl;
        private boolean showRetrieveUrlStudyOnly;
        private boolean uidOnly;
        private String kheopsInstance;
        private HashMap<Series, Integer> seriesLst;

        public Builder(Study study) {
            this.study = study;
            this.seriesLst = new HashMap<>();
            showRetrieveUrl = false;
            uidOnly = false;
        }

        public Builder showRetrieveUrl() {
            this.showRetrieveUrl = true;
            return this;
        }
        public Builder showRetrieveUrl(boolean showRetrieveUrl) {
            this.showRetrieveUrl = showRetrieveUrl;
            return this;
        }
        public Builder hideRetrieveUrl() {
            this.showRetrieveUrl = false;
            return this;
        }

        public Builder hideRetrieveUrlSeriesOnly() {
            this.showRetrieveUrlStudyOnly = true;
            return this;
        }

        public Builder uidOnly(boolean uidOnly) {
            this.uidOnly = uidOnly;
            return this;
        }

        public Builder kheopsInstance(String kheopsInstance) {
            this.kheopsInstance = kheopsInstance;
            return this;
        }

        public Builder addSeries(Series series) {
            this.seriesLst.putIfAbsent(series, null);
            return this;
        }

        public Builder addSeries(Series series, Integer numberOfNewInstances) {
            this.seriesLst.put(series, numberOfNewInstances);
            return this;
        }

        public StudyResponse build () {
            final StudyResponse studyResponse = new StudyResponse();
            studyResponse.studyInstanceUID = study.getStudyInstanceUID();
            if (!uidOnly) {
                studyResponse.patientName = study.getPatientName();
                studyResponse.studyDate = study.getStudyDate();
                studyResponse.studyDescription = study.getStudyDescription();
                studyResponse.patientID = study.getPatientID();
                studyResponse.patientBirthDate = study.getPatientBirthDate();
                studyResponse.patientSex = study.getPatientSex();
                studyResponse.referringPhysicianName = study.getReferringPhysicianName();
                studyResponse.accessionNumber = study.getAccessionNumber();
                studyResponse.studyId = study.getStudyID();
                studyResponse.timezoneOffsetFromUtc = study.getTimezoneOffsetFromUTC();
                studyResponse.studyTime = study.getStudyTime();
            }
            if (kheopsInstance != null && showRetrieveUrl) {
                studyResponse.retrieveUrl = kheopsInstance + "/api/studies/" + studyResponse.studyInstanceUID;
            }

            if (!seriesLst.isEmpty()) {
                studyResponse.series = new ArrayList<>();
                for (Map.Entry<Series, Integer> seriesLstEntry : seriesLst.entrySet()) {
                    final Series series = seriesLstEntry.getKey();
                    final Integer numberOfNewInstances = seriesLstEntry.getValue();
                    final SeriesResponse seriesResponse = new SeriesResponse.Builder(series)
                            .kheopsInstance(kheopsInstance)
                            .uidOnly(uidOnly)
                            .numberOfNewInstances(numberOfNewInstances)
                            .showRetrieveUrl(showRetrieveUrl && !showRetrieveUrlStudyOnly)
                            .build();

                    studyResponse.series.add(seriesResponse);
                }
            }

            return studyResponse;
        }
    }
}