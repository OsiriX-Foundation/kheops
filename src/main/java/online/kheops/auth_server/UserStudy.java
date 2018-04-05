package online.kheops.auth_server;

import javax.persistence.*;

@Entity
@Table(name = "user_studies")
public class UserStudy {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    public long getPk() {
        return pk;
    }
}
