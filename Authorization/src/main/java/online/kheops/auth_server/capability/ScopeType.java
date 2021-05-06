package online.kheops.auth_server.capability;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.AlbumResponse;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;

import java.time.format.DateTimeParseException;

import static online.kheops.auth_server.capability.Capabilities.createAlbumCapability;
import static online.kheops.auth_server.capability.Capabilities.createUserCapability;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;

public enum ScopeType {
    USER {
        @Override
        public CapabilitiesResponse generateCapability(CapabilityParameters capabilityParameters, KheopsLogBuilder kheopsLogBuilder)
                throws BadQueryParametersException {
            return createUserCapability(capabilityParameters, kheopsLogBuilder);
        }

        @Override
        public CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, String albumId) {
            return capabilityParametersBuilder.scope().userScope();
        }

        @Override
        public void setCapabilityResponse(CapabilitiesResponse capabilityResponse, Capability capability) {
            capabilityResponse.setScopeType(this.name().toLowerCase());
            capabilityResponse.setOriginNull();
        }

        @Override
        public void setCapabilityEntityScope(Capability capability, Album album, Study study, Series series) {
            capability.setScopeType(this);
            //capability.getUser().getCapabilities().add(capability);
        }
    },
    ALBUM {
        @Override
        public CapabilitiesResponse generateCapability(CapabilityParameters capabilityParameters, KheopsLogBuilder kheopsLogBuilder)
                throws AlbumNotFoundException, NewCapabilityForbidden, BadQueryParametersException, UserNotMemberException {
            return createAlbumCapability(capabilityParameters, kheopsLogBuilder);
        }

        @Override
        public CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, String albumId)
                throws BadQueryParametersException {
            if (albumId == null) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("'album' query parameter muste be set with an album id")
                        .build();
                throw new BadQueryParametersException(errorResponse);
            }
            return capabilityParametersBuilder.scope().albumScope(albumId);
        }

        @Override
        public void setCapabilityResponse(CapabilitiesResponse capabilityResponse, Capability capability) {
            final AlbumResponse albumResponse = new AlbumResponse(capability);
            capabilityResponse.setAlbumResponse(albumResponse);
            capabilityResponse.setScopeType(this.name().toLowerCase());
            capabilityResponse.setAppropriatePermission(capability.hasAppropriatePermission());
            capabilityResponse.setDownloadPermission(capability.hasDownloadButtonPermission());
            capabilityResponse.setReadPermission(capability.hasReadPermission());
            capabilityResponse.setWritePermission(capability.hasWritePermission());
        }

        @Override
        public void setCapabilityEntityScope(Capability capability, Album album, Study study, Series series) throws BadQueryParametersException {
            if (album == null) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("'album' query parameter muste be set with an album id")
                        .build();
                throw new BadQueryParametersException(errorResponse);
            }
            capability.setAlbum(album);
            capability.setScopeType(this);
            album.addCapability(capability);
        }
    };

    public abstract CapabilitiesResponse generateCapability(CapabilityParameters capabilityParameters, KheopsLogBuilder kheopsLogBuilder)
            throws UserNotFoundException, AlbumNotFoundException, NewCapabilityForbidden, BadQueryParametersException, UserNotMemberException;

    /**
     * @throws DateTimeParseException if the text cannot be parsed
     */
    public abstract CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, String albumId)
            throws BadQueryParametersException;

    public abstract void setCapabilityResponse(CapabilitiesResponse capabilityResponse, Capability capability);

    public abstract void setCapabilityEntityScope(Capability capability, Album album, Study study, Series series) throws BadQueryParametersException;

}
