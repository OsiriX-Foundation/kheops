package online.kheops.auth_server.user;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.entity.UserPermission;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static online.kheops.auth_server.album.Albums.*;
import static online.kheops.auth_server.user.UserQueries.*;

public class Users {
    private static final Logger LOG = Logger.getLogger(Users.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient();

    private Users() {
        throw new IllegalStateException("Utility class");
    }

    public static User getUser(String userReference, EntityManager em)
            throws UserNotFoundException {

        if (userReference.contains("@")) {
            return findUserByEmail(userReference, em);
        } else {
            return findUserByUserId(userReference, em);
        }
    }

    public static User getUser(String userReference)
            throws UserNotFoundException {
        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            if (userReference.contains("@")) {
                return findUserByEmail(userReference, em);
            } else {
                return findUserByUserId(userReference, em);
            }
        } finally {
            em.close();
        }
    }


    public static User upsertUser(String sub, String name, String email, String welcomebotWebhook, KheopsLogBuilder kheopsLogBuilder) {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            try {
                final User user = getUser(sub, em);
                user.setEmail(email);
                user.setName(name);
                tx.commit();
                kheopsLogBuilder.action(KheopsLogBuilder.ActionType.UPDATE_USER);
                kheopsLogBuilder.user(user.getSub());
                return user;
            } catch (UserNotFoundException unused) { /*empty*/ }

            final User user = new User(sub);
            user.setEmail(email);
            user.setName(name);
            final Album inbox = new Album();
            inbox.setName("inbox");
            inbox.setId(new AlbumId(em).getId());
            user.setInbox(inbox);
            final UsersPermission usersPermission = new UsersPermission();
            usersPermission.setInboxPermission();
            UserPermission userPermission = new UserPermission(usersPermission);
            inbox.setUserPermission(userPermission);

            final AlbumUser albumUser = new AlbumUser(inbox, user, false);
            albumUser.setNewCommentNotifications(false);
            albumUser.setNewSeriesNotifications(false);

            em.persist(inbox);
            em.persist(user);
            em.persist(albumUser);

            tx.commit();

            kheopsLogBuilder.action(KheopsLogBuilder.ActionType.NEW_USER);
            kheopsLogBuilder.user(user.getSub());

            // Go tickle the welcomebot when a new user is added.
            // Block until the reply so that the welcome bot has an opportunity to call back to the
            // Authorization server and share series/albums.

            if(welcomebotWebhook != null && welcomebotWebhook.compareTo("") != 0) {
                try {
                    LOG.log(INFO, "About to try to share with the welcomebot");
                    CLIENT.target(welcomebotWebhook)
                            .queryParam("user", user.getSub())
                            .queryParam("email", email)
                            .queryParam("name", name)
                            .request()
                            .post(Entity.text(""));
                } catch (ProcessingException | WebApplicationException e) {
                    LOG.log(SEVERE, "Unable to communicate with the welcomebot", e);
                }
                LOG.log(INFO, "Finished sharing with the welcomebot");
            }

            return user;
        } catch (PersistenceException e) {
            try {
                tx.rollback();
                tx.begin();
                final User user = getUser(sub, em);
                user.setEmail(email);
                user.setName(name);
                tx.commit();
                kheopsLogBuilder.action(KheopsLogBuilder.ActionType.UPDATE_USER);
                kheopsLogBuilder.user(user.getSub());
                return user;
            } catch (UserNotFoundException unused) {
                throw new IllegalStateException();
            }
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static List<UserResponse> searchUsersInAlbum(String search, String albumId, Integer limit, Integer offset)
            throws AlbumNotFoundException, UserNotMemberException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            final List<User> userList = SearchUserByAlbumId(search, albumId, limit, offset, em);
            final List<UserResponse> userResponseList = new ArrayList<>();
            final Album album = getAlbum(albumId, em);
            for (User user : userList) {
                final AlbumUser albumUser = getAlbumUser(album, user, em);
                userResponseList.add(new UserResponse(albumUser));
            }
            return userResponseList;
        } finally {
            em.close();
        }
    }

    public static List<UserResponse> searchUsersInStudy(String search, String studyInstanceUID, Integer limit, Integer offset) {

        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            final List<User> userList = SearchUserByStudyUID(search, studyInstanceUID, limit, offset, em);
            final List<UserResponse> userResponseList = new ArrayList<>();
            for (User user : userList) {
                userResponseList.add(new UserResponse(user));
            }
            return userResponseList;
        } finally {
            em.close();
        }
    }

    public static List<UserResponse> searchUsers(String search, Integer limit, Integer offset) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            final List<User> userList = SearchUser(search, limit, offset, em);
            final List<UserResponse> userResponseList = new ArrayList<>();
            for (User user : userList) {
                userResponseList.add(new UserResponse(user));
            }
            return userResponseList;
        } finally {
            em.close();
        }
    }
}
