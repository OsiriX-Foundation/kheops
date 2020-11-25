package online.kheops.auth_server.entity;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")

@NamedQueries({
        @NamedQuery(name = "ReportProvider.findByClientId",
                query = "SELECT dsr FROM ReportProvider dsr WHERE :clientId = dsr.clientId AND dsr.removed = false"),
        @NamedQuery(name = "ReportProvider.findByClientIdAndAlbumId",
                query = "SELECT dsr FROM ReportProvider dsr JOIN dsr.album a WHERE :clientId = dsr.clientId AND :albumId = a.id"),
        @NamedQuery(name = "ReportProvider.findAllByAlbumId",
                query = "SELECT dsr FROM ReportProvider dsr JOIN dsr.album a WHERE :albumId = a.id AND dsr.removed = false ORDER BY dsr.creationTime desc"),
        @NamedQuery(name = "ReportProvider.countAllByAlbumId",
                query = "SELECT count(dsr) FROM ReportProvider dsr JOIN dsr.album a WHERE :albumId = a.id AND dsr.removed = false")
})

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
    @Column(name = "client_id", updatable = false)
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
    @JoinColumn (name = "album_fk", nullable=false, insertable = true, updatable = false)
    private Album album;

    @OneToMany(mappedBy = "reportProvider")
    private Set<Mutation> mutations = new HashSet<>();

    @PrePersist
    public void onPrePersist() {
        creationTime = LocalDateTime.now(ZoneOffset.UTC);
        this.removed = false;
    }

    public ReportProvider() {}

    public ReportProvider(String url, String name, Album album, String clientId) {
        this.clientId = clientId;
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

    public void setUrl(String url) { this.url = url; }

    public void setName(String name) { this.name = name; }

    public boolean isRemoved() { return removed; }

    public void setAsRemoved() { this.removed = true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportProvider reportProvider = (ReportProvider) o;
        return clientId.equals(reportProvider.clientId);
    }

    @Override
    public int hashCode() { return clientId.hashCode(); }
}
