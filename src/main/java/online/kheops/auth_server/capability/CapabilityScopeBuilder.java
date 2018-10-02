package online.kheops.auth_server.capability;

public class CapabilityScopeBuilder{
    private final CapabilityParametersBuilder capabilityParametersBuilder;

    private Long albumPk;
    private String seriesInstanceUID;
    private String studyInstanceUID;
    private ScopeType scopeType;

    public CapabilityScopeBuilder(CapabilityParametersBuilder capabilityParametersBuilder) {
        this.capabilityParametersBuilder = capabilityParametersBuilder;
    }

    public CapabilityParametersBuilder albumScope(Long albumPK) {
        this.albumPk = albumPK;
        scopeType = ScopeType.ALBUM;
        return capabilityParametersBuilder.scope(this);
    }
    public CapabilityParametersBuilder seriesScope(String seriesInstanceUID, String studyInstanceUID) {
        this.seriesInstanceUID = seriesInstanceUID;
        this.studyInstanceUID = studyInstanceUID;
        scopeType = ScopeType.SERIES;
        return capabilityParametersBuilder.scope(this);
    }
    public CapabilityParametersBuilder studyScope(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
        scopeType = ScopeType.STUDY;
        return capabilityParametersBuilder.scope(this);
    }
    public CapabilityParametersBuilder userScope() {
        scopeType = ScopeType.USER;
        return capabilityParametersBuilder.scope(this);
    }

    public Long getAlbumPk() { return albumPk; }
    public String getSeriesInstanceUID() { return seriesInstanceUID; }
    public String getStudyInstanceUID() { return studyInstanceUID; }
    public ScopeType getScopeType() { return scopeType; }
}
