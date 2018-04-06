package online.kheops.auth_server.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_studies")
public class UserStudy {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @ManyToMany
    @JoinTable (name = "user_studies_series",
            joinColumns = @JoinColumn(name = "user_study_fk"),
            inverseJoinColumns = @JoinColumn(name = "series_fk"))
    private Set<Series> series = new HashSet<Series>();

    @ManyToOne
    @JoinColumn(name = "user_fk", insertable=false, updatable=false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "study_fk")
    private Study study;

    public long getPk() {
        return pk;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public Set<Series> getSeries() {
        return series;
    }
}
