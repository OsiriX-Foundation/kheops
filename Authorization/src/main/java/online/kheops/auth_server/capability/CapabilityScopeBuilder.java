package online.kheops.auth_server.capability;

public class CapabilityScopeBuilder{
    private final CapabilityParametersBuilder capabilityParametersBuilder;

    private String albumId;
    private String seriesInstanceUID;
    private String studyInstanceUID;
    private ScopeType scopeType;

    public CapabilityScopeBuilder(CapabilityParametersBuilder capabilityParametersBuilder) {
        this.capabilityParametersBuilder = capabilityParametersBuilder;
    }

    public CapabilityParametersBuilder albumScope(String albumId) {
        this.albumId = albumId;
        scopeType = ScopeType.ALBUM;
        return capabilityParametersBuilder.scope(this);
    }
    public CapabilityParametersBuilder userScope() {
        scopeType = ScopeType.USER;
        return capabilityParametersBuilder.scope(this);
    }

    public String getAlbumId() { return albumId; }
    public String getSeriesInstanceUID() { return seriesInstanceUID; }
    public String getStudyInstanceUID() { return studyInstanceUID; }
    public ScopeType getScopeType() { return scopeType; }
}
