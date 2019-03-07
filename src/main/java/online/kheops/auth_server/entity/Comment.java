package online.kheops.auth_server.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "Comment")
@DiscriminatorValue("Comment")
public class Comment extends Event{

    @Basic(optional = true)
    @Column(name = "comment", updatable = false)
    private String commentContent;

    public String getComment() { return commentContent; }

    public Comment() {}

    public Comment(String commentContent, User user, Album album) {
        super(user, album);
        this.commentContent = commentContent;
    }

    public Comment(String commentContent, User user, Study study) {
        super(user, study);
        this.commentContent = commentContent;
    }
}
