package online.kheops.auth_server.entity;

import online.kheops.auth_server.user.UsersPermission;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import static online.kheops.auth_server.util.JPANamedQueryConstants.*;

@SuppressWarnings({"WeakerAccess", "unused"})

@NamedQueries({
        @NamedQuery(name = "Albums.findById",
        query = "SELECT a FROM Album a WHERE :"+ALBUM_ID+" = a.id"),
        @NamedQuery(name = "Albums.findWithEnabledNewSeriesWebhooks",
        query = "SELECT distinct a FROM Album a JOIN a.albumSeries alS JOIN alS.series s JOIN s.study st JOIN a.webhooks w WHERE w.newSeries = true AND w.enabled = true AND st.studyInstanceUID = :"+STUDY_UID),

})

@Entity
@Table(name = "albums")
public class Album {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "name")
    private String name;

    @Basic(optional = false)
    @Column(name = "id")
    private String id;

    @Column(name = "description")
    private String description;

    @Basic(optional = false)
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @Basic(optional = false)
    @Column(name = "last_event_time", updatable = true)
    private LocalDateTime lastEventTime;

    @Embedded private UserPermission userPermission;

    @OneToMany(mappedBy = "album")
    private Set<AlbumSeries> albumSeries = new HashSet<>();

    @OneToMany(mappedBy = "album")
    private Set<AlbumUser> albumUser = new HashSet<>();

    @OneToMany(mappedBy = "album")
    private Set<Webhook> webhooks = new HashSet<>();

    @OneToMany(mappedBy = "album")
    @Where(clause = "enabled=true and new_series = true")
    private Set<Webhook> webhooksNewSeriesEnabled = new HashSet<>();

    @OneToMany(mappedBy = "album")
    @Where(clause = "enabled=true and new_user = true")
    private Set<Webhook> webhooksNewUserEnabled = new HashSet<>();

    @OneToMany(mappedBy = "album")
    private Set<Event> events = new HashSet<>();

    @OneToOne(mappedBy = "inbox")
    private User inboxUser;

    @OneToMany(mappedBy = "album")
    private Set<Capability> capabilities = new HashSet<>();

    @OneToMany(mappedBy = "album")
    private Set<ReportProvider> reportProviders = new HashSet<>();

    @PrePersist
    public void onPrePersist() {
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        createdTime = now;
        lastEventTime = now;
    }

    public Album() {}

    public Album(String name, String description, UsersPermission usersPermission, String id) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.userPermission = new UserPermission(usersPermission);
    }

    public long getPk() { return pk; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName( String name ) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription( String description ) { this.description = description; }

    public LocalDateTime getCreatedTime() { return createdTime; }

    public LocalDateTime getLastEventTime() { return lastEventTime; }

    public UserPermission getUserPermission() { return userPermission; }

    public void setUserPermission(UserPermission userPermission) { this.userPermission = userPermission; }

    public void updateLastEventTime() { this.lastEventTime = LocalDateTime.now(ZoneOffset.UTC); }

    public boolean containsSeries(Series series, EntityManager em) {
        try {
            em.createNamedQuery("AlbumSeries.findByAlbumAndSeries", AlbumSeries.class)
                    .setParameter(SERIES, series)
                    .setParameter(ALBUM, this)
                    .getSingleResult();
        } catch (NoResultException e) {
            return false;
        }
        return true;
    }

    public void addSeries(AlbumSeries albumSeries) { this.albumSeries.add(albumSeries); }

    public void removeSeries(Series series, EntityManager em) {
        AlbumSeries localAlbumSeries = em.createQuery("SELECT alS from AlbumSeries alS where :"+SERIES+" = alS.series and :"+ALBUM+" = alS.album", AlbumSeries.class)
                .setParameter(SERIES, series)
                .setParameter(ALBUM, this)
                .getSingleResult();
        //series.removeAlbumSeries(localAlbumSeries);
        this.albumSeries.remove(localAlbumSeries);
        em.remove(localAlbumSeries);
    }

    public Set<AlbumSeries> getAlbumSeries() { return albumSeries; }

    public Set<AlbumUser> getAlbumUser() { return albumUser; }

    public void addAlbumUser(AlbumUser albumUser) { this.albumUser.add(albumUser); }

    public User getInboxUser() { return inboxUser; }

    public void setInboxUser(User inboxUser) { this.inboxUser = inboxUser; }

    // Set<Event> getEvents() { return events; }

   // public void setEvents(Set<Event> events) { this.events = events; }

    //public void addEvents(Event event) { this.events.add(event); }

    public void addCapability(Capability capability) { this.capabilities.add(capability); }

    public Set<Capability> getCapabilities() { return capabilities; }

    public void addReportProvider(ReportProvider reportProvider) { this.reportProviders.add(reportProvider); }

    //public Set<ReportProvider> getReportProviders() {return reportProviders; }

    public void addWebhook(Webhook webhook) { this.webhooks.add(webhook); }

    public Set<Webhook> getWebhooks() { return webhooks; }

    public Set<Webhook> getWebhooksNewSeriesEnabled() { return webhooksNewSeriesEnabled; }

    public Set<Webhook> getWebhooksNewUserEnabled() { return webhooksNewUserEnabled; }

    @Override
    public String toString() {
        return "[Album_id:"+id+"]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return id.equals(album.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
