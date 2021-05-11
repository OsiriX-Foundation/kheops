package online.kheops.auth_server.inbox;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static online.kheops.auth_server.util.JPANamedQueryConstants.USER_PK;

public class InboxQueries {

    private InboxQueries() {
        throw new IllegalStateException("Utility class");
    }


    public static InboxInfoResponse getInboxInfoJPA(long userPk, EntityManager em) {

        TypedQuery<InboxInfoResponse> query = em.createNamedQuery("Albums.getInboxInfoByUserPk", InboxInfoResponse.class);
        query.setParameter(USER_PK, userPk);
        return query.getSingleResult();
    }



}
