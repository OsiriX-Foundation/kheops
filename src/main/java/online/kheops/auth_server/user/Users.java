package online.kheops.auth_server.user;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.keycloak.KeycloakException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import static online.kheops.auth_server.user.UserQueries.findUserByPk;
import static online.kheops.auth_server.user.UserQueries.findUserByUserId;

public class Users {
    private static final Logger LOG = Logger.getLogger(Users.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient();

    private Users() {
        throw new IllegalStateException("Utility class");
    }

    private static User getUser(String userReference, EntityManager entityManager)
            throws UserNotFoundException {

        if(userReference.contains("@")) {
            try {
                final Keycloak keycloak = Keycloak.getInstance();
                userReference= keycloak.getUser(userReference.toLowerCase()).getSub();
            } catch (KeycloakException e) {
                throw new UserNotFoundException("Error during request to keycloak", e);
            }
        }

        return findUserByUserId(userReference, entityManager);
    }

    public static boolean userExist(long callingUserPk, EntityManager entityManager){
        return findUserByPk(callingUserPk, entityManager) != null;
    }

    public static User getOrCreateUser(String userReference)
            throws UserNotFoundException {

        final EntityManager getUserEntityManager = EntityManagerListener.createEntityManager();
        //try to find the user in kheops db
        try {
            return getUser(userReference, getUserEntityManager);
        } catch (UserNotFoundException e) {
            LOG.log(WARNING, "User was not found in the db, going on to try to add the user", e);
        } finally {
            getUserEntityManager.close();
        }

        //the user is not in kheops db
        //try to find the user in keycloak
        try {
            final Keycloak keycloak = Keycloak.getInstance();
            userReference = keycloak.getUser(userReference).getSub();
        } catch (KeycloakException e) {
            throw new UserNotFoundException("Error during request to keycloak", e);
        }

        //the user is in keycloak but not in kheops => add the user in kheops
        LOG.log(INFO, "Adding new user: " + userReference);

        final User newUser;
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
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
        } catch (PersistenceException e) {
            final EntityManager secondTryEntityManager = EntityManagerListener.createEntityManager();
            try {
                return getUser(userReference, secondTryEntityManager);
            } catch (UserNotFoundException notFoundException) {
                notFoundException.addSuppressed(e);
                throw notFoundException;
            } finally {
                secondTryEntityManager.close();
            }
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
        try {
            LOG.log(INFO, "About to try to share with the welcomebot");
            CLIENT.target("http://welcomebot:8080/share")
                    .queryParam("user", newUser.getKeycloakId())
                    .request()
                    .post(Entity.text(""));
        } catch (ProcessingException | WebApplicationException e) {
            LOG.log(SEVERE, "Unable to communicate with the welcomebot", e);
        }
        LOG.log(INFO, "Finished sharing with the welcomebot");

        return newUser;
    }
}
