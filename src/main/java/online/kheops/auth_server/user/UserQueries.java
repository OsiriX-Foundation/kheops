package online.kheops.auth_server.user;

import online.kheops.auth_server.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class UserQueries {

    private UserQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static User findUserByUserId(String userId, EntityManager em) throws UserNotFoundException{
        try {
            TypedQuery<User> query = em.createQuery("SELECT u from User u where u.keycloakId = :userId", User.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new UserNotFoundException();
        }
    }

    public static User findUserByPk(long userPk, EntityManager em)  {
        return em.find(User.class, userPk);
    }
}