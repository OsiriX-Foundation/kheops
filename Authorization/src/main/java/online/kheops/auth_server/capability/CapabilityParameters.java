package online.kheops.auth_server.capability;

import online.kheops.auth_server.entity.User;

import java.time.LocalDateTime;

public class CapabilityParameters {

    private final User callingUser;
    private final String title;
    private final LocalDateTime expirationTime;
    private final LocalDateTime notBeforeTime;
    private final ScopeType scopeType;
    private String albumId;
    private String seriesInstanceUID;
    private String studyInstanceUID;
    private final boolean readPermission;
    private final boolean writePermission;
    private final boolean appropriatePermission;
    private final boolean downloadPermission;

    protected CapabilityParameters(User callingUser, String title, LocalDateTime expirationTime, LocalDateTime notBeforeTime,
                                   ScopeType scopeType, String albumId, String studyInstanceUID, String seriesInstanceUID,
                                   boolean readPermission, boolean writePermission, boolean appropriatePermission, boolean downloadPermission) {
        this.callingUser = callingUser;
        this.title = title;
        this.expirationTime = expirationTime;
        this.notBeforeTime = notBeforeTime;
        this.albumId = albumId;
        this.seriesInstanceUID = seriesInstanceUID;
        this.studyInstanceUID = studyInstanceUID;
        this.readPermission = readPermission;
        this.writePermission = writePermission;
        this.scopeType = scopeType;
        this.appropriatePermission = appropriatePermission;
        this.downloadPermission = downloadPermission;
    }

    public User getCallingUser() { return callingUser; }
    public String getTitle() { return title; }
    public LocalDateTime getExpirationTime() { return expirationTime; }
    public LocalDateTime getNotBeforeTime() { return notBeforeTime; }
    public ScopeType getScopeType() { return scopeType; }
    public String getAlbumId() { return albumId; }
    public String getSeriesInstanceUID() { return seriesInstanceUID; }
    public String getStudyInstanceUID() { return studyInstanceUID; }
    public boolean isReadPermission() {  return readPermission; }
    public boolean isWritePermission() { return writePermission; }
    public boolean isAppropriatePermission() { return appropriatePermission; }
    public boolean isDownloadPermission() { return downloadPermission; }
}

