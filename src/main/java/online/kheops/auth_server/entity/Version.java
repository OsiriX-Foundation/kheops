package online.kheops.auth_server.entity;

import javax.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "version")

public class Version {

    @Id
    @Basic(optional = false)
    @Column(name = "version")
    private String version;
}
