package online.kheops.auth_server.inbox;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.persistence.EntityManager;


public class Inbox {

    private Inbox() {
        throw new IllegalStateException("Utility class");
    }


    public static InboxInfoResponse getInboxInfo(User user, KheopsLogBuilder kheopsLogBuilder) {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final InboxInfoResponse inboxInfoResponse = InboxQueries.getInboxInfo(user.getPk(), em);

        kheopsLogBuilder.action(KheopsLogBuilder.ActionType.INBOX_INFO)
                .log();

        return inboxInfoResponse;
    }
}
