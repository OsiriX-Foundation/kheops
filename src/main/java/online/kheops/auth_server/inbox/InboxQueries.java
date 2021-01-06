package online.kheops.auth_server.inbox;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class InboxQueries {

    private InboxQueries() {
        throw new IllegalStateException("Utility class");
    }


    public static InboxInfoResponse getInboxInfoJPA(long userPk, EntityManager em) {

        TypedQuery<InboxInfoResponse> query = em.createNamedQuery("Albums.getInboxInfoByUserPk", InboxInfoResponse.class);
        query.setParameter("userPk", userPk);
        return query.getSingleResult();
    }



}
