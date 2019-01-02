package online.kheops.auth_server.album;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.user.UsersPermission;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.ws.rs.core.MultivaluedMap;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import static online.kheops.auth_server.album.AlbumQueries.*;
import static online.kheops.auth_server.user.Users.getUser;
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

        while (idBuilder.length() < ID_LENGTH) {
            int index = rdm.nextInt(DICT.length());
            idBuilder.append(DICT.charAt(index));
        }
        return idBuilder.toString();
    }

    public static AlbumResponses.AlbumResponse createAlbum(User callingUser, String name, String description, UsersPermission usersPermission)
            throws JOOQException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final AlbumResponses.AlbumResponse albumResponse;

        try {
            tx.begin();

            final User mergedCallingUser = em.merge(callingUser);

            final Album newAlbum = new Album(name, description, usersPermission);
            final AlbumUser newAlbumUser = new AlbumUser(newAlbum, mergedCallingUser, true);
            final Mutation newAlbumMutation = Events.albumPostNewAlbumMutation(mergedCallingUser, newAlbum);

            em.persist(newAlbum);
            em.persist(newAlbumUser);
            em.persist(newAlbumMutation);

            tx.commit();

            albumResponse = findAlbumByUserPkAndAlbumId(newAlbum.getId(), mergedCallingUser.getPk());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return albumResponse;
    }

    public static AlbumResponses.AlbumResponse editAlbum(long callingUserPk, String albumId, String name, String description, UsersPermission usersPermission,
                                                         Boolean notificationNewComment , Boolean notificationNewSeries)
            throws AlbumNotFoundException, AlbumForbiddenException, UserNotFoundException, JOOQException, UserNotMemberException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final AlbumResponses.AlbumResponse albumResponse;

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
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

            } else if (name != null || description != null || usersPermission.areSet()) {
                throw new AlbumForbiddenException("Not admin: The user must be an admin for editing name, description or permission");
            }

            tx.commit();

            albumResponse = findAlbumByUserPkAndAlbumId(editAlbum.getId(), callingUserPk);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return albumResponse;
    }

    public static PairListXTotalCount<AlbumResponses.AlbumResponse> getAlbumList(long callingUserPk, MultivaluedMap<String, String> queryParameters)
            throws UserNotFoundException, JOOQException, BadQueryParametersException {

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            if (!userExist(callingUserPk, em)) {
                throw new UserNotFoundException();
            }
        } finally {
            em.close();
        }
        return findAlbumsByUserPk(callingUserPk, queryParameters);
    }

    public static void deleteAlbum(long callingUserPk, String albumId)
            throws AlbumNotFoundException, UserNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
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

    public static AlbumResponses.AlbumResponse getAlbum(long callingUserPk, String albumId, boolean withUserAccess)
           throws JOOQException {
        if (withUserAccess) {
            return findAlbumByUserPkAndAlbumId(albumId, callingUserPk);
        } else {
            return findAlbumByAlbumId(albumId);
        }
    }

    public static List<AlbumResponses.UserAlbumResponse> getUsers(long callingUserPk, String albumId)
            throws AlbumNotFoundException, UserNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final List<AlbumResponses.UserAlbumResponse> usersAlbumResponses = new ArrayList<>();

        try {
            if (!userExist(callingUserPk, em)) {
                throw new UserNotFoundException();
            }
            final Album album = getAlbum(albumId, em);

            for (AlbumUser albumUser : album.getAlbumUser()) {
                usersAlbumResponses.add(AlbumResponses.albumUserToUserAlbumResponce(albumUser));
            }
            Collections.sort(usersAlbumResponses);
        } finally {
            em.close();
        }
        return usersAlbumResponses;
    }

    public static void addUser(long callingUserPk, String userName, String albumId, boolean isAdmin)
            throws AlbumNotFoundException, AlbumForbiddenException, UserNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final User targetUser = getUser(userName, em);

            if (targetUser.getPk() == callingUser.getPk()) {
                throw new AlbumForbiddenException("Add yourself forbidden");
            }

            final Album album = getAlbum(albumId, em);

            if (isMemberOfAlbum(targetUser, album, em)) {
                try {
                    final AlbumUser targetAlbumUser = getAlbumUser(album, targetUser, em);
                    if (targetAlbumUser.isAdmin() == isAdmin) {
                        return; // the target is already a member of the album with the same profile (admin / non-admin)
                    }
                    if (targetAlbumUser.isAdmin() && !isAdmin) {
                        throw new AlbumForbiddenException("The user: " + targetUser.getGoogleEmail() + "is an admin. Use : DELETE /albums/" + albumId + "/users/" + callingUser.getGoogleId() + "/admin");
                    }
                    //From here, the targetUser is an normal member and he will be promot admin
                    targetAlbumUser.setAdmin(isAdmin);
                    final Mutation mutationPromoteAdmin = Events.albumPostUserMutation(callingUser, album, Events.MutationType.PROMOTE_ADMIN, targetUser);
                    em.persist(mutationPromoteAdmin);

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

    public static void deleteUser(long callingUserPk, String userName, String albumId)
            throws UserNotFoundException, AlbumNotFoundException, UserNotMemberException, AlbumForbiddenException{

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final User removedUser = getUser(userName, em);
            final Album album = getAlbum(albumId, em);
            final AlbumUser callingAlbumUser = getAlbumUser(album, callingUser, em);
            final AlbumUser removedAlbumUser = getAlbumUser(album, removedUser, em);

            final Events.MutationType mutationType;

            if (callingUser.getPk() == removedUser.getPk()) {
                mutationType = Events.MutationType.LEAVE_ALBUM;
            } else if (callingAlbumUser.isAdmin()){
                mutationType = Events.MutationType.REMOVE_USER;
            } else {
                throw new AlbumForbiddenException("You must be an admin for removing another user");
            }

            final Mutation mutation = Events.albumPostUserMutation(callingUser, album, mutationType, removedUser);

            if (removedAlbumUser.isAdmin()) {
                for (Capability capability: album.getCapabilities()) {
                    if (capability.getUser() == removedUser) {
                        capability.setRevoked(true);
                    }
                }
            }

            em.persist(mutation);
            em.remove(removedAlbumUser);

            //Delete the album if it was the last User
            if (album.getAlbumUser().size() == 1) {
                deleteAlbum(callingUserPk, albumId);
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void removeAdmin(long callingUserPk, String userName, String albumId)
            throws UserNotFoundException, AlbumNotFoundException, UserNotMemberException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final User removedUser = getUser(userName, em);
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

    public static void setFavorites(long callingUserPk, String albumId, Boolean favorite)
            throws UserNotFoundException, AlbumNotFoundException, UserNotMemberException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
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

        try {
            return findAlbumById(albumId, em);
        } catch (NoResultException e) {
            throw new AlbumNotFoundException(e);
        }
    }

    public static AlbumUser getAlbumUser(Album album, User user, EntityManager em)
            throws UserNotMemberException {

        try {
        return findAlbumUserByUserAndAlbum(user, album, em);
        } catch (NoResultException e) {
            throw new UserNotMemberException(e);
        }
    }

    public static boolean albumExist(String albumId, EntityManager em) {
        try {
            findAlbumById(albumId, em);
        } catch (NoResultException e) {
            return false;
        }
        return true;
    }

    public static boolean isMemberOfAlbum(User user, Album album, EntityManager em) {
        try {
            findAlbumUserByUserAndAlbum(user, album, em);
        } catch (NoResultException e) {
            return false;
        }
        return true;
    }
}
