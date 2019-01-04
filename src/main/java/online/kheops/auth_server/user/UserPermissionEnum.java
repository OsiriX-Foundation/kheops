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

    };


    public abstract boolean hasUserPermission(Album album);
    public abstract boolean hasCapabilityPermission(Capability capability);
}
