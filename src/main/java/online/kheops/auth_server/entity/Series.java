package online.kheops.auth_server.entity;

import online.kheops.auth_server.SeriesDTO;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.StringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "timezone_offset_from_utc")
    private String timezoneOffsetFromUTC;

    @Column(name = "series_description")
    private String seriesDescription;

    @Column(name = "series_number")
    private int seriesNumber;

    @Column(name = "number_of_series_related_instances")
    private int numberOfSeriesRelatedInstances;

    @Basic(optional = false)
    @Column(name = "populated")
    private boolean populated = false;

    @ManyToOne
    @JoinColumn(name = "study_fk", insertable=false, updatable=false)
    private Study study;

    @ManyToMany(mappedBy = "series")
    private Set<User> users = new HashSet<>();

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

    public void mergeSeriesDTO(SeriesDTO seriesDTO) {
        setModality(seriesDTO.getModality());
        setTimezoneOffsetFromUTC(seriesDTO.getTimezoneOffsetFromUTC());
        setSeriesDescription(seriesDTO.getSeriesDescription());
        setSeriesNumber(seriesDTO.getSeriesNumber());
        setNumberOfSeriesRelatedInstances(seriesDTO.getNumberOfSeriesRelatedInstances());
        setPopulated(true);
    }

    public SeriesDTO createSeriesDTO() {
        SeriesDTO seriesDTO = new SeriesDTO();

        seriesDTO.setModality(getModality());
        seriesDTO.setTimezoneOffsetFromUTC(getTimezoneOffsetFromUTC());
        seriesDTO.setSeriesDescription(getSeriesDescription());
        seriesDTO.setSeriesInstanceUID(getSeriesInstanceUID());
        seriesDTO.setSeriesNumber(getSeriesNumber());
        seriesDTO.setNumberOfSeriesRelatedInstances(getNumberOfSeriesRelatedInstances());


        return seriesDTO;
    }

    public Attributes getAttributes() throws IllegalStateException {
        if (!isPopulated()) {
            throw new IllegalStateException();
        }

        Attributes attributes = new Attributes();

        safeAttributeSetString(attributes, Tag.Modality, VR.CS, getModality());
        safeAttributeSetString(attributes, Tag.TimezoneOffsetFromUTC, VR.SH, getTimezoneOffsetFromUTC());
        safeAttributeSetString(attributes, Tag.SeriesDescription, VR.LO, getSeriesDescription());

        attributes.setInt(Tag.SeriesNumber, VR.IS, getSeriesNumber());
        attributes.setInt(Tag.NumberOfSeriesRelatedInstances, VR.IS, getNumberOfSeriesRelatedInstances());

        return attributes;
    }

    private void safeAttributeSetString(Attributes attributes, int tag, VR vr, String string) {
        if (string != null) {
            attributes.setString(tag, vr, string);
        }
    }

    // doesn't set populated, but the caller probably will want to set populated after calling this method
    public void mergeAttributes(Attributes attributes) {
        setModality(attributes.getString(Tag.Modality, getModality()));
        setSeriesDescription(attributes.getString(Tag.SeriesDescription, getSeriesDescription()));
        setTimezoneOffsetFromUTC(attributes.getString(Tag.TimezoneOffsetFromUTC, getTimezoneOffsetFromUTC()));
        setSeriesNumber(attributes.getInt(Tag.SeriesNumber, getSeriesNumber()));
        setNumberOfSeriesRelatedInstances(attributes.getInt(Tag.NumberOfSeriesRelatedInstances, getNumberOfSeriesRelatedInstances()));
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

    public void setModality(String modality) {
        this.modality = modality;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public boolean isPopulated() {
        return populated;
    }

    public void setPopulated(boolean populated) {
        this.populated = populated;
    }

    public String getTimezoneOffsetFromUTC() {
        return timezoneOffsetFromUTC;
    }

    public void setTimezoneOffsetFromUTC(String timezoneOffsetFromUTC) {
        this.timezoneOffsetFromUTC = timezoneOffsetFromUTC;
    }

    public String getSeriesDescription() {
        return seriesDescription;
    }

    public void setSeriesDescription(String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }

    public int getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(int seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public int getNumberOfSeriesRelatedInstances() {
        return numberOfSeriesRelatedInstances;
    }

    public void setNumberOfSeriesRelatedInstances(int numberOfSeriesRelatedInstances) {
        this.numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances;
    }

    public Set<User> getUsers() {
        return users;
    }
}
