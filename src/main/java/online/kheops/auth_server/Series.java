package online.kheops.auth_server;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "series")
public class Series {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time", updatable = false)
    private Date createdTime;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_time")
    private Date updatedTime;

    @Basic(optional = false)
    @Column(name = "series_uid", updatable = false)
    private String seriesInstanceUID;

    @Column(name = "modality")
    private String modality;

    @Basic(optional = false)
    @Column(name = "series_size")
    private long size = -1L;

    @Basic(optional = false) // this might need to be populated
    @Column(name = "study_uid", updatable = false)
    private String studyInstanceUID;

// needed parameters
    // if I'm not going to have a Study object (or I'm going to need a study object per user) I'm going to store the study related info in the series.

    // StudyInstanceUID
    // StudyID
    // Study Date
    //

    public Series(String newStudyInstanceUID, String newSeriesInstanceUID) {
        studyInstanceUID = newStudyInstanceUID;
        seriesInstanceUID = newSeriesInstanceUID;

//        Date now = new Date();
//        createdTime = now;
//        updatedTime = now;

    }

    @Override
    public String toString() {
        return "Series[pk=" + pk
                + ", uid=" + seriesInstanceUID
                + ", StudyInstanceUID=" + seriesInstanceUID
                + ", mod=" + modality
                + "]";
    }

    @PrePersist
    public void onPrePersist() {
        Date now = new Date();
        createdTime = now;
        updatedTime = now;

        System.out.println("In prepersist");
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedTime = new Date();
    }

    public long getPk() {
        return pk;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public String getModality() {
        return modality;
    }

    public void resetSize() {
        this.size = -1L;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
