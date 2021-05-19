package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.UserPermission;
import online.kheops.auth_server.user.UserResponse;

import java.time.LocalDateTime;
import java.util.*;


public class AlbumResponseBuilder {

    private String id;
    private String name;
    private String description;
    private SortedSet<String> modalities;
    private LocalDateTime createdTime;
    private LocalDateTime lastEventTime;
    private Integer numberOfUsers;
    private List<UserResponse> users;
    private Integer numberOfComments;
    private Integer numberOfStudies;
    private Integer numberOfSeries;
    private Integer numberOfInstances;
    private Boolean addUser;
    private Boolean downloadSeries;
    private Boolean sendSeries;
    private Boolean deleteSeries;
    private Boolean addSeries;
    private Boolean writeComments;
    private Boolean isFavorite;
    private Boolean notificationNewSeries;
    private Boolean notificationNewComment;
    private Boolean isAdmin;

    public AlbumResponseBuilder() {
        users = new ArrayList<>();
    }

    public AlbumResponseBuilder(String albumId, String albumName, String albumDescription, LocalDateTime albumCreatedTime, LocalDateTime albumLastEventTime, UserPermission userPermission, boolean albumUserAdmin, boolean albumUserFavorite, boolean albumUserNewCommentNotifications, boolean albumUserNewSeriesNotifications, long nbStudy, long nbSeries, long nbInstance, long nbUsers, long nbComment, String modalitiesLst) {
        users = new ArrayList<>();
        this.id = albumId;
        this.name = albumName;
        this.description = albumDescription;
        this.createdTime = albumCreatedTime;
        this.lastEventTime = albumLastEventTime;
        this.numberOfUsers = ((Long)nbUsers).intValue();
        this.numberOfStudies = ((Long) nbStudy).intValue();
        this.numberOfSeries = ((Long)nbSeries).intValue();
        this.numberOfInstances = ((Long) nbInstance).intValue();
        this.addSeries = userPermission.isAddSeries();
        this.addUser = userPermission.isAddUser();
        this.deleteSeries = userPermission.isDeleteSeries();
        this.downloadSeries = userPermission.isDownloadSeries();
        this.sendSeries = userPermission.isSendSeries();
        this.writeComments = userPermission.isWriteComments();
        this.numberOfComments = ((Long)nbComment).intValue();
        this.isFavorite = albumUserFavorite;
        this.notificationNewComment = albumUserNewCommentNotifications;
        this.notificationNewSeries = albumUserNewSeriesNotifications;
        this.isAdmin = albumUserAdmin;

        this.modalities = new TreeSet<>();
        this.modalities.addAll(Arrays.asList(modalitiesLst.substring(1, modalitiesLst.length() - 1).split(",")));
        this.modalities.remove("NULL");
    }

    public AlbumResponseBuilder(Album album, long nbStudy, long nbSeries, long nbInstance, String modalitiesLst) {
        users = new ArrayList<>();
        this.id = album.getId();
        this.name = album.getName();
        this.description = album.getDescription();
        this.numberOfStudies = ((Long) nbStudy).intValue();
        this.numberOfSeries = ((Long)nbSeries).intValue();
        this.numberOfInstances = ((Long) nbInstance).intValue();

        this.modalities = new TreeSet<>();
        this.modalities.addAll(Arrays.asList(modalitiesLst.substring(1, modalitiesLst.length() - 1).split(",")));
        this.modalities.remove("NULL");
    }

    public AlbumResponseBuilder addUser(AlbumUser albumUser) {
        UserResponse userResponse = new UserResponse(albumUser);
        users.add(userResponse);
        return this;
    }

    public AlbumResponse build() { return new AlbumResponse(this); }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SortedSet<String> getModalities() {
        return modalities;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getLastEventTime() {
        return lastEventTime;
    }

    public Integer getNumberOfUsers() {
        return numberOfUsers;
    }

    public List<UserResponse> getUsers() {
        return users;
    }

    public Integer getNumberOfComments() {
        return numberOfComments;
    }

    public Integer getNumberOfStudies() { return numberOfStudies; }

    public Integer getNumberOfSeries() { return numberOfSeries; }

    public Integer getNumberOfInstances() { return numberOfInstances; }

    public Boolean getAddUser() {
        return addUser;
    }

    public Boolean getDownloadSeries() {
        return downloadSeries;
    }

    public Boolean getSendSeries() {
        return sendSeries;
    }

    public Boolean getDeleteSeries() {
        return deleteSeries;
    }

    public Boolean getAddSeries() {
        return addSeries;
    }

    public Boolean getWriteComments() {
        return writeComments;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public Boolean getNotificationNewSeries() {
        return notificationNewSeries;
    }

    public Boolean getNotificationNewComment() {
        return notificationNewComment;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }
}
