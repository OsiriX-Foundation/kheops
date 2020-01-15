package online.kheops.auth_server.capability;

import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.entity.User;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public class CapabilityParametersBuilder {

    private User callingUser;
    private String title;
    private LocalDateTime expirationTime;
    private LocalDateTime notBeforeTime;
    private boolean readPermission;
    private boolean writePermission;
    private boolean downloadPermission;
    private boolean appropriatePermission;
    private CapabilityScopeBuilder capabilityScopeBuilder;

    public CapabilityParametersBuilder() { /*empty*/ }

    public CapabilityParametersBuilder callingUser(User callingUser) {
        this.callingUser = callingUser;
        return this;
    }

    public CapabilityParametersBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * @throws DateTimeParseException if the text cannot be parsed
     */
    private LocalDateTime stringToLocalDateTime(String dateTime) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTime);
        return LocalDateTime.ofInstant(offsetDateTime.toInstant(), ZoneOffset.UTC);
    }

    /**
     * @throws DateTimeParseException if the text cannot be parsed
     */
    public CapabilityParametersBuilder expirationTime(String expirationTime) {
        this.expirationTime = stringToLocalDateTime(expirationTime);
        return this;
    }

    /**
     * @throws DateTimeParseException if the text cannot be parsed
     */
    public CapabilityParametersBuilder notBeforeTime(String notBeforeTime) {
        this.notBeforeTime = stringToLocalDateTime(notBeforeTime);
        return this;
    }

    protected CapabilityScopeBuilder scope() {
        return new CapabilityScopeBuilder(this);
    }

    public CapabilityParametersBuilder scope(String scopeType, String albumId)
            throws BadQueryParametersException {
        ScopeType.valueOf(scopeType.toUpperCase()).initScope(this, albumId);
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
        if (callingUser == null) {
            throw new IllegalStateException("Missing callingUser");
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

        return new CapabilityParameters(callingUser, title, expirationTime, notBeforeTime,
                capabilityScopeBuilder.getScopeType(), capabilityScopeBuilder.getAlbumId(), capabilityScopeBuilder.getStudyInstanceUID(), capabilityScopeBuilder.getSeriesInstanceUID(),
                readPermission, writePermission, appropriatePermission, downloadPermission);
    }


}
