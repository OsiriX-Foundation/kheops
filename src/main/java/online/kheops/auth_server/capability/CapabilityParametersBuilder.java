package online.kheops.auth_server.capability;

import java.time.*;
import java.time.format.DateTimeParseException;

public class CapabilityParametersBuilder {

    private Long callingUserPk;
    private String title;
    private LocalDateTime expirationTime;
    private LocalDateTime issuedAtTime;
    private boolean readPermission;
    private boolean writePermission;
    private CapabilityScopeBuilder capabilityScopeBuilder;

    public CapabilityParametersBuilder() { }

    public CapabilityParametersBuilder callingUserPk(Long callingUserPk) {
        this.callingUserPk = callingUserPk;
        return this;
    }

    public CapabilityParametersBuilder title(String title) {
        this.title = title;
        return this;
    }

    private LocalDateTime stringToLocalDateTime(String dateTime)  throws DateTimeParseException {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTime);
        return LocalDateTime.ofInstant(offsetDateTime.toInstant(), ZoneOffset.UTC);
    }

    public CapabilityParametersBuilder expirationTime(String expirationTime) throws DateTimeParseException {
        this.expirationTime = stringToLocalDateTime(expirationTime);
        return this;
    }

    public CapabilityParametersBuilder issuedAtTime(String issuedAtTime) throws DateTimeParseException{
        this.issuedAtTime = stringToLocalDateTime(issuedAtTime);
        return this;
    }

    protected CapabilityScopeBuilder scope() {
        return new CapabilityScopeBuilder(this);
    }

    public CapabilityParametersBuilder scope(String scopeType, Long albumPk, String seriesInstanceUID, String studyInstanceUID)
    throws CapabilityBadRequest {
        ScopeType.valueOf(scopeType.toUpperCase()).initScope(this, albumPk, seriesInstanceUID, studyInstanceUID);
        return this;
    }

    protected CapabilityParametersBuilder scope(CapabilityScopeBuilder capabilityScopeBuilder) {
        this.capabilityScopeBuilder = capabilityScopeBuilder;
        return this;
    }

    public CapabilityParametersBuilder readPermission(boolean readPermission) {
        this.readPermission = readPermission;
        return this;
    }

    public CapabilityParametersBuilder writePermission(boolean writePermission) {
        this.writePermission = writePermission;
        return this;
    }

    public CapabilityParameters build() {
        if (callingUserPk == null) {
            throw new IllegalStateException("Missing callingUserPk");
        }
        if (title == null) {
            throw new IllegalStateException("Missing title");
        }

        if (capabilityScopeBuilder == null) {
            throw new IllegalStateException("Missing scope");
        }

        if (expirationTime == null) {
            expirationTime = LocalDateTime.now(ZoneOffset.UTC).plusMonths(3);
        }

        if (issuedAtTime == null) {
            issuedAtTime = LocalDateTime.now(ZoneOffset.UTC);
        }

        return new CapabilityParameters(callingUserPk, title, expirationTime, issuedAtTime,
                capabilityScopeBuilder.getScopeType(), capabilityScopeBuilder.getAlbumPk(), capabilityScopeBuilder.getStudyInstanceUID(), capabilityScopeBuilder.getSeriesInstanceUID(),
                readPermission, writePermission);
    }


}
