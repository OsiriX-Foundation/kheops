package online.kheops.auth_server.album;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UsersPermission;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Pattern;

import static online.kheops.auth_server.album.AlbumQueries.*;
import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.user.Users.userExist;

public class Albums {

    private static final String DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int ID_LENGTH = 10;
    public static final String ID_PATTERN = "[A-Za-z0-9]{" + ID_LENGTH + "}";
    private static final String ID_PATTERN_STRICT = "^" + ID_PATTERN + "$";
    private static final Pattern pattern = Pattern.compile(ID_PATTERN_STRICT);
    private static final Random rdm = new SecureRandom();

    private Albums() {
        throw new IllegalStateException("Utility class");
    }

    public static String newAlbumID() {
        StringBuilder idBuilder = new StringBuilder();

        do {
            idBuilder.setLength(0);
            while (idBuilder.length() < ID_LENGTH) {
                int index = rdm.nextInt(DICT.length());
                idBuilder.append(DICT.charAt(index));
            }
        } while (albumExist(idBuilder.toString()));
        return idBuilder.toString();
    }

    public static AlbumResponse createAlbum(User callingUser, String name, String description, UsersPermission usersPermission)
            throws JOOQException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final AlbumResponse albumResponse;

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final Album newAlbum = new Album(name, description, usersPermission);
            final AlbumUser newAlbumUser = new AlbumUser(newAlbum, callingUser, true);
            final Mutation newAlbumMutation = Events.albumPostNewAlbumMutation(callingUser, newAlbum);

            em.persist(newAlbum);
            em.persist(newAlbumUser);
            em.persist(newAlbumMutation);

            tx.commit();

            albumResponse = findAlbumByUserPkAndAlbumId(newAlbum.getId(), callingUser.getPk());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return albumResponse;
    }

    public static AlbumResponse editAlbum(User callingUser, String albumId, String name, String description, UsersPermission usersPermission,
                                                        Boolean notificationNewComment , Boolean notificationNewSeries)
            throws AlbumNotFoundException, AlbumForbiddenException, JOOQException, UserNotMemberException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final AlbumResponse albumResponse;

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Album editAlbum = getAlbum(albumId, em);
            final AlbumUser callingAlbumUser = getAlbumUser(editAlbum, callingUser, em);

            if (notificationNewComment != null) {
                callingAlbumUser.setNewCommentNotifications(notificationNewComment);
            }
            if (notificationNewSeries != null) {
                callingAlbumUser.setNewSeriesNotifications(notificationNewSeries);
            }

            if (callingAlbumUser.isAdmin()){

                if (name != null) {
                    editAlbum.setName(name);
                }
                if (description != null) {
                    editAlbum.setDescription(description);
                }
                usersPermission.getAddUser().ifPresent(editAlbum::setAddUser);
                usersPermission.getDownloadSeries().ifPresent(editAlbum::setDownloadSeries);
                usersPermission.getSendSeries().ifPresent(editAlbum::setSendSeries);
                usersPermission.getDeleteSeries().ifPresent(editAlbum::setDeleteSeries);
                usersPermission.getAddSeries().ifPresent(editAlbum::setAddSeries);
                usersPermission.getWriteComments().ifPresent(editAlbum::setWriteComments);

                Mutation mutation = Events.albumPostEditMutation(callingUser, editAlbum);
                em.persist(mutation);

            } else if (name != null || description != null || usersPermission.areSet()) {
                throw new AlbumForbiddenException("Not admin: The user must be an admin for editing name, description or permissions");
            }

            tx.commit();

            albumResponse = findAlbumByUserPkAndAlbumId(editAlbum.getId(), callingUser.getPk());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return albumResponse;
    }

    public static PairListXTotalCount<AlbumResponse.Response> getAlbumList(AlbumQueryParams albumQueryParams)
            throws UserNotFoundException, JOOQException, BadQueryParametersException {

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            if (!userExist(albumQueryParams.getDBID(), em)) {
                throw new UserNotFoundException();
            }
        } finally {
            em.close();
        }
        return findAlbumsByUserPk(albumQueryParams);
    }

    public static void deleteAlbum(User callingUser, String albumId)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);

            if (album.getPk() == callingUser.getInbox().getPk()) {
                throw new AlbumNotFoundException();
            }

            for (AlbumUser albumUser:album.getAlbumUser()) {
                em.remove(albumUser);
            }

            for (Event event:album.getEvents()) {
                em.remove(event);
            }

            for (Capability capability:album.getCapabilities()) {
                capability.setRevoked(true);
                em.remove(capability);
            }

            for (AlbumSeries albumSeries:album.getAlbumSeries()) {
                em.remove(albumSeries);
            }

            em.remove(album);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static AlbumResponse getAlbum(long callingUserPk, String albumId, boolean withUserAccess, boolean withUsersList)
           throws JOOQException {
        AlbumResponse albumResponse;
        if (withUserAccess) {
            albumResponse = findAlbumByUserPkAndAlbumId(albumId, callingUserPk);
            if(withUsersList) {
                albumResponse.addUsersList();
            }
            return albumResponse;
        } else {
            return findAlbumByAlbumId(albumId);
        }
    }

    public static AlbumResponse getUsers(String albumId)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final AlbumResponseBuilder albumResponseBuilder = new AlbumResponseBuilder();
        try {
            final Album album = getAlbum(albumId, em);

            for (AlbumUser albumUser : album.getAlbumUser()) {
                albumResponseBuilder.addUser(albumUser);
            }
        } finally {
            em.close();
        }
        return albumResponseBuilder.build();
    }

    public static void addUser(User callingUser, String userName,  String albumId, boolean isAdmin)
            throws AlbumNotFoundException, AlbumForbiddenException, UserNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final User targetUser = em.merge(getOrCreateUser(userName));

            if (targetUser.getPk() == callingUser.getPk()) {
                throw new AlbumForbiddenException("Add yourself forbidden");
            }

            final Album album = getAlbum(albumId, em);

            if (isMemberOfAlbum(targetUser, album, em)) {
                try {
                    final AlbumUser targetAlbumUser = getAlbumUser(album, targetUser, em);
                    if (! targetAlbumUser.isAdmin() && isAdmin) {
                        //Here, the targetUser is an normal member and he will be promot admin
                        targetAlbumUser.setAdmin(isAdmin);
                        final Mutation mutationPromoteAdmin = Events.albumPostUserMutation(callingUser, album, Events.MutationType.PROMOTE_ADMIN, targetUser);
                        em.persist(mutationPromoteAdmin);
                    }

                } catch (UserNotMemberException e) {
                    throw new AlbumNotFoundException("Album not found");//normally, this exception should never happen
                }
            } else {
                final AlbumUser targetAlbumUser = new AlbumUser(album, targetUser, isAdmin);

                final Events.MutationType mutationType;
                if (isAdmin) {
                    mutationType = Events.MutationType.ADD_ADMIN;
                } else {
                    mutationType = Events.MutationType.ADD_USER;
                }

                final Mutation mutation = Events.albumPostUserMutation(callingUser, album, mutationType, targetUser);
                em.persist(mutation);
                em.persist(targetAlbumUser);
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteUser(User callingUser, String userName,  String albumId)
            throws UserNotFoundException, AlbumNotFoundException, UserNotMemberException, AlbumForbiddenException{

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final User removedUser = em.merge(getOrCreateUser(userName));
            final Album album = getAlbum(albumId, em);
            final AlbumUser callingAlbumUser = getAlbumUser(album, callingUser, em);
            final AlbumUser removedAlbumUser = getAlbumUser(album, removedUser, em);

            if (removedAlbumUser.isAdmin()) {
                for (Capability capability: album.getCapabilities()) {
                    if (capability.getUser() == removedUser) {
                        capability.setRevoked(true);
                    }
                }
            }

            //Delete the album if it was the last User
            if (album.getAlbumUser().size() == 1) {
                deleteAlbum(callingUser, albumId);
            } else {
                final Events.MutationType mutationType;

                if (callingUser.getPk() == removedUser.getPk()) {
                    mutationType = Events.MutationType.LEAVE_ALBUM;
                } else if (callingAlbumUser.isAdmin()){
                    mutationType = Events.MutationType.REMOVE_USER;
                } else {
                    throw new AlbumForbiddenException("You must be an admin for removing another user");
                }

                final Mutation mutation = Events.albumPostUserMutation(callingUser, album, mutationType, removedUser);
                em.persist(mutation);
                em.remove(removedAlbumUser);
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void removeAdmin(User callingUser, String userName,  String albumId)
            throws UserNotFoundException, AlbumNotFoundException, UserNotMemberException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final User removedUser = em.merge(getOrCreateUser(userName));
            final Album targetAlbum = getAlbum(albumId, em);
            final AlbumUser removedAlbumUser = getAlbumUser(targetAlbum, removedUser, em);

            final Mutation mutation = Events.albumPostUserMutation(callingUser, targetAlbum, Events.MutationType.DEMOTE_ADMIN, removedUser);

            em.persist(mutation);
            removedAlbumUser.setAdmin(false);

            for (Capability capability: targetAlbum.getCapabilities()) {
                if (capability.getUser() == removedUser) {
                    capability.setRevoked(true);
                }
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void setFavorites(User callingUser,  String albumId, Boolean favorite)
            throws AlbumNotFoundException, UserNotMemberException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);
            final AlbumUser albumUser = getAlbumUser(album, callingUser, em);

            albumUser.setFavorite(favorite);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static Album getAlbum(String albumId, EntityManager em)
            throws AlbumNotFoundException {

            return findAlbumById(albumId, em);
    }

    public static AlbumUser getAlbumUser(Album album, User user, EntityManager em)
            throws UserNotMemberException {

            return findAlbumUserByUserAndAlbum(user, album, em);
    }

    public static boolean albumExist(String albumId, EntityManager em) {
        try {
            findAlbumById(albumId, em);
        } catch (AlbumNotFoundException e) {
            return false;
        }
        return true;
    }

    public static boolean albumExist(String albumId) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            findAlbumById(albumId, em);
        } catch (AlbumNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
        return true;
    }

    public static boolean isMemberOfAlbum(User user, Album album, EntityManager em) {
        try {
            findAlbumUserByUserAndAlbum(user, album, em);
        } catch (UserNotMemberException e) {
            return false;
        }
        return true;
    }
}
