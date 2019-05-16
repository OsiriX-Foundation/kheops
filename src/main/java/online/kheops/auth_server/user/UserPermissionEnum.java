package online.kheops.auth_server.user;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Capability;

public enum UserPermissionEnum {

    ADD_USER {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.isAddUser();
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return false;
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }

    },
    REMOVE_USER {
        @Override
        public boolean hasUserPermission(Album album) {
            return true;
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return false;
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }


    },
    ADD_ADMIN {
        @Override
        public boolean hasUserPermission(Album album) {
            return false;
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return false;
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }


    },
    REMOVE_ADMIN {
        @Override
        public boolean hasUserPermission(Album album) {
            return false;
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return false;
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }


    },
    DOWNLOAD_SERIES {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.isDownloadSeries();
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return capability.isReadPermission() && capability.isDownloadPermission();
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return true; }


    },
    SEND_SERIES {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.isSendSeries();
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return capability.isReadPermission() && capability.isAppropriatePermission();
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }


    },
    DELETE_SERIES {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.isDeleteSeries();
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return  capability.isReadPermission() && capability.isWritePermission();
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }


    },
    ADD_SERIES {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.isAddSeries();
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return capability.isWritePermission();
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
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
            return capability.isReadPermission();
        }
        @Override
        public boolean hasViewerPermission(Album album) { return true; }
        @Override
        public boolean hasProviderPermission(Album album) { return true; }


    },
    WRITE_COMMENT {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.isWriteComments();
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return false;
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }


    },
    READ_COMMENT {
        @Override
        public boolean hasUserPermission(Album album) { return true; }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return false;
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }


    },
    EDIT_ALBUM {
        @Override
        public boolean hasUserPermission(Album album) {
            return true;
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return false;
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }


    },
    DELETE_ALBUM {
        @Override
        public boolean hasUserPermission(Album album) {
            return false;
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return false;
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }


    },
    LIST_USERS {
        @Override
        public boolean hasUserPermission(Album album) {
            return true;
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return false;
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }


    },
    MANAGE_CAPABILITIES_TOKEN {
        @Override
        public boolean hasUserPermission(Album album) {
            return false;
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return false;
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }


    },
    EDIT_FAVORITES {
        @Override
        public boolean hasUserPermission(Album album) {
            return album.isAddSeries();
        }
        @Override
        public boolean hasCapabilityPermission(Capability capability) {
            return false;
        }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }

    },
    MANAGE_DICOM_SR {
        @Override
        public boolean hasUserPermission(Album album) { return false; }
        @Override
        public boolean hasCapabilityPermission(Capability capability) { return false; }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }

    },
    GET_DICOM_SR {
        @Override
        public boolean hasUserPermission(Album album) { return true; }
        @Override
        public boolean hasCapabilityPermission(Capability capability) { return false; }
        @Override
        public boolean hasViewerPermission(Album album) { return false; }
        @Override
        public boolean hasProviderPermission(Album album) { return false; }

    }
    ;

    public abstract boolean hasUserPermission(Album album);
    public abstract boolean hasCapabilityPermission(Capability capability);
    public abstract boolean hasViewerPermission(Album album);
    public abstract boolean hasProviderPermission(Album album);
}
