package online.kheops.auth_server.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Event.findAllByAlbum",
        query = "SELECT e FROM Event e WHERE :album = e.album AND (e.privateTargetUser = null OR e.privateTargetUser = :user OR e.user = :user) ORDER BY e.eventTime desc"),
        @NamedQuery(name = "Event.countAllByAlbumAndUser",
        query = "SELECT count(e) FROM Event e WHERE :album = e.album AND (e.privateTargetUser = null OR e.privateTargetUser = :user OR e.user = :user)")
})

@Entity
@Table(name = "events")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type")
public abstract class Event {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "event_time", updatable = false)
    private LocalDateTime eventTime;

    @ManyToOne
    @JoinColumn (name = "user_fk", nullable=false, insertable = true, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn (name = "album_fk", nullable=true, insertable = true, updatable = false)
    private Album album;

    @ManyToOne
    @JoinColumn (name = "study_fk", nullable=true, insertable = true, updatable = false)
    private Study study;

    @ManyToOne
    @JoinColumn(name = "private_target_user_fk", nullable=true, insertable = true, updatable = false)
    private User privateTargetUser;

    //@OneToMany(mappedBy = "event", fetch = FetchType.EAGER)
    //private Set<EventSeries> eventSeries = new HashSet<>();
    @ManyToMany()
    @JoinTable(name = "event_series",
            joinColumns = @JoinColumn(name = "event_fk"),
            inverseJoinColumns = @JoinColumn(name = "series_fk"))
    private Set<Series> seriesLst = new HashSet<>();

    @PrePersist
    public void onPrePersist() {
        eventTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    protected Event() {}

    protected Event(User callingUser) {
        user = callingUser;
        callingUser.addEvents(this);
    }

    protected Event(User callingUser, Album album) {
        user = callingUser;
        this.album = album;
        callingUser.addEvents(this);
        album.addEvents(this);
    }

    protected Event(User callingUser, Album album, Study study) {
        user = callingUser;
        this.album = album;
        this.study = study;
        callingUser.addEvents(this);
        album.addEvents(this);
        study.addEvents(this);
    }

    protected Event(User callingUser, Study study) {
        user = callingUser;
        this.study = study;
        callingUser.addEvents(this);
        study.addEvents(this);
    }

    public long getPk() { return pk; }

    public LocalDateTime getEventTime() { return eventTime; }

    public User getUser() { return user; }

    public Album getAlbum() { return album; }

    public Study getStudy() { return study; }

    public User getPrivateTargetUser() {  return privateTargetUser; }

    public void setPrivateTargetUser(User privateTargetUser) { this.privateTargetUser = privateTargetUser; }

    public void addSeries(Series series) {
        this.seriesLst.add(series);
    }

    public void removeSeries(Series series) {
        this.seriesLst.remove(series);
    }

    public void removeAllSeries() {
        this.seriesLst.clear();
    }

    public Set<Series> getSeriesLst() { return seriesLst; }

    //public void addEventSeries(EventSeries eventSeries) { this.eventSeries.add(eventSeries); }
    //public Set<EventSeries> getEventSeries() { return eventSeries; }
}
