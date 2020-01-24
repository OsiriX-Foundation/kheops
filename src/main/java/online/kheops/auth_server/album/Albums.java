package online.kheops.auth_server.album;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponse;
import online.kheops.auth_server.user.UsersPermission;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.PairListXTotalCount;
import online.kheops.auth_server.webhook.NewUserWebhook;
import online.kheops.auth_server.webhook.WebhookAsyncRequest;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static online.kheops.auth_server.album.AlbumQueries.*;
import static online.kheops.auth_server.user.UserQueries.findUserByUserId;
import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;
import static online.kheops.auth_server.util.ErrorResponse.Message.AUTHORIZATION_ERROR;

public class Albums {

    private Albums() {
        throw new IllegalStateException("Utility class");
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

            albumResponse = findAlbumByUserAndAlbumId(newAlbum.getId(), callingUser);
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
                editAlbum.updateLastEventTime();
                em.persist(mutation);

            } else if (name != null || description != null || usersPermission.areSet()) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(AUTHORIZATION_ERROR)
                        .detail("The user must be an admin for editing name, description or permissions")
                        .build();
                throw new AlbumForbiddenException(errorResponse);
            }

            tx.commit();

            albumResponse = findAlbumByUserAndAlbumId(editAlbum.getId(), callingUser);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return albumResponse;
    }

    public static PairListXTotalCount<AlbumResponse> getAlbumList(AlbumQueryParams albumQueryParams)
            throws JOOQException, BadQueryParametersException {

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

            for (ReportProvider reportProvider:album.getReportProviders()) {
                em.remove(reportProvider);
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

            for (Webhook webhook:album.getWebhooks()) {
                for (WebhookHistory webhookHistory:webhook.getWebhookHistory()) {
                    em.remove(webhookHistory);
                }
                em.remove(webhook);
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

    public static AlbumResponse getAlbum(User user, String albumId, boolean withUserAccess, boolean withUsersList)
           throws JOOQException, AlbumNotFoundException {
        AlbumResponse albumResponse;
        if (withUserAccess) {
            albumResponse = findAlbumByUserAndAlbumId(albumId, user);
            if(withUsersList) {
                albumResponse.setUsers(getUsers(albumId, Integer.MAX_VALUE, 0).getAttributesList());
            }
            return albumResponse;
        } else {
            return findAlbumByAlbumId(albumId);
        }
    }

    public static PairListXTotalCount<UserResponse> getUsers(String albumId, Integer limit , Integer offset)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final List<UserResponse> listUserAlbumResponse = new ArrayList<>();
        final long totalCount;
        try {
            final Album album = getAlbum(albumId, em);
            totalCount = album.getAlbumUser().size();

            int i = 0;
            for (AlbumUser albumUser : album.getAlbumUser()) {
                if (i++ >= offset) {
                    if (i > offset + limit) {
                        break;
                    } else {
                        listUserAlbumResponse.add(new UserResponse(albumUser));
                    }
                }
            }
        } finally {
            em.close();
        }
        Collections.sort(listUserAlbumResponse);
        return new PairListXTotalCount<>(totalCount, listUserAlbumResponse);
    }

    public static User addUser(User callingUser, String userName, String albumId, boolean isAdmin, ServletContext context)
            throws AlbumNotFoundException, AlbumForbiddenException, UserNotFoundException, UserNotMemberException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final User targetUser = em.merge(getOrCreateUser(userName));

            if (targetUser.getPk() == callingUser.getPk()) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(AUTHORIZATION_ERROR)
                        .detail("You can not add yourself to the album")
                        .build();
                throw new AlbumForbiddenException(errorResponse);
            }

            final Album album = getAlbum(albumId, em);

            if (isMemberOfAlbum(targetUser, album, em)) {
                try {
                    final AlbumUser targetAlbumUser = getAlbumUser(album, targetUser, em);
                    if (!targetAlbumUser.isAdmin() && isAdmin) {
                        //Here, the targetUser is an normal member and he will be promot as admin
                        targetAlbumUser.setAdmin(isAdmin);
                        final Mutation mutationPromoteAdmin = Events.albumPostUserMutation(callingUser, album, Events.MutationType.PROMOTE_ADMIN, targetUser);
                        em.persist(mutationPromoteAdmin);
                    }

                } catch (UserNotMemberException e) {
                    throw new AlbumNotFoundException();//normally, this exception should never happen
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

                final AlbumUser albumCallingUser = getAlbumUser(album, callingUser, em);
                final NewUserWebhook newUserWebhook = new NewUserWebhook(albumId, albumCallingUser, targetAlbumUser, context.getInitParameter(HOST_ROOT_PARAMETER),false);
                for (Webhook webhook : album.getWebhooks()) {
                    if (webhook.getNewUser() && webhook.isEnable()) {
                        new WebhookAsyncRequest(webhook, newUserWebhook, false);
                    }
                }
            }
            album.updateLastEventTime();
            tx.commit();
            return targetUser;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static User deleteUser(User callingUser, String userName,  String albumId)
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

            //Delete the album if it is the last User
            if (album.getAlbumUser().size() == 1) {
                deleteAlbum(callingUser, albumId);
            } else {

                if (removedAlbumUser.isAdmin()) {
                    for (Capability capability: album.getCapabilities()) {
                        if (capability.getUser() == removedUser) {
                            capability.setRevoked(true);
                        }
                    }
                }

                final Events.MutationType mutationType;

                if (callingUser.getPk() == removedUser.getPk()) {
                    mutationType = Events.MutationType.LEAVE_ALBUM;
                } else if (callingAlbumUser.isAdmin()){
                    mutationType = Events.MutationType.REMOVE_USER;
                } else {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(AUTHORIZATION_ERROR)
                            .detail("You must be an admin for removing another user")
                            .build();
                    throw new AlbumForbiddenException(errorResponse);
                }

                final Mutation mutation = Events.albumPostUserMutation(callingUser, album, mutationType, removedUser);
                em.persist(mutation);
                em.remove(removedAlbumUser);
                album.updateLastEventTime();
            }

            tx.commit();
            return removedUser;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static User removeAdmin(User callingUser, String userName,  String albumId)
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
            targetAlbum.updateLastEventTime();
            tx.commit();
            return removedUser;
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

    public static Album getAlbum(String albumId)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            return getAlbum(albumId, em);
        } finally {
            em.close();
        }
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

    public static boolean isMemberOfAlbum(String sub, String albumId) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        try {
            final Album album = findAlbumById(albumId, em);
            final User user = findUserByUserId(sub, em);
            return isMemberOfAlbum(user, album, em);
        } catch (AlbumNotFoundException | UserNotFoundException e ) {
            return false;
        } finally {
            em.close();
        }
    }
}
