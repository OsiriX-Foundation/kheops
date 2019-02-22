package online.kheops.auth_server.album;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static online.kheops.auth_server.album.Albums.getUsers;

public class AlbumResponse {

    private Response response;

    public static class Response {
        @XmlElement(name = "album_id")
        public String id;
        @XmlElement(name = "name")
        public String name;
        @XmlElement(name = "description")
        public String description;
        @XmlElement(name = "modalities")
        private String[] modalities;
        @XmlElement(name = "created_time")
        public LocalDateTime createdTime;
        @XmlElement(name = "last_event_time")
        public LocalDateTime lastEventTime;
        @XmlElement(name = "number_of_users")
        public Integer numberOfUsers;
        @XmlElement(name = "users")
        public List<UserAlbumResponse> users;
        @XmlElement(name = "number_of_comments")
        public Integer numberOfComments;
        @XmlElement(name = "number_of_studies")
        public Integer numberOfStudies;
        @XmlElement(name = "add_user")
        public Boolean addUser;
        @XmlElement(name = "download_series")
        public Boolean downloadSeries;
        @XmlElement(name = "send_series")
        public Boolean sendSeries;
        @XmlElement(name = "delete_series")
        public Boolean deleteSeries;
        @XmlElement(name = "add_series")
        public Boolean addSeries;
        @XmlElement(name = "write_comments")
        public Boolean writeComments;
        @XmlElement(name = "is_favorite")
        public Boolean isFavorite ;
        @XmlElement(name = "notification_new_series")
        public Boolean notificationNewSeries;
        @XmlElement(name = "notification_new_comment")
        public Boolean notificationNewComment;
        @XmlElement(name = "is_admin")
        public Boolean isAdmin;
    }

    public static class UserAlbumResponse  implements Comparable<UserAlbumResponse> {
        @XmlElement(name = "user_name")
        public String userName;
        @XmlElement(name = "user_id")
        public String userId;
        @XmlElement(name = "is_admin")
        public Boolean isAdmin;

        @Override
        public int compareTo(UserAlbumResponse userAlbumResponse) {
            return userName.compareTo(userAlbumResponse.userName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof UserAlbumResponse) {
                final UserAlbumResponse userAlbumResponse = (UserAlbumResponse) obj;
                return  userAlbumResponse.userId.compareTo(userId) == 0 &&
                        userAlbumResponse.isAdmin == isAdmin &&
                        userAlbumResponse.userName.compareTo(userName) == 0;
            }
            return false;
        }
    }

    protected AlbumResponse(AlbumResponseBuilder albumResponseBuilder) {
        response = new Response();

        response.id=albumResponseBuilder.getId();
        response.name=albumResponseBuilder.getName();
        response.description=albumResponseBuilder.getDescription();
        response.modalities=albumResponseBuilder.getModalities();
        response.createdTime=albumResponseBuilder.getCreatedTime();
        response.lastEventTime=albumResponseBuilder.getLastEventTime();
        response.numberOfUsers=albumResponseBuilder.getNumberOfUsers();
        response.numberOfComments=albumResponseBuilder.getNumberOfComments();
        response.numberOfStudies=albumResponseBuilder.getNumberOfStudies();
        response.addUser=albumResponseBuilder.getAddUser();
        response.downloadSeries=albumResponseBuilder.getDownloadSeries();
        response.sendSeries=albumResponseBuilder.getSendSeries();
        response.deleteSeries=albumResponseBuilder.getDeleteSeries();
        response.addSeries=albumResponseBuilder.getAddSeries();
        response.writeComments=albumResponseBuilder.getWriteComments();
        response.isFavorite=albumResponseBuilder.getFavorite();
        response.notificationNewSeries=albumResponseBuilder.getNotificationNewSeries();
        response.notificationNewComment=albumResponseBuilder.getNotificationNewComment();
        response.isAdmin=albumResponseBuilder.getAdmin();

        if(!albumResponseBuilder.getUsers().isEmpty()) {
            response.users = albumResponseBuilder.getUsers();
        }
    }

    public void addUsersList(){
        try {
            response.users = getUsers(response.id).response.users;
        } catch (AlbumNotFoundException e) { /*empty*/ }
    }

    public Response getResponse() {
        if(response.users != null) {
            Collections.sort(response.users);
        }
        return response;
    }

    public List<UserAlbumResponse> getUsersResponse() {
        if(response.users != null) {
            Collections.sort(response.users);
        }
        return response.users;
    }
}
