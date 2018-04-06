package online.kheops.auth_server.entity;

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

    @OneToMany
    @JoinColumn (name = "study_fk", nullable=false)
    private Set<Series> series = new HashSet<Series>();

    public long getPk() {
        return pk;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public Set<Series> getSeries() {
        return series;
    }
}
