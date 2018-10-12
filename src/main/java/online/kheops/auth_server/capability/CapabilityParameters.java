package online.kheops.auth_server.capability;

import java.time.LocalDateTime;

public class CapabilityParameters {

    private final Long callingUserPk;
    private final String title;
    private final LocalDateTime expirationTime;
    private final LocalDateTime notBeforeTime;
    private final ScopeType scopeType;
    private Long albumPk;
    private String seriesInstanceUID;
    private String studyInstanceUID;
    private final boolean readPermission;
    private final boolean writePermission;

    protected CapabilityParameters(Long callingUserPk, String title, LocalDateTime expirationTime, LocalDateTime notBeforeTime,
                                   ScopeType scopeType, Long albumPk, String studyInstanceUID, String seriesInstanceUID,
                                   boolean readPermission, boolean writePermission) {
        this.callingUserPk = callingUserPk;
        this.title = title;
        this.expirationTime = expirationTime;
        this.notBeforeTime = notBeforeTime;
        this.albumPk = albumPk;
        this.seriesInstanceUID = seriesInstanceUID;
        this.studyInstanceUID = studyInstanceUID;
        this.readPermission = readPermission;
        this.writePermission = writePermission;
        this.scopeType = scopeType;
    }

    public Long getCallingUserPk() { return callingUserPk; }
    public String getTitle() { return title; }
    public LocalDateTime getExpirationTime() { return expirationTime; }
    public LocalDateTime getNotBeforeTime() { return notBeforeTime; }
    public ScopeType getScopeType() { return scopeType; }
    public Long getAlbumPk() { return albumPk; }
    public String getSeriesInstanceUID() { return seriesInstanceUID; }
    public String getStudyInstanceUID() { return studyInstanceUID; }
    public boolean isReadPermission() {  return readPermission; }
    public boolean isWritePermission() { return writePermission; }

}

