package online.kheops.auth_server.entity;

import javax.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "study_fk")
    private Study study;

    public Series() {}

    public Series(String newSeriesInstanceUID) {
        seriesInstanceUID = newSeriesInstanceUID;
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

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
