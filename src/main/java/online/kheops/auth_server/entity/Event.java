package online.kheops.auth_server.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
    @JoinColumn (name = "user_fk", nullable=false, insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn (name = "album_fk", nullable=true, insertable = false, updatable = false)
    private Album album;

    @ManyToOne
    @JoinColumn (name = "study_fk", nullable=true, insertable = false, updatable = false)
    private Study study;

    @ManyToOne
    @JoinColumn(name = "private_target_user_fk", nullable=true, insertable = false, updatable = false)
    private User privateTargetUser;

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
}
