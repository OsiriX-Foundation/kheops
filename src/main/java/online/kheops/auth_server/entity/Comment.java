package online.kheops.auth_server.entity;

import javax.persistence.*;

import static online.kheops.auth_server.util.JPANamedQueryConstants.*;

@NamedQueries({
        @NamedQuery(name = "Comment.findAllByAlbum",
                query = "SELECT c FROM Comment c WHERE :"+ALBUM+" = c.album AND (c.privateTargetUser = null OR c.privateTargetUser = :"+USER+" OR c.user = :"+USER+") ORDER BY c.eventTime desc"),
        @NamedQuery(name = "Comment.countAllByAlbumAndUser",
                query = "SELECT count(c) FROM Comment c WHERE :"+ALBUM+" = c.album AND (c.privateTargetUser = null OR c.user = :"+USER+" OR c.privateTargetUser = :"+USER+")"),
        @NamedQuery(name = "Comment.findAllByStudyUIDAndUser",
                query = "SELECT c FROM Comment c WHERE c.study.studyInstanceUID = :"+STUDY_UID+" AND (c.privateTargetUser = null OR c.privateTargetUser = :"+USER+" OR c.user = :"+USER+") ORDER BY c.eventTime desc"),
        @NamedQuery(name = "Comment.countAllByStudyUIDAndUser",
                query = "SELECT count(c) FROM Comment c WHERE c.study.studyInstanceUID = :"+STUDY_UID+" AND (c.privateTargetUser = null OR c.privateTargetUser = :"+USER+" OR c.user = :"+USER+")"),
        @NamedQuery(name = "Comment.findAllPublicByStudyUID",
                query = "SELECT c FROM Comment c WHERE c.study.studyInstanceUID =  :"+STUDY_UID+" AND c.privateTargetUser = null ORDER BY c.eventTime desc"),
        @NamedQuery(name = "Comment.coundAllPublicByStudyUID",
                query = "SELECT count(c) FROM Comment c WHERE c.study.studyInstanceUID = :"+STUDY_UID+" AND c.privateTargetUser = null")
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
