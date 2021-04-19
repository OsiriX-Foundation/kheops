package online.kheops.auth_server.user;

import online.kheops.auth_server.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

import static online.kheops.auth_server.util.JPANamedQueryConstants.*;

public class UserQueries {

    private UserQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static User findUserByUserId(String userId, EntityManager em) throws UserNotFoundException{
        try {
            TypedQuery<User> query = em.createNamedQuery("User.findById", User.class);
            query.setParameter(USER_ID, userId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new UserNotFoundException();
        }
    }

    public static User findUserByEmail(String email, EntityManager em) throws UserNotFoundException{
        try {
            TypedQuery<User> query = em.createNamedQuery("User.findByEmail", User.class);
            query.setParameter(USER_EMAIL, email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new UserNotFoundException();
        }
    }


    public static List<User> SearchUserByAlbumId(String search, String albumId, Integer limit, Integer offset, EntityManager em) {

        TypedQuery<User> query = em.createNamedQuery("User.searchByEmailOrNameInAlbumId", User.class);
        query.setParameter(ALBUM_ID, albumId);
        query.setParameter(SEARCH_EMAIL, searchByMail(search));
        query.setParameter(SEARCH_NAME, searchByName(search));
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public static List<User> SearchUserByStudyUID(String search, String studyUID, Integer limit, Integer offset, EntityManager em) {

        TypedQuery<User> query = em.createNamedQuery("User.searchByEmailWithStudyAccess", User.class);
        query.setParameter(STUDY_UID, studyUID);
        query.setParameter(SEARCH_EMAIL, searchByMail(search));
        query.setParameter(SEARCH_NAME, searchByName(search));
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }


    public static List<User> SearchUser(String search, Integer limit, Integer offset, EntityManager em) {

        TypedQuery<User> query = em.createNamedQuery("User.searchByEmailOrName", User.class);
        query.setParameter(SEARCH_EMAIL, searchByMail(search));
        query.setParameter(SEARCH_NAME, searchByName(search));
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    private static String searchByName(String search) {
        search = search.replace("_", "\\_").replace("%", "\\%");
        return "%" + search + "%";
    }

    private static String searchByMail(String search) {
        search = search.replace("_", "\\_").replace("%", "\\%");
        if(search.contains("@")) {
            return search + "%";
        } else {
            return "%" + search + "%@%";
        }
    }
}