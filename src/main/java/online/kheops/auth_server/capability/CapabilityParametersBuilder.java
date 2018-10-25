package online.kheops.auth_server.capability;

import java.time.*;
import java.time.format.DateTimeParseException;

public class CapabilityParametersBuilder {

    private Long callingUserPk;
    private String title;
    private LocalDateTime expirationTime;
    private LocalDateTime notBeforeTime;
    private boolean readPermission;
    private boolean writePermission;
    private boolean downloadPermission;
    private boolean appropriatePermission;
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

    public CapabilityParametersBuilder notBeforeTime(String notBeforeTime) throws DateTimeParseException{
        this.notBeforeTime = stringToLocalDateTime(notBeforeTime);
        return this;
    }

    protected CapabilityScopeBuilder scope() {
        return new CapabilityScopeBuilder(this);
    }

    public CapabilityParametersBuilder scope(String scopeType, Long albumPk)
    throws CapabilityBadRequestException {
        ScopeType.valueOf(scopeType.toUpperCase()).initScope(this, albumPk);
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

    public CapabilityParametersBuilder appropriatePermission(boolean appropriatePermission) {
        this.appropriatePermission = appropriatePermission;
        return this;
    }

    public CapabilityParametersBuilder downloadPermission(boolean downloadPermission) {
        this.downloadPermission = downloadPermission;
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

        if (notBeforeTime == null) {
            notBeforeTime = LocalDateTime.now(ZoneOffset.UTC);
        }

        return new CapabilityParameters(callingUserPk, title, expirationTime, notBeforeTime,
                capabilityScopeBuilder.getScopeType(), capabilityScopeBuilder.getAlbumPk(), capabilityScopeBuilder.getStudyInstanceUID(), capabilityScopeBuilder.getSeriesInstanceUID(),
                readPermission, writePermission, appropriatePermission, downloadPermission);
    }


}
