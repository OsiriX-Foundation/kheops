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
        public long numberOfUsers;
        @XmlElement(name = "number_of_comments")
        public long numberOfComments;
        @XmlElement(name = "number_of_studies")
        public long numberOfStudies;
        @XmlElement(name = "add_user")
        public boolean addUser;
        @XmlElement(name = "download_series")
        public boolean downloadSeries;
        @XmlElement(name = "send_series")
        public boolean sendSeries;
        @XmlElement(name = "delete_series")
        public boolean deleteSeries;
        @XmlElement(name = "add_series")
        public boolean addSeries;
        @XmlElement(name = "write_comments")
        public boolean writeComments;
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
        //@XmlElement(name = "notification_new_series")
        //public Boolean notificationNewSeries;
        //@XmlElement(name = "notification_new_comment")
        //public Boolean notificationNewComment;
        //@XmlElement(name = "is_favorite")
        //public Boolean isFavorite ;


        public int compareTo(UserAlbumResponse userAlbumResponse) {
            return userName.compareTo(userAlbumResponse.userName);
        }
    }


  /*  public static AlbumResponse albumToAlbumResponce(Album album, boolean isFavorite, long numberOfComments) {
        final AlbumResponses.AlbumResponse albumResponse = new AlbumResponse();

        albumResponse.pk = album.getPk();
        albumResponse.name = album.getName();
        albumResponse.description = album.getDescription();
        albumResponse.createdTime = album.getCreatedTime();
        albumResponse.lastEventTime = album.getLastEventTime();
        albumResponse.numberOfUsers = album.getAlbumUser().size();
        albumResponse.numberOfStudies = album.getSeries().size();
        albumResponse.addSeries = album.isAddSeries();
        albumResponse.addUser = album.isAddUser();
        albumResponse.deleteSeries = album.isDeleteSeries();
        albumResponse.downloadSeries = album.isDownloadSeries();
        albumResponse.sendSeries = album.isSendSeries();
        albumResponse.writeComments = album.isWriteComments();

        albumResponse.numberOfComments = numberOfComments;

        albumResponse.isFavorite = isFavorite;

        return albumResponse;
    }*/

    public static UserAlbumResponse albumUserToUserAlbumResponce(AlbumUser albumUser) {
        final UserAlbumResponse userAlbumResponse = new UserAlbumResponse();

        userAlbumResponse.userName = albumUser.getUser().getGoogleEmail();
        userAlbumResponse.isAdmin = albumUser.isAdmin();
        //userAlbumResponse.isFavorite = albumUser.isFavorite();
        //userAlbumResponse.notificationNewComment = albumUser.isNewCommentNotifications();
        //userAlbumResponse.notificationNewSeries = albumUser.isNewSeriesNotifications();

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
        albumResponse.addSeries = ((Byte) r.getValue("add_series_permission")) != 0;
        albumResponse.addUser = ((Byte) r.getValue("add_user_permission")) != 0;
        albumResponse.deleteSeries = ((Byte) r.getValue("delete_series_permision")) != 0;
        albumResponse.downloadSeries = ((Byte) r.getValue("download_user_permission")) != 0;
        albumResponse.sendSeries = ((Byte) r.getValue("send_series_permission")) != 0;
        albumResponse.writeComments = ((Byte) r.getValue("write_comment_permission")) != 0;
        albumResponse.numberOfComments = (Integer) r.getValue("number_of_comments");
        albumResponse.isFavorite = ((Byte) r.getValue("favorite")) != 0;
        albumResponse.notificationNewComment = ((Byte) r.getValue("new_comment_notifications")) != 0;
        albumResponse.notificationNewSeries = ((Byte) r.getValue("new_series_notifications")) != 0;
        albumResponse.isAdmin = ((Byte) r.getValue("admin")) != 0;
        if(r.getValue("modalities") != null) {
            albumResponse.modalities = r.getValue("modalities").toString().split("/");
        }

        return albumResponse;
    }
}
