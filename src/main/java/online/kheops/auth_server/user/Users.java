package online.kheops.auth_server.user;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.filter.SecuredFilter;
import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.keycloak.KeycloakException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.user.UserQueries.*;

public class Users {
    private static final Logger LOG = Logger.getLogger(Users.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient();

    private Users() {
        throw new IllegalStateException("Utility class");
    }

    /*public static User getUser(long callingUserPk, EntityManager entityManager) throws UserNotFoundException {
        final User user = findUserByPk(callingUserPk, entityManager);
        if (user != null) {
            return user;
        } else {
            throw new UserNotFoundException("User :" +callingUserPk +"does not exist.");
        }
    }*/

    public static User getUser(String userReference, EntityManager entityManager)
            throws UserNotFoundException {

        if(userReference.contains("@")) {
            try {
                final Keycloak keycloak = Keycloak.getInstance();
                userReference= keycloak.getUser(userReference).getSub();
            } catch (KeycloakException e) {
                throw new UserNotFoundException("Error during request to keycloak", e);
            }
        }

        //try {
            return findUserByUserId(userReference, entityManager);
        //} catch (UserNotFoundException e) {
        //    return getOrCreateUser(userReference);
        //}
    }

    private static User getUser(String userReference) throws UserNotFoundException{
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

    public static User getOrCreateUser(String userReference) throws UserNotFoundException {

        //try to find the user in kheops db
        try {
            return getUser(userReference);
        } catch (UserNotFoundException e) {/*empty*/ }

        //the user is not in kheops db
        //try to find the user in keycloak
        try {
            final Keycloak keycloak = Keycloak.getInstance();
            userReference = keycloak.getUser(userReference).getSub();
        } catch (KeycloakException e) {
            throw new UserNotFoundException("Error during request to keycloak", e);
        }

        //the user is in keycloak but not in kheops => add the user in kheops
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        User newUser;
        try {
            tx.begin();
            newUser = new User(userReference);
            final Album inbox = new Album();
            inbox.setName("inbox");
            newUser.setInbox(inbox);
            final UsersPermission usersPermission = new UsersPermission();
            usersPermission.setInboxPermission();
            inbox.setPermission(usersPermission);

            final AlbumUser albumUser = new AlbumUser(inbox, newUser, false);
            albumUser.setNewCommentNotifications(false);
            albumUser.setNewSeriesNotifications(false);

            em.persist(inbox);
            em.persist(newUser);
            em.persist(albumUser);
            tx.commit();
        } catch (Exception e) {
            throw new UserNotFoundException("Error while adding a new user to the kheops db", e);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        // Demo specific, go tickle the welcomebot when a new user is added.
        // Block until the reply so that the welcome bot has an opportunity to call back to the
        // Authorization server and share series/albums.
        if (newUser != null) {
            try {
                CLIENT.target("http://welcomebot/share")
                        .queryParam("user", newUser.getKeycloakId())
                        .request()
                        .post(Entity.text(""));
            } catch (ProcessingException | WebApplicationException e) {
                LOG.log(Level.SEVERE, "Unable to communicate with the welcomebot", e);
            }
        }

        return newUser;
    }
}
