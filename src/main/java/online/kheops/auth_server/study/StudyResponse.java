package online.kheops.auth_server.study;

import online.kheops.auth_server.entity.Study;

import javax.xml.bind.annotation.XmlElement;

public class StudyResponse {

    @XmlElement(name = "patient_name")
    private String patientName;
    @XmlElement(name = "study_uid")
    private String studyInstanceUID;
    @XmlElement(name = "study_date")
    private String studyDate;
    @XmlElement(name = "study_description")
    private String studyDescription;
    @XmlElement(name = "patient_ID")
    private String patientID;


    private StudyResponse() { /*empty*/ }

    public StudyResponse(Study study) {
        this.patientName = study.getPatientName();
        this.studyInstanceUID = study.getStudyInstanceUID();
        this.studyDate = study.getStudyDate();
        this.studyDescription = study.getStudyDescription();
        this.patientID = study.getPatientID();
    }
}
