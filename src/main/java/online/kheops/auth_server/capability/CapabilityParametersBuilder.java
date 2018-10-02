package online.kheops.auth_server.capability;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public class CapabilityParametersBuilder {

    private Long callingUserPk;
    private String title;
    private LocalDateTime expirationDate;
    private LocalDateTime startDate;
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

    public CapabilityParametersBuilder expiration(String expirationDate) throws DateTimeParseException {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(expirationDate);
        this.expirationDate = LocalDateTime.ofInstant(offsetDateTime.toInstant(), ZoneOffset.UTC);
        return this;
    }

    public CapabilityParametersBuilder start(String startDate) throws DateTimeParseException{
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(startDate);
        this.startDate = LocalDateTime.ofInstant(offsetDateTime.toInstant(), ZoneOffset.UTC);
        return this;
    }

    public CapabilityScopeBuilder scope() {
        return new CapabilityScopeBuilder(this);
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

        if (expirationDate == null) {
            expirationDate = LocalDateTime.now(ZoneOffset.UTC).plusMonths(3);
        }

        if (startDate == null) {
            startDate = LocalDateTime.now(ZoneOffset.UTC);
        }

        return new CapabilityParameters(callingUserPk, title, expirationDate, startDate,
                capabilityScopeBuilder.getScopeType(), capabilityScopeBuilder.getAlbumPk(), capabilityScopeBuilder.getStudyInstanceUID(), capabilityScopeBuilder.getSeriesInstanceUID(),
                readPermission, writePermission);
    }


}
