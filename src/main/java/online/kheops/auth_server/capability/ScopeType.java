package online.kheops.auth_server.capability;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.Capability;
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
                throws UserNotFoundException, DateTimeParseException  {
            return createUserCapability(capabilityParameters);
        }

        @Override
        public CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, Long albumPk, String seriesInstanceUID, String studyInstanceUID) {
            return capabilityParametersBuilder.scope().userScope();
        }
        @Override
        public CapabilitiesResponses.CapabilityResponse setCapabilityResponse(CapabilitiesResponses.CapabilityResponse capabilityResponse, Capability capability) {
            capabilityResponse.scopeType = this.name().toLowerCase();
            return capabilityResponse;
        }
    },
    ALBUM {
        @Override
        public CapabilitiesResponses.CapabilityResponse generateCapability(CapabilityParameters capabilityParameters)
                throws UserNotFoundException, DateTimeParseException, AlbumNotFoundException, NewCapabilityForbidden {
            return createAlbumCapability(capabilityParameters);
        }

        @Override
        public CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, Long albumPk, String seriesInstanceUID, String studyInstanceUID)
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
            return capabilityResponse;
        }
    },
    SERIES {
        @Override
        public CapabilitiesResponses.CapabilityResponse generateCapability(CapabilityParameters capabilityParameters)
                throws UserNotFoundException, DateTimeParseException, SeriesNotFoundException{
            return createSeriesCapability(capabilityParameters);
        }

        @Override
        public CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, Long albumPk, String seriesInstanceUID, String studyInstanceUID)
                throws CapabilityBadRequest {
            if( seriesInstanceUID == null && studyInstanceUID == null) {
                throw new CapabilityBadRequest("The {series} and {study} query parameters must be set");
            }
            checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);
            checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
            return capabilityParametersBuilder.scope().seriesScope(seriesInstanceUID, studyInstanceUID);
        }

        @Override
        public CapabilitiesResponses.CapabilityResponse setCapabilityResponse(CapabilitiesResponses.CapabilityResponse capabilityResponse, Capability capability) {
            capabilityResponse.series = capability.getSeries().getSeriesInstanceUID();
            capabilityResponse.study = capability.getStudy().getStudyInstanceUID();
            capabilityResponse.scopeType = this.name().toLowerCase();
            return capabilityResponse;
        }
    },
    STUDY {
        @Override
        public CapabilitiesResponses.CapabilityResponse generateCapability(CapabilityParameters capabilityParameters)
                throws UserNotFoundException, DateTimeParseException, StudyNotFoundException{
            return createStudyCapability(capabilityParameters);
        }

        @Override
        public CapabilityParametersBuilder initScope (CapabilityParametersBuilder capabilityParametersBuilder, Long albumPk, String seriesInstanceUID, String studyInstanceUID)
                throws CapabilityBadRequest {
            if( studyInstanceUID == null) {
                throw new CapabilityBadRequest("The {study} query parameter must be set");
            }
            checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
            return capabilityParametersBuilder.scope().studyScope(studyInstanceUID);
        }

        @Override
        public CapabilitiesResponses.CapabilityResponse setCapabilityResponse(CapabilitiesResponses.CapabilityResponse capabilityResponse, Capability capability) {
            capabilityResponse.study = capability.getStudy().getStudyInstanceUID();
            capabilityResponse.scopeType = this.name().toLowerCase();
            return capabilityResponse;
        }
    };

    
    public abstract CapabilitiesResponses.CapabilityResponse generateCapability(CapabilityParameters capabilityParameters)
            throws UserNotFoundException, DateTimeParseException, AlbumNotFoundException,
            NewCapabilityForbidden, SeriesNotFoundException, StudyNotFoundException;

    public abstract CapabilityParametersBuilder initScope(CapabilityParametersBuilder capabilityParametersBuilder, Long albumPk, String seriesInstanceUID, String studyInstanceUID)
            throws CapabilityBadRequest;

    public abstract CapabilitiesResponses.CapabilityResponse setCapabilityResponse(CapabilitiesResponses.CapabilityResponse capabilityResponse, Capability capability);


}
