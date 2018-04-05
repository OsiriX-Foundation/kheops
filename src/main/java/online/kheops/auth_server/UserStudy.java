package online.kheops.auth_server;

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
    Set<Series> series = new HashSet<Series>();

    public long getPk() {
        return pk;
    }
}
