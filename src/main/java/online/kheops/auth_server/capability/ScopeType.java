package online.kheops.auth_server.capability;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.Consts;

import java.time.format.DateTimeParseException;

import static online.kheops.auth_server.capability.Capabilities.*;
import static online.kheops.auth_server.series.Series.checkValidUID;

public enum ScopeType {
    USER {
        @Override
        public CapabilitiesResponses.CapabilityResponse generateCapability(CapabilityParameters capabilityParameters)
                throws UserNotFoundException, DateTimeParseException, CapabilityBadRequest {
            return createUserCapability(capabilityParameters);
        }

        @Override
        public CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, Long albumPk) {
            return capabilityParametersBuilder.scope().userScope();
        }

        @Override
        public CapabilitiesResponses.CapabilityResponse setCapabilityResponse(CapabilitiesResponses.CapabilityResponse capabilityResponse, Capability capability) {
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
        public CapabilitiesResponses.CapabilityResponse generateCapability(CapabilityParameters capabilityParameters)
                throws UserNotFoundException, DateTimeParseException, AlbumNotFoundException, NewCapabilityForbidden, CapabilityBadRequest {
            return createAlbumCapability(capabilityParameters);
        }

        @Override
        public CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, Long albumPk)
                throws CapabilityBadRequest {
            if( albumPk == null) {
                throw new CapabilityBadRequest("The {album} query parameter must be set");
            }
            return capabilityParametersBuilder.scope().albumScope(albumPk);
        }
        @Override
        public CapabilitiesResponses.CapabilityResponse setCapabilityResponse(CapabilitiesResponses.CapabilityResponse capabilityResponse, Capability capability) {
            capabilityResponse.albumId = capability.getAlbum().getPk();
            capabilityResponse.scopeType = this.name().toLowerCase();
            capabilityResponse.appropriatePermission = capability.isAppropriatePermission();
            capabilityResponse.downloadPermission = capability.isDownloadPermission();
            capabilityResponse.readPermission = capability.isReadPermission();
            capabilityResponse.writePermission = capability.isWritePermission();
            return capabilityResponse;
        }

        @Override
        public void setCapabilityEntityScope(Capability capability, Album album, Study study, Series series) throws CapabilityBadRequest{
            if (album == null) {
                throw new CapabilityBadRequest("\"album\" must be set");
            }
            capability.setAlbum(album);
            capability.setScopeType(this.name().toLowerCase());
            album.addCapability(capability);
        }
    };

    public abstract CapabilitiesResponses.CapabilityResponse generateCapability(CapabilityParameters capabilityParameters)
            throws UserNotFoundException, DateTimeParseException, AlbumNotFoundException, NewCapabilityForbidden, CapabilityBadRequest;

    public abstract CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, Long albumPk)
            throws CapabilityBadRequest;

    public abstract CapabilitiesResponses.CapabilityResponse setCapabilityResponse(CapabilitiesResponses.CapabilityResponse capabilityResponse, Capability capability);

    public abstract void setCapabilityEntityScope(Capability capability, Album album, Study study, Series series) throws CapabilityBadRequest;

}
