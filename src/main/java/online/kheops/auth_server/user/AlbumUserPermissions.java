package online.kheops.auth_server.user;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Capability;

public enum AlbumUserPermissions {

    ADD_USER {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.getUserPermission().isAddUser();
        }
    },
    REMOVE_USER {
        @Override
        public boolean hasUserPermission(Album album) {
            return true;
        }
    },
    ADD_ADMIN,
    REMOVE_ADMIN,
    DOWNLOAD_SERIES {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.getUserPermission().isDownloadSeries();
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return capability.hasReadPermission() && capability.hasDownloadButtonPermission();
        }
    },
    SEND_SERIES {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.getUserPermission().isSendSeries();
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return capability.hasReadPermission() && capability.hasAppropriatePermission();
        }
    },
    DELETE_SERIES {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.getUserPermission().isDeleteSeries();
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return  capability.hasReadPermission() && capability.hasWritePermission();
        }
    },
    ADD_SERIES {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.getUserPermission().isAddSeries();
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return capability.hasWritePermission();
        }
        @Override
        public boolean hasProviderPermission(Album album) { return true; }
    },
    READ_SERIES {
        @Override
        public boolean hasUserPermission(Album album) {
            return true;
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return capability.hasReadPermission();
        }
        @Override
        public boolean hasProviderPermission(Album album) { return true; }
    },
    WRITE_COMMENT {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.getUserPermission().isWriteComments();
        }
    },
    READ_COMMENT {
        @Override
        public boolean hasUserPermission(Album album) { return true; }
    },
    EDIT_ALBUM {
        @Override
        public boolean hasUserPermission(Album album) {
            return true;
        }
    },
    DELETE_ALBUM,
    LIST_USERS {
        @Override
        public boolean hasUserPermission(Album album) {
            return true;
        }
    },
    MANAGE_CAPABILITIES_TOKEN,
    EDIT_FAVORITES {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.getUserPermission().isAddSeries();
        }
    },
    MANAGE_DICOM_SR,
    GET_DICOM_SR {
        @Override
        public boolean hasUserPermission(Album album) { return true; }
    },
    MANAGE_WEBHOOK;

    /**
     * @param album For overrides
     */
    public boolean hasUserPermission(Album album) {
        return false;
    }

    /**
     * @param capability For overrides
     */
    public boolean hasCapabilityPermission(Capability capability) {
        return false;
    }

    /**
     * @param album For overrides
     */
    public boolean hasProviderPermission(Album album) {
        return false;
    }
}
