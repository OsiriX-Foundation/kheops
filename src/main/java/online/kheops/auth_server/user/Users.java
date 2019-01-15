package online.kheops.auth_server.user;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.keycloak.KeycloakException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.user.UserQueries.*;

public class Users {
    private Users() {
        throw new IllegalStateException("Utility class");
    }

    public static User getUser(long callingUserPk, EntityManager entityManager) throws UserNotFoundException {
        final User user = findUserByPk(callingUserPk, entityManager);
        if (user != null) {
            return user;
        } else {
            throw new UserNotFoundException("User :" +callingUserPk +"does not exist.");
        }
    }

    public static User getUser(String userReference, EntityManager entityManager)
            throws UserNotFoundException {

        if(userReference.contains("@")) {
            try {
                final Keycloak keycloak = new Keycloak();
                userReference= keycloak.getUser(userReference).sub;
            } catch (UserNotFoundException | KeycloakException e2) {
                throw new UserNotFoundException();
            }
        }

        return findUserByUserId(userReference, entityManager);
    }

    public static User getUser(String userReference) throws UserNotFoundException{
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        User user;

        try {
            tx.begin();
            user = getUser(userReference, em);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return user;
    }

    public static boolean userExist(long callingUserPk, EntityManager entityManager){
        return findUserByPk(callingUserPk, entityManager) != null;
    }
}
