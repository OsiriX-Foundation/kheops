package online.kheops.auth_server.entity;

import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "Comment.findAllByAlbum",
                query = "SELECT c FROM Comment c WHERE :album = c.album AND (c.privateTargetUser = null OR c.privateTargetUser = :user OR c.user = :user) ORDER BY c.eventTime desc"),
        @NamedQuery(name = "Comment.countAllByAlbumAndUser",
                query = "SELECT count(c) FROM Comment c WHERE :album = c.album AND (c.privateTargetUser = null OR c.user = :user OR c.privateTargetUser = :user)"),
        @NamedQuery(name = "Comment.findAllByStudyUIDAndUser",
                query = "SELECT c FROM Comment c WHERE c.study.studyInstanceUID =  :studyUID AND (c.privateTargetUser = null OR c.privateTargetUser = :user OR c.user = :user) ORDER BY c.eventTime desc"),
        @NamedQuery(name = "Comment.countAllByStudyUIDAndUser",
                query = "SELECT count(c) FROM Comment c WHERE c.study.studyInstanceUID = :studyUID AND (c.privateTargetUser = null OR c.privateTargetUser = :user OR c.user = :user)"),
        @NamedQuery(name = "Comment.findAllPublicByStudyUID",
                query = "SELECT c FROM Comment c WHERE c.study.studyInstanceUID =  :studyUID AND c.privateTargetUser = null ORDER BY c.eventTime desc"),
        @NamedQuery(name = "Comment.coundAllPublicByStudyUID",
                query = "SELECT count(c) FROM Comment c WHERE c.study.studyInstanceUID = :studyUID AND c.privateTargetUser = null")
})

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
