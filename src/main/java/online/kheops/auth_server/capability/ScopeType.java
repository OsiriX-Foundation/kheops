package online.kheops.auth_server.capability;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.AlbumResponse;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.user.UserNotFoundException;

import java.time.format.DateTimeParseException;

import static online.kheops.auth_server.capability.Capabilities.createAlbumCapability;
import static online.kheops.auth_server.capability.Capabilities.createUserCapability;

public enum ScopeType {
    USER {
        @Override
        public CapabilitiesResponse generateCapability(CapabilityParameters capabilityParameters)
                throws CapabilityBadRequestException {
            return createUserCapability(capabilityParameters);
        }

        @Override
        public CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, String albumId) {
            return capabilityParametersBuilder.scope().userScope();
        }

        @Override
        public void setCapabilityResponse(CapabilitiesResponse capabilityResponse, Capability capability) {
            capabilityResponse.setScopeType(this.name().toLowerCase());
        }

        @Override
        public void setCapabilityEntityScope(Capability capability, Album album, Study study, Series series) {
            capability.setScopeType(this.name().toLowerCase());
            capability.getUser().getCapabilities().add(capability);
        }
    },
    ALBUM {
        @Override
        public CapabilitiesResponse generateCapability(CapabilityParameters capabilityParameters)
                throws AlbumNotFoundException, NewCapabilityForbidden, CapabilityBadRequestException, UserNotMemberException {
            return createAlbumCapability(capabilityParameters);
        }

        @Override
        public CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, String albumId)
                throws CapabilityBadRequestException {
            if (albumId == null) {
                throw new CapabilityBadRequestException("The {album} query parameter must be set");
            }
            return capabilityParametersBuilder.scope().albumScope(albumId);
        }

        @Override
        public void setCapabilityResponse(CapabilitiesResponse capabilityResponse, Capability capability) {
            final AlbumResponse albumResponse = new AlbumResponse(capability);
            capabilityResponse.setAlbumResponse(albumResponse);
            capabilityResponse.setScopeType(this.name().toLowerCase());
            capabilityResponse.setAppropriatePermission(capability.isAppropriatePermission());
            capabilityResponse.setDownloadPermission(capability.isDownloadPermission());
            capabilityResponse.setReadPermission(capability.isReadPermission());
            capabilityResponse.setWritePermission(capability.isWritePermission());
        }

        @Override
        public void setCapabilityEntityScope(Capability capability, Album album, Study study, Series series) throws CapabilityBadRequestException {
            if (album == null) {
                throw new CapabilityBadRequestException("\"album\" must be set");
            }
            capability.setAlbum(album);
            capability.setScopeType(this.name().toLowerCase());
            album.addCapability(capability);
        }
    };

    public abstract CapabilitiesResponse generateCapability(CapabilityParameters capabilityParameters)
            throws UserNotFoundException, AlbumNotFoundException, NewCapabilityForbidden, CapabilityBadRequestException, UserNotMemberException;

    /**
     * @throws DateTimeParseException if the text cannot be parsed
     */
    public abstract CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, String albumId)
            throws CapabilityBadRequestException;

    public abstract void setCapabilityResponse(CapabilitiesResponse capabilityResponse, Capability capability);

    public abstract void setCapabilityEntityScope(Capability capability, Album album, Study study, Series series) throws CapabilityBadRequestException;

}
