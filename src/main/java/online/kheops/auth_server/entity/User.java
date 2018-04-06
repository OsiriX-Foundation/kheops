package online.kheops.auth_server.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    private String username;

    @OneToMany
    @JoinColumn (name = "user_fk", nullable=false)
    Set<UserStudy> userStudies = new HashSet<UserStudy>();

    public User() {}

    public User(String username) {
        this.username = username;
    }

    public long getPk() {
        return pk;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<UserStudy> getUserStudies() {
        return userStudies;
    }
}
