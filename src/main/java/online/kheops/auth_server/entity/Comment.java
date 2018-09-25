package online.kheops.auth_server.entity;

import javax.persistence.*;

@Entity(name = "Comment")
@DiscriminatorValue("Comment")
public class Comment extends Event{

    @Basic(optional = true)
    @Column(name = "comment", updatable = false)
    private String comment;

    public String getComment() { return comment; }

    public Comment() {}

    public Comment(String comment, User user, Album album) {
        super(user, album);
        this.comment = comment;
    }

    public Comment(String comment, User user, Study study) {
        super(user, study);
        this.comment = comment;
    }
}
