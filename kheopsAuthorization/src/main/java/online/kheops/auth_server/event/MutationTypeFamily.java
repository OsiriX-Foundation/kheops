package online.kheops.auth_server.event;

import java.util.Arrays;
import java.util.List;

public enum MutationTypeFamily {
    WEBHOOKS (Arrays.asList(
            MutationType.CREATE_WEBHOOK,
            MutationType.DELETE_WEBHOOK,
            MutationType.EDIT_WEBHOOK,
            MutationType.TRIGGER_WEBHOOK) ),
    SENDING (Arrays.asList(
            MutationType.IMPORT_SERIES,
            MutationType.IMPORT_STUDY,
            MutationType.REMOVE_SERIES,
            MutationType.REMOVE_STUDY) ),
    USERS (Arrays.asList(
            MutationType.ADD_USER,
            MutationType.REMOVE_USER,
            MutationType.ADD_ADMIN,
            MutationType.PROMOTE_ADMIN,
            MutationType.DEMOTE_ADMIN,
            MutationType.LEAVE_ALBUM) ),
    REPORT_PROVIDER (Arrays.asList(
            MutationType.CREATE_REPORT_PROVIDER,
            MutationType.EDIT_REPORT_PROVIDER,
            MutationType.DELETE_REPORT_PROVIDER) );

    private final List<MutationType> mutationTypes;

    MutationTypeFamily(List<MutationType> mutationTypes) {
        this.mutationTypes = mutationTypes;
    }

    public List<MutationType> getMutationTypes() {
        return mutationTypes;
    }
}
