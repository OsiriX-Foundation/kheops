package online.kheops.auth_server.entity;

import online.kheops.auth_server.report_provider.ClientId;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
@Table(name = "report_providers")

public class ReportProvider {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Basic(optional = false)
    @Column(name = "client_id")
    private String clientId;

    @Basic(optional = false)
    @Column(name = "url")
    private String url;

    @Basic(optional = false)
    @Column(name = "name")
    private String name;

    @Basic(optional = false)
    @Column(name = "removed")
    private boolean removed;

    @ManyToOne
    @JoinColumn (name = "album_fk", nullable=false, insertable = false, updatable = false)
    private Album album;

    @OneToMany
    @JoinColumn (name = "report_provider_fk", nullable=true)
    private Set<Mutation> mutations = new HashSet<>();

    @PrePersist
    public void onPrePersist() {
        creationTime = LocalDateTime.now(ZoneOffset.UTC);
        this.removed = false;
    }


    public ReportProvider() {}

    public ReportProvider(String url, String name, Album album) {
        this.clientId = new ClientId().getClientId();
        this.url = url;
        this.name = name;
        this.album = album;

        album.addReportProvider(this);
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Album getAlbum() { return album; }

    public void addMutation(Mutation mutation) { mutations.add(mutation); }

    public void setClientId(String clientId) { this.clientId = clientId; }

    public void setUrl(String url) { this.url = url; }

    public void setName(String name) { this.name = name; }

    public boolean isRemoved() { return removed; }

    public void setAsRemoved() { this.removed = true; }
}
