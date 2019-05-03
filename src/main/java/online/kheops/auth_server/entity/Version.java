package online.kheops.auth_server.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "version")

public class Version {

    @Basic(optional = false)
    @Column(name = "version")
    private String version;
}
