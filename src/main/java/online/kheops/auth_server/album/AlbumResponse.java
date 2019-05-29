package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.Capability;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static online.kheops.auth_server.album.Albums.getUsers;

public class AlbumResponse {

    @XmlElement(name = "album_id")
    private String id;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "description")
    private String description;
    @XmlElement(name = "modalities")
    private String[] modalities;
    @XmlElement(name = "created_time")
    private LocalDateTime createdTime;
    @XmlElement(name = "last_event_time")
    private LocalDateTime lastEventTime;
    @XmlElement(name = "number_of_users")
    private Integer numberOfUsers;
    @XmlElement(name = "users")
    private List<UserAlbumResponse> users;
    @XmlElement(name = "number_of_comments")
    private Integer numberOfComments;
    @XmlElement(name = "number_of_studies")
    private Integer numberOfStudies;
    @XmlElement(name = "add_user")
    private Boolean addUser;
    @XmlElement(name = "download_series")
    private Boolean downloadSeries;
    @XmlElement(name = "send_series")
    private Boolean sendSeries;
    @XmlElement(name = "delete_series")
    private Boolean deleteSeries;
    @XmlElement(name = "add_series")
    private Boolean addSeries;
    @XmlElement(name = "write_comments")
    private Boolean writeComments;
    @XmlElement(name = "is_favorite")
    private Boolean isFavorite ;
    @XmlElement(name = "notification_new_series")
    private Boolean notificationNewSeries;
    @XmlElement(name = "notification_new_comment")
    private Boolean notificationNewComment;
    @XmlElement(name = "is_admin")
    private Boolean isAdmin;

    private AlbumResponse() { /*empty*/ }

    protected AlbumResponse(AlbumResponseBuilder albumResponseBuilder) {

        id=albumResponseBuilder.getId();
        name=albumResponseBuilder.getName();
        description=albumResponseBuilder.getDescription();
        modalities=albumResponseBuilder.getModalities();
        createdTime=albumResponseBuilder.getCreatedTime();
        lastEventTime=albumResponseBuilder.getLastEventTime();
        numberOfUsers=albumResponseBuilder.getNumberOfUsers();
        numberOfComments=albumResponseBuilder.getNumberOfComments();
        numberOfStudies=albumResponseBuilder.getNumberOfStudies();
        addUser=albumResponseBuilder.getAddUser();
        downloadSeries=albumResponseBuilder.getDownloadSeries();
        sendSeries=albumResponseBuilder.getSendSeries();
        deleteSeries=albumResponseBuilder.getDeleteSeries();
        addSeries=albumResponseBuilder.getAddSeries();
        writeComments=albumResponseBuilder.getWriteComments();
        isFavorite=albumResponseBuilder.getFavorite();
        notificationNewSeries=albumResponseBuilder.getNotificationNewSeries();
        notificationNewComment=albumResponseBuilder.getNotificationNewComment();
        isAdmin=albumResponseBuilder.getAdmin();

        if(!albumResponseBuilder.getUsers().isEmpty()) {
            users = albumResponseBuilder.getUsers();
            Collections.sort(users);
        }
    }

    public AlbumResponse(Capability capability) {
        if(capability.getAlbum() == null) {
            throw new IllegalStateException();
        } else {
            id = capability.getAlbum().getId();
            name = capability.getAlbum().getName();
        }
    }

    public String getId() { return id; }

    public void setUsers(List<UserAlbumResponse> users) { this.users = users; }
}
