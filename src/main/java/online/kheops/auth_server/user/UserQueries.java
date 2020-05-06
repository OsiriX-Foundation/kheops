package online.kheops.auth_server.user;

import online.kheops.auth_server.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class UserQueries {

    private UserQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static User findUserByUserId(String userId, EntityManager em) throws UserNotFoundException{
        try {
            TypedQuery<User> query = em.createNamedQuery("User.findById", User.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new UserNotFoundException();
        }
    }

    public static User findUserByEmail(String email, EntityManager em) throws UserNotFoundException{
        try {
            TypedQuery<User> query = em.createNamedQuery("User.findByEmail", User.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new UserNotFoundException();
        }
    }


    public static List<User> SearchUserByAlbumId(String search, String albumId, Integer limit, Integer offset, EntityManager em) {

        TypedQuery<User> query = em.createNamedQuery("User.searchByEmailInAlbumId", User.class);
        query.setParameter("albumId", albumId);
        query.setParameter("search", "%" + search + "%");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public static List<User> SearchUserByStudyUID(String search, String studyUID, Integer limit, Integer offset, EntityManager em) {

        TypedQuery<User> query = em.createNamedQuery("User.searchByEmailWithStudyAccess", User.class);
        query.setParameter("studyUID", studyUID);
        query.setParameter("search", "%" + search + "%");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }


    public static List<User> SearchUser(String search, Integer limit, Integer offset, EntityManager em) {

        TypedQuery<User> query = em.createNamedQuery("User.searchByEmail", User.class);
        query.setParameter("search", "%" + search + "%");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }



}