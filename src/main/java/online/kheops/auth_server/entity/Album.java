package online.kheops.auth_server.entity;

import online.kheops.auth_server.user.UsersPermission;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity
@Table(name = "album")
public class Album {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Basic(optional = false)
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @Basic(optional = false)
    @Column(name = "last_event_time", updatable = true)
    private LocalDateTime lastEventTime;

    @Basic(optional = false)
    @Column(name = "add_user_permission")
    private boolean addUser;

    @Basic(optional = false)
    @Column(name = "download_series_permission")
    private boolean downloadSeries;

    @Basic(optional = false)
    @Column(name = "send_series_permission")
    private boolean sendSeries;

    @Basic(optional = false)
    @Column(name = "delete_series_permission")
    private boolean deleteSeries;

    @Basic(optional = false)
    @Column(name = "add_series_permission")
    private boolean addSeries;

    @Basic(optional = false)
    @Column(name = "write_comments_permission")
    private boolean writeComments;

    @ManyToMany
    @JoinTable (name = "album_series",
            joinColumns = @JoinColumn(name = "album_fk"),
            inverseJoinColumns = @JoinColumn(name = "series_fk"))
    private Set<Series> series = new HashSet<>();

    @OneToMany
    @JoinColumn (name = "album_fk", nullable=false)
    private Set<AlbumUser> albumUser = new HashSet<>();

    @OneToMany
    @JoinColumn (name = "album_fk", nullable=true)
    private Set<Event> events = new HashSet<>();

    @OneToOne(mappedBy = "inbox")
    private User inboxUser;

    @OneToMany
    @JoinColumn (name = "album_fk")
    private Set<Capability> capabilities = new HashSet<>();

    @PrePersist
    public void onPrePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        createdTime = now;
        lastEventTime = now;
    }

    public Album() {}

    public Album(String name, String description, UsersPermission usersPermission) {
        this.name = name;
        this.description = description;
        this.setPermission(usersPermission);
    }

    public long getPk() { return pk; }

    public String getName() { return name; }

    public void setName( String name ) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription( String description ) { this.description = description; }

    public LocalDateTime getCreatedTime() { return createdTime; }

    public LocalDateTime getLastEventTime() { return lastEventTime; }

    public void setLastEventTime(LocalDateTime lasteEventTime) { this.lastEventTime = lasteEventTime; }

    public boolean isAddUser() { return addUser; }

    public void setAddUser( boolean addUser ) { this.addUser = addUser; }

    public boolean isDownloadSeries() { return downloadSeries; }

    public void setDownloadSeries( boolean downloadSeries ) { this.downloadSeries = downloadSeries; }

    public boolean isSendSeries() { return sendSeries; }

    public void setSendSeries( boolean sendSeries ) { this.sendSeries = sendSeries; }

    public boolean isDeleteSeries() { return deleteSeries; }

    public void setDeleteSeries( boolean deleteSeries ) { this.deleteSeries = deleteSeries; }

    public boolean isAddSeries() { return addSeries; }

    public void setAddSeries( boolean addSeries ) { this.addSeries = addSeries; }

    public boolean isWriteComments() { return writeComments; }

    public void setWriteComments( boolean writeComments ) { this.writeComments = writeComments; }

    public Set<Series> getSeries() { return series; }

    public void addSeries(Series series) {
        this.series.add(series);
        series.addAlbum(this);
    }

    public void removeSeries(Series series) {
        this.series.remove(series);
        series.removeAlbum(this);
    }

    public Set<AlbumUser> getAlbumUser() { return albumUser; }

    public void addAlbumUser(AlbumUser albumUser) { this.albumUser.add(albumUser); }

    public User getInboxUser() { return inboxUser; }

    public void setInboxUser(User inboxUser) { this.inboxUser = inboxUser; }

    public void setPermission(UsersPermission usersPermission) {
        usersPermission.getAddUser().ifPresent(this::setAddUser);
        usersPermission.getDownloadSeries().ifPresent(this::setDownloadSeries);
        usersPermission.getSendSeries().ifPresent(this::setSendSeries);
        usersPermission.getDeleteSeries().ifPresent(this::setDeleteSeries);
        usersPermission.getAddSeries().ifPresent(this::setAddSeries);
        usersPermission.getWriteComments().ifPresent(this::setWriteComments);
    }

    public Set<Event> getEvents() { return events; }

    public void setEvents(Set<Event> events) { this.events = events; }

    public void addEvents(Event event) { this.events.add(event); }

    public void addCapability(Capability capability) { this.capabilities.add(capability); }
}
