package online.kheops.auth_server.user;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

import static online.kheops.auth_server.user.UserQueries.*;

public class Users {
    private Users() {
        throw new IllegalStateException("Utility class");
    }

    public static User getUser(long callingUserPk, EntityManager entityManager) throws UserNotFoundException{
        try {
            return findUserByPk(callingUserPk, entityManager);
        } catch (NoResultException e) {
            throw new UserNotFoundException(e);
        }
    }

    public static User getUser(String userName, EntityManager entityManager) throws UserNotFoundException {
        return findUserByUsername(userName, entityManager);
    }

    public static User getUser(String username) throws UserNotFoundException{
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        User user;

        try {
            tx.begin();
            user = getUser(username, em);
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
        try {
            findUserByPk(callingUserPk, entityManager);
        } catch (UserNotFoundException e) {
            return false;
        }
        return true;
    }
}
