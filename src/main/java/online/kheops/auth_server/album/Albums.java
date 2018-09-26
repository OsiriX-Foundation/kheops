package online.kheops.auth_server.album;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.generated.tables.AlbumSeries;
import online.kheops.auth_server.user.UsersPermission;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.user.UserNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static online.kheops.auth_server.album.AlbumQueries.*;
import static online.kheops.auth_server.user.Users.getUser;
import static online.kheops.auth_server.user.Users.userExist;

public class Albums {

    private Albums() {
        throw new IllegalStateException("Utility class");
    }

    public static AlbumResponses.AlbumResponse cerateAlbum(long callingUserPk, String name, String description, UsersPermission usersPermission)
            throws UserNotFoundException, JOOQException {

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        AlbumResponses.AlbumResponse albumResponse;

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);

            final Album album = new Album(name, description, usersPermission);
            final AlbumUser albumUser = new AlbumUser(album, callingUser, true);
            final Mutation newAlbumMutation = Events.albumPostNewAlbumMutation(callingUser, album);

            em.persist(callingUser);
            em.persist(album);
            em.persist(albumUser);
            em.persist(newAlbumMutation);

            tx.commit();
            albumResponse = findAlbumByUserPkAndAlbumPk(album.getPk(), callingUserPk);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return albumResponse;
    }

    public static AlbumResponses.AlbumResponse editAlbum(long callingUserPk, long albumPk, String name, String description, UsersPermission usersPermission)
            throws AlbumNotFoundException, AlbumForbiddenException, UserNotFoundException, JOOQException{
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        AlbumResponses.AlbumResponse albumResponse;

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album album = getAlbum(albumPk, em);
            final AlbumUser targetAlbumUser = getAlbumUser(album, callingUser, em);

            if(!targetAlbumUser.isAdmin()) {
                throw new AlbumForbiddenException("Not admin");
            }

            if (name != null) { album.setName(name); }
            if (description != null) { album.setDescription(description); }

            usersPermission.getAddUser().ifPresent(album::setAddUser);
            usersPermission.getDownloadSeries().ifPresent(album::setDownloadSeries);
            usersPermission.getSendSeries().ifPresent(album::setSendSeries);
            usersPermission.getDeleteSeries().ifPresent(album::setDeleteSeries);
            usersPermission.getAddSeries().ifPresent(album::setAddSeries);
            usersPermission.getWriteComments().ifPresent(album::setWriteComments);

            em.persist(album);

            tx.commit();

            albumResponse = findAlbumByUserPkAndAlbumPk(album.getPk(), callingUserPk);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return albumResponse;
    }

    public static PairAlbumsTotalAlbum getAlbumList(long callingUserPk, MultivaluedMap<String, String> queryParameters)
            throws UserNotFoundException, JOOQException, BadQueryParametersException {

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        PairAlbumsTotalAlbum pairAlbumsTotalAlbum;

        try {
            tx.begin();

            if (!userExist(callingUserPk, em)) {
                throw new UserNotFoundException();
            }

            pairAlbumsTotalAlbum = findAlbumsByUserPk(callingUserPk, queryParameters);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return pairAlbumsTotalAlbum;
    }

    public static void deleteAlbum(long callingUserPk, long albumPk) throws AlbumNotFoundException, AlbumForbiddenException, UserNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);

            final Album album = getAlbum(albumPk, em);

            if (album.getPk() == callingUser.getInbox().getPk()) {
                throw new AlbumNotFoundException();
            }

            final AlbumUser callingAlbumUser = getAlbumUser(album, callingUser, em);
            if ( !callingAlbumUser.isAdmin()) {
                throw new AlbumForbiddenException("Only an admin can delete an album");
            }

            for (AlbumUser albumUser:album.getAlbumUser()) {
                em.remove(albumUser);
            }

            for (Event event:album.getEvents()) {
                em.remove(event);
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

    public static AlbumResponses.AlbumResponse getAlbum(long callingUserPk, long albumPk)
           throws AlbumNotFoundException, UserNotFoundException, JOOQException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        AlbumResponses.AlbumResponse albumResponse;

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            Album album = getAlbum(albumPk, em);

            if (!isMemberOfAlbum(callingUser, album, em)) {
                throw new AlbumNotFoundException();
            }

            albumResponse = findAlbumByUserPkAndAlbumPk(albumPk, callingUserPk);

            if (Long.parseLong(albumResponse.id) == callingUser.getInbox().getPk()) {
                throw new AlbumNotFoundException();
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return albumResponse;
    }

    public static List<AlbumResponses.UserAlbumResponse> getUsers(long callingUserPk, long albumPk)
            throws AlbumNotFoundException, AlbumForbiddenException, UserNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<AlbumResponses.UserAlbumResponse> usersAlbumResponses = new ArrayList<>();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album album = getAlbum(albumPk, em);

            if (album.getPk() == callingUser.getInbox().getPk()) {
                throw new AlbumNotFoundException();
            }

            final AlbumUser callingAlbumUser = getAlbumUser(album, callingUser, em);
            if ( !callingAlbumUser.isAdmin() && !album.isAddUser() ) {
                throw new AlbumForbiddenException("Only an admin or a user with permission can get users list");
            }

            for (AlbumUser albumUser : album.getAlbumUser()) {
                usersAlbumResponses.add(AlbumResponses.albumUserToUserAlbumResponce(albumUser));
            }
            Collections.<AlbumResponses.UserAlbumResponse>sort(usersAlbumResponses);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return usersAlbumResponses;
    }

    public static void addUser(long callingUserPk, String userName, long albumPk, Boolean isAdmin)
            throws AlbumNotFoundException, AlbumForbiddenException, UserNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final User targetUser = getUser(userName, em);

            if (targetUser.getPk() == callingUser.getPk()) {
                throw new AlbumForbiddenException("Add yourself forbidden");
            }

            final Album album = getAlbum(albumPk, em);

            if (album.getPk() == callingUser.getInbox().getPk()) {
                throw new AlbumNotFoundException();
            }

            final AlbumUser callingAlbumUser = getAlbumUser(album, callingUser, em);
            if ( !callingAlbumUser.isAdmin() && !album.isAddUser() ) {
                throw new AlbumForbiddenException("Only an admin or a user with permission can add an user");
            }
            if ( !callingAlbumUser.isAdmin() && isAdmin) {
                throw new AlbumForbiddenException("Only an admin can add an admin");
            }

            AlbumUser targetAlbumUser;
            try {
                targetAlbumUser = findAlbumUserByUserAndAlbum(targetUser, album, em);
                if (targetAlbumUser.isAdmin() == isAdmin) {
                    return;
                }
                if (targetAlbumUser.isAdmin() && !isAdmin) {
                    throw new AlbumForbiddenException("Use : DELETE /albums/"+albumPk+"/users/"+callingUser.getGoogleId()+"/admin");
                }
                targetAlbumUser.setAdmin(isAdmin);
                final Mutation mutationPromoteAdmin = Events.albumPostUserMutation(callingUser, album, Events.MutationType.PROMOTE_ADMIN, targetUser);
                em.persist(mutationPromoteAdmin);

            } catch (NoResultException e) {
                targetAlbumUser = new AlbumUser(album, targetUser, isAdmin);
                final Mutation mutation;
                if (isAdmin) {
                    mutation = Events.albumPostUserMutation(callingUser, album, Events.MutationType.ADD_ADMIN, targetUser);
                } else {
                    mutation = Events.albumPostUserMutation(callingUser, album, Events.MutationType.ADD_USER, targetUser);
                }
                em.persist(mutation);
            }

            em.persist(album);
            em.persist(targetUser);
            em.persist(targetAlbumUser);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteUser(long callingUserPk, String userName, long albumPk)
            throws UserNotFoundException, AlbumNotFoundException, AlbumForbiddenException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final User removedUser = getUser(userName, em);

            final Album album = getAlbum(albumPk, em);

            if (album.getPk() == callingUser.getInbox().getPk()) {
                throw new AlbumNotFoundException();
            }

            if (callingUser.getPk() == removedUser.getPk()) {
                final AlbumUser removedAlbumUser = getAlbumUser(album, removedUser, em);
                final Mutation mutation = Events.albumPostUserMutation(callingUser, album, Events.MutationType.LEAVE_ALBUM, callingUser);

                em.persist(callingUser);
                em.persist(mutation);
                em.persist(album);

                em.remove(removedAlbumUser);
            } else {

                final AlbumUser callingAlbumUser = getAlbumUser(album, callingUser, em);
                final AlbumUser removedAlbumUser;
                try {
                    removedAlbumUser = getAlbumUser(album, removedUser, em);
                } catch (AlbumNotFoundException exception) {
                    throw new UserNotFoundException();
                }

                if (!callingAlbumUser.isAdmin()) {
                    throw new AlbumForbiddenException("Only an admin can remove an user");
                }
                final Mutation mutation = Events.albumPostUserMutation(callingUser, album, Events.MutationType.REMOVE_USER, removedUser);

                em.persist(callingUser);
                em.persist(removedUser);
                em.persist(mutation);
                em.persist(album);

                em.remove(removedAlbumUser);
            }

            //Delete the album if it's the last User
            if (album.getAlbumUser().size() == 1) {
                for (Event event:album.getEvents()) {
                    em.remove(event);
                }
                em.remove(album);
            }

            tx.commit();

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void removeAdmin(long callingUserPk, String userName, long albumPk)
            throws UserNotFoundException, AlbumNotFoundException , AlbumForbiddenException{
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final User removedUser = getUser(userName, em);
            final Album targetAlbum = getAlbum(albumPk, em);

            final AlbumUser callingAlbumUser = getAlbumUser(targetAlbum, callingUser, em);
            if (!callingAlbumUser.isAdmin()) {
                throw new AlbumForbiddenException("Only an admin can remove the admin permission");
            }

            final AlbumUser removedAlbumUser;
            try {
                removedAlbumUser = getAlbumUser(targetAlbum, removedUser, em);
            } catch (AlbumNotFoundException exception) {
                throw new UserNotFoundException();
            }

            final Mutation mutation = Events.albumPostUserMutation(callingUser, targetAlbum, Events.MutationType.DEMOTE_ADMIN, removedUser);

            em.persist(callingUser);
            em.persist(mutation);
            em.persist(targetAlbum);
            em.persist(removedUser);

            em.remove(removedAlbumUser);

            removedAlbumUser.setAdmin(false);
            em.persist(removedAlbumUser);

            tx.commit();

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void setFavorites(long callingUserPk, long albumPk, Boolean favorite)
    throws UserNotFoundException, AlbumNotFoundException{
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        AlbumResponses.UserAlbumResponse userAlbumResponses;

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album album = getAlbum(albumPk, em);
            final AlbumUser albumUser = getAlbumUser(album, callingUser, em);

            if (album.getPk() == callingUser.getInbox().getPk()) {
                throw new AlbumNotFoundException();
            }

            albumUser.setFavorite(favorite);
            em.persist(albumUser);

            tx.commit();

            userAlbumResponses = AlbumResponses.albumUserToUserAlbumResponce(albumUser);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static Album getAlbum(long albumPk, EntityManager em) throws AlbumNotFoundException{
        try {
            return findAlbumByPk(albumPk, em);
        } catch (NoResultException e) {
            throw new AlbumNotFoundException(e);
        }
    }

    public static AlbumUser getAlbumUser(Album album, User user, EntityManager em) throws AlbumNotFoundException{
        try {
        return findAlbumUserByUserAndAlbum(user, album, em);
        } catch (NoResultException e) {
            throw new AlbumNotFoundException(e);
        }
    }

    public static boolean albumExist(long albumPk, EntityManager em) {
        try {
            findAlbumByPk(albumPk, em);
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
