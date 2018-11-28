package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.AlbumUser;
import org.jooq.Record;

import javax.xml.bind.annotation.XmlElement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class AlbumResponses {

    private AlbumResponses() {
        throw new IllegalStateException("Utility class");
    }

    public static class AlbumResponse {
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
        @XmlElement(name = "number_of_comments")
        public long numberOfComments;
        @XmlElement(name = "number_of_studies")
        public long numberOfStudies;
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
        @XmlElement(name = "is_admin")
        public Boolean isAdmin;


        public int compareTo(UserAlbumResponse userAlbumResponse) {
            return userName.compareTo(userAlbumResponse.userName);
        }
    }

    public static UserAlbumResponse albumUserToUserAlbumResponce(AlbumUser albumUser) {
        final UserAlbumResponse userAlbumResponse = new UserAlbumResponse();

        userAlbumResponse.userName = albumUser.getUser().getGoogleEmail();
        userAlbumResponse.isAdmin = albumUser.isAdmin();

        return userAlbumResponse;
    }

    public static AlbumResponse recordToAlbumResponse(Record r) {
        final AlbumResponse albumResponse = new AlbumResponse();

        albumResponse.id = r.getValue("album_pk").toString();
        albumResponse.name = r.getValue("album_name").toString();
        albumResponse.description = r.getValue("album_description").toString();
        albumResponse.createdTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(((Timestamp) r.getValue("album_created_time")).getTime()), TimeZone.getDefault().toZoneId());
        albumResponse.lastEventTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(((Timestamp) r.getValue("album_last_event_time")).getTime()), TimeZone.getDefault().toZoneId());
        albumResponse.numberOfUsers = (Integer) r.getValue("number_of_users");
        albumResponse.numberOfStudies = (Integer) r.getValue("number_of_studies");

        albumResponse.addSeries = (boolean) (r.getValue("add_series_permission"));
        albumResponse.addUser = ((boolean) r.getValue("add_user_permission"));
        albumResponse.deleteSeries = ((boolean) r.getValue("delete_series_permision"));
        albumResponse.downloadSeries = ((boolean) r.getValue("download_user_permission"));
        albumResponse.sendSeries = ((boolean) r.getValue("send_series_permission"));
        albumResponse.writeComments = ((boolean) r.getValue("write_comment_permission"));
        albumResponse.numberOfComments = (Integer) r.getValue("number_of_comments");
        albumResponse.isFavorite = ((boolean) r.getValue("favorite"));
        albumResponse.notificationNewComment = ((boolean) r.getValue("new_comment_notifications"));
        albumResponse.notificationNewSeries = ((boolean) r.getValue("new_series_notifications"));
        albumResponse.isAdmin = ((boolean) r.getValue("admin"));
        if(r.getValue("modalities") != null) {
            albumResponse.modalities = r.getValue("modalities").toString().split("/");
        } else {
            albumResponse.modalities = new String[0];
        }

        return albumResponse;
    }

    public static AlbumResponse recordToAlbumResponseForCapabilityToken(Record r) {
        final AlbumResponse albumResponse = new AlbumResponse();

        albumResponse.id = r.getValue("album_pk").toString();
        albumResponse.name = r.getValue("album_name").toString();
        albumResponse.description = r.getValue("album_description").toString();
        albumResponse.numberOfStudies = (Integer) r.getValue("number_of_studies");
        albumResponse.numberOfComments = (Integer) r.getValue("number_of_comments");
        if(r.getValue("modalities") != null) {
            albumResponse.modalities = r.getValue("modalities").toString().split("/");
        } else {
            albumResponse.modalities = new String[0];
        }

        return albumResponse;
    }
}
