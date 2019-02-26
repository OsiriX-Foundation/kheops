package online.kheops.auth_server.capability;

import online.kheops.auth_server.album.AlbumNotFoundException;
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
        public CapabilitiesResponse.Response generateCapability(CapabilityParameters capabilityParameters)
                throws UserNotFoundException, DateTimeParseException, CapabilityBadRequestException {
            return createUserCapability(capabilityParameters);
        }

        @Override
        public CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, String albumId) {
            return capabilityParametersBuilder.scope().userScope();
        }

        @Override
        public CapabilitiesResponse.Response setCapabilityResponse(CapabilitiesResponse.Response capabilityResponse, Capability capability) {
            capabilityResponse.scopeType = this.name().toLowerCase();
            return capabilityResponse;
        }

        @Override
        public void setCapabilityEntityScope(Capability capability, Album album, Study study, Series series) {
            capability.setScopeType(this.name().toLowerCase());
            capability.getUser().getCapabilities().add(capability);
        }
    },
    ALBUM {
        @Override
        public CapabilitiesResponse.Response generateCapability(CapabilityParameters capabilityParameters)
                throws UserNotFoundException, DateTimeParseException, AlbumNotFoundException, NewCapabilityForbidden, CapabilityBadRequestException, UserNotMemberException {
            return createAlbumCapability(capabilityParameters);
        }

        @Override
        public CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, String albumId)
                throws CapabilityBadRequestException {
            if( albumId == null) {
                throw new CapabilityBadRequestException("The {album} query parameter must be set");
            }
            return capabilityParametersBuilder.scope().albumScope(albumId);
        }
        @Override
        public CapabilitiesResponse.Response setCapabilityResponse(CapabilitiesResponse.Response capabilityResponse, Capability capability) {
            CapabilitiesResponse.AlbumScope albumScope =  new CapabilitiesResponse.AlbumScope();
            albumScope.id = capability.getAlbum().getId();
            albumScope.name = capability.getAlbum().getName();
            capabilityResponse.albumScope =  albumScope;
            capabilityResponse.scopeType = this.name().toLowerCase();
            capabilityResponse.appropriatePermission = capability.isAppropriatePermission();
            capabilityResponse.downloadPermission = capability.isDownloadPermission();
            capabilityResponse.readPermission = capability.isReadPermission();
            capabilityResponse.writePermission = capability.isWritePermission();
            return capabilityResponse;
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

    public abstract CapabilitiesResponse.Response generateCapability(CapabilityParameters capabilityParameters)
            throws UserNotFoundException, DateTimeParseException, AlbumNotFoundException, NewCapabilityForbidden, CapabilityBadRequestException, UserNotMemberException;

    public abstract CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, String albumId)
            throws CapabilityBadRequestException;

    public abstract CapabilitiesResponse.Response setCapabilityResponse(CapabilitiesResponse.Response capabilityResponse, Capability capability);

    public abstract void setCapabilityEntityScope(Capability capability, Album album, Study study, Series series) throws CapabilityBadRequestException;

}
