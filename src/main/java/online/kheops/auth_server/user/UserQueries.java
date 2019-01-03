package online.kheops.auth_server.user;

import online.kheops.auth_server.entity.User;

import javax.persistence.*;

public class UserQueries {

    private UserQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static User findUserByUsername(String username, EntityManager em) throws UserNotFoundException{
        try {
            if (username.contains("@")) {
                TypedQuery<User> googleEmailQuery = em.createQuery("SELECT u from User u where u.googleEmail = :googleEmail", User.class);
                googleEmailQuery.setParameter("googleEmail", username);
                return googleEmailQuery.getSingleResult();
            } else {
                TypedQuery<User> googleIdQuery = em.createQuery("SELECT u from User u where u.googleId = :googleId", User.class);
                googleIdQuery.setParameter("googleId", username);
                return googleIdQuery.getSingleResult();
            }
        } catch (NoResultException e) {
            throw new UserNotFoundException();
        }
    }

    public static User findUserByPk(long userPk, EntityManager em)  {
        return em.find(User.class, userPk);
    }
}