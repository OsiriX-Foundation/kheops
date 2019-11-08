package online.kheops.auth_server.inbox;

import online.kheops.auth_server.album.JOOQException;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.util.KheopsLogBuilder;


public class Inbox {

    private Inbox() {
        throw new IllegalStateException("Utility class");
    }


    public static InboxInfoResponse getInboxInfo(User user, KheopsLogBuilder kheopsLogBuilder)
            throws JOOQException {

        final InboxInfoResponse inboxInfoResponse = InboxQueries.getInboxInfo(user.getPk());

        kheopsLogBuilder.action(KheopsLogBuilder.ActionType.INBOX_INFO)
                .log();

        return inboxInfoResponse;
    }
}
