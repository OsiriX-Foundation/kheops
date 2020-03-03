package online.kheops.auth_server.user;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.keycloak.KeycloakException;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static online.kheops.auth_server.album.Albums.isMemberOfAlbum;
import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.user.UserQueries.findUserByPk;
import static online.kheops.auth_server.user.UserQueries.findUserByUserId;

public class Users {
    private static final Logger LOG = Logger.getLogger(Users.class.getName());

    private Users() {
        throw new IllegalStateException("Utility class");
    }

    private static User getUser(String userReference, EntityManager entityManager)
            throws UserNotFoundException {

        if(userReference.contains("@")) {
            try {
                final Keycloak keycloak = Keycloak.getInstance();
                userReference= keycloak.getUser(userReference).getSub();
            } catch (KeycloakException e) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("Error during request the authority provider")
                        .detail("An error occure during request the authority provider")
                        .build();
                throw new UserNotFoundException(errorResponse, e);
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
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Error during request the authority provider")
                    .detail("An error occure during request the authority provider")
                    .build();
            throw new UserNotFoundException(errorResponse, e);
        }

        //the user is in keycloak but not in kheops => add the user in kheops
        new KheopsLogBuilder().user(userReference)
                .action(KheopsLogBuilder.ActionType.NEW_USER)
                .log();

        final User newUser;
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            newUser = new User(userReference);
            final Album inbox = new Album();
            inbox.setName("inbox");
            inbox.setId(new AlbumId(em).getId());
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
            return newUser;
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
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Error")
                    .detail("Error while adding a new user to the kheops db")
                    .build();
            throw new UserNotFoundException(errorResponse, e);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static List<UserResponse> searchUsersInAlbum(String search, String albumId, Integer limit, Integer offset)
            throws KeycloakException {
        final Keycloak keycloak = Keycloak.getInstance();
        final List<UserResponse> users = keycloak.getUsers(search, 1000, 0);
        List<UserResponse> result = new ArrayList<>();
        for (UserResponse user : users) {
            if (isMemberOfAlbum(user.getSub(), albumId)) {
                if (offset > 0) {
                    offset--;
                } else {
                    result.add(user);
                    limit--;
                    if (limit == 0) {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    public static List<UserResponse> searchUsersInStudy(String search, String studyInstanceUID, Integer limit, Integer offset)
        throws KeycloakException {
        final Keycloak keycloak = Keycloak.getInstance();
        final List<UserResponse> users = keycloak.getUsers(search, 1000, 0);
        List<UserResponse> result = new ArrayList<>();
        for (UserResponse user : users) {
            if (canAccessStudy(user.getSub(), studyInstanceUID)) {
                if (offset > 0) {
                    offset--;
                } else {
                    result.add(user);
                    limit--;
                    if (limit == 0) {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    public static List<UserResponse> searchUsers(String search, Integer limit, Integer offset)
        throws KeycloakException {
        final Keycloak keycloak = Keycloak.getInstance();
        return keycloak.getUsers(search, limit, offset);
    }
}
