package online.kheops.auth_server.capability;

import java.time.LocalDateTime;

public class CapabilityParameters extends CapabilityParametersBuilder{

    private final Long callingUserPk;
    private final String title;
    private final LocalDateTime expirationDate;
    private final LocalDateTime startDate;
    private final ScopeType scopeType;
    private Long albumPk;
    private String seriesInstanceUID;
    private String studyInstanceUID;
    private final boolean readPermission;
    private final boolean writePermission;

    protected CapabilityParameters(Long callingUserPk, String title, LocalDateTime expirationDate, LocalDateTime startDate,
                                   ScopeType scopeType, Long albumPk, String studyInstanceUID, String seriesInstanceUID,
                                   boolean readPermission, boolean writePermission) {
        this.callingUserPk = callingUserPk;
        this.title = title;
        this.expirationDate = expirationDate;
        this.startDate = startDate;
        this.albumPk = albumPk;
        this.seriesInstanceUID = seriesInstanceUID;
        this.studyInstanceUID = studyInstanceUID;
        this.readPermission = readPermission;
        this.writePermission = writePermission;
        this.scopeType = scopeType;
    }

    public Long getCallingUserPk() { return callingUserPk; }
    public String getTitle() { return title; }
    public LocalDateTime getExpirationDate() { return expirationDate; }
    public LocalDateTime getStartDate() { return startDate; }
    public ScopeType getScopeType() { return scopeType; }
    public Long getAlbumPk() { return albumPk; }
    public String getSeriesInstanceUID() { return seriesInstanceUID; }
    public String getStudyInstanceUID() { return studyInstanceUID; }
    public boolean isReadPermission() {  return readPermission; }
    public boolean isWritePermission() { return writePermission; }

}

