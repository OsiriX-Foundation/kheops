package online.kheops.auth_server.event;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.study.Studies;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static online.kheops.auth_server.album.Albums.isMemberOfAlbum;
import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.user.Users.getUser;

public class Events {

    private Events() {
        throw new IllegalStateException("Utility class");
    }

    public enum MutationType {ADD_USER, ADD_ADMIN, REMOVE_USER, PROMOTE_ADMIN, DEMOTE_ADMIN, CREATE_ALBUM, LEAVE_ALBUM, IMPORT_STUDY, IMPORT_SERIES, REMOVE_STUDY, REMOVE_SERIES, EDIT_ALBUM, ADD_FAV, REMOVE_FAV}

    public static void albumPostComment(long callingUserPk,String albumId,String commentContent, String user)
            throws UserNotFoundException, AlbumNotFoundException, BadQueryParametersException{

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final boolean isPrivateComment = user != null;

            final User callingUser = getUser(callingUserPk, em);
            final Album album = getAlbum(albumId, em);

            final Comment comment = new Comment(commentContent, callingUser, album);

            if(isPrivateComment) {
                final User targetUser = getUser(user, em);

                if (targetUser == callingUser) {
                    throw new BadQueryParametersException("Self comment forbidden");
                }

                if (!isMemberOfAlbum(targetUser, album, em)) {
                    throw new UserNotFoundException("Target user is not a member of the album : " + albumId);
                }

                comment.setPrivateTargetUser(targetUser);
                targetUser.getComments().add(comment);
            } else {
                album.setLastEventTime(LocalDateTime.now(ZoneOffset.UTC));
            }

            em.persist(comment);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static Mutation albumPostUserMutation(User callingUser, Album album, MutationType mutationType, User targetUser) {
        return new Mutation(callingUser, album, mutationType, targetUser);
    }

    public static Mutation albumPostNewAlbumMutation(User callingUser, Album album) {
        return new Mutation(callingUser, album, Events.MutationType.CREATE_ALBUM);
    }

    public static Mutation albumPostSeriesMutation(User callingUser, Album album, MutationType mutationType, Series series) {
        return new Mutation(callingUser, album, mutationType, series);
    }

    public static Mutation albumPostStudyMutation(User callingUser, Album album, MutationType mutationType, Study study) {
        return new Mutation(callingUser, album, mutationType, study);
    }

    public static Mutation albumPostEditMutation(User callingUser, Album album) {
        return new Mutation(callingUser, album, MutationType.EDIT_ALBUM);
    }

    public static PairListXTotalCount<EventResponse.Response> getEventsAlbum(long callingUserPk, String albumId, Integer offset, Integer limit)
            throws UserNotFoundException, AlbumNotFoundException {

        final List<EventResponse.Response> eventResponses = new ArrayList<>();
        final PairListXTotalCount<EventResponse.Response> pair;
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album album = getAlbum(albumId, em);

            for (Event e : EventQueries.getEventsByAlbum(callingUser, album, offset, limit, em)) {
                if (e instanceof Comment) {
                    eventResponses.add(new EventResponse((Comment)e).getResponse());
                } else if (e instanceof Mutation) {
                    eventResponses.add(new EventResponse((Mutation) e).getResponse());
                }
            }

            XTotalCount = EventQueries.getTotalEventsByAlbum(callingUser, album, em);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        pair = new PairListXTotalCount<>(XTotalCount, eventResponses);
        return pair;
    }

    public static PairListXTotalCount<EventResponse.Response> getMutationsAlbum(String albumId, Integer offset, Integer limit)
            throws AlbumNotFoundException {

        final List<EventResponse.Response> eventResponses = new ArrayList<>();
        final PairListXTotalCount<EventResponse.Response> pair;
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final Album album = getAlbum(albumId, em);

            for (Mutation m : EventQueries.getMutationByAlbum(album, offset, limit, em)) {
                    eventResponses.add(new EventResponse(m).getResponse());
            }

            XTotalCount = EventQueries.getTotalMutationByAlbum(album, em);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        pair = new PairListXTotalCount<>(XTotalCount, eventResponses);
        return pair;
    }

    public static PairListXTotalCount<EventResponse.Response> getCommentsAlbum(long callingUserPk, String albumId, Integer offset, Integer limit)
            throws UserNotFoundException, AlbumNotFoundException {

        final List<EventResponse.Response> eventResponses = new ArrayList<>();
        final PairListXTotalCount<EventResponse.Response> pair;
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album album = getAlbum(albumId, em);

            for (Comment c : EventQueries.getCommentByAlbum(callingUser, album, offset, limit, em)) {
                eventResponses.add(new EventResponse(c).getResponse());
            }
            XTotalCount = EventQueries.getTotalCommentsByAlbum(callingUser, album, em);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        pair = new PairListXTotalCount<>(XTotalCount, eventResponses);
        return pair;
    }

    public static PairListXTotalCount<EventResponse.Response> getCommentsByStudyUID(long callingUserPk, String studyInstanceUID, Integer offset, Integer limit)
            throws UserNotFoundException {

        final List<EventResponse.Response> eventResponses = new ArrayList<>();
        final PairListXTotalCount<EventResponse.Response> pair;
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);

            for (Comment c : EventQueries.getCommentsByStudy(callingUser, studyInstanceUID, offset, limit, em)) {
                eventResponses.add(new EventResponse(c).getResponse());
            }
            XTotalCount = EventQueries.getTotalCommentsByStudy(callingUser, studyInstanceUID, em);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        pair = new PairListXTotalCount<>(XTotalCount, eventResponses);
        return pair;
    }


    public static void studyPostComment(long callingUserPk, String studyInstanceUID, String commentContent, String targetUserPk)
            throws UserNotFoundException, StudyNotFoundException, BadQueryParametersException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        if (commentContent.isEmpty()) {
            throw new BadQueryParametersException("Comment is empty");
        }

        try {
            tx.begin();

            final boolean isPrivateComment = targetUserPk != null;

            final User callingUser = getUser(callingUserPk, em);
            final Study study = getStudy(studyInstanceUID, em);

            final Comment comment = new Comment(commentContent, callingUser, study);

            if(isPrivateComment) {
                User targetUser = getUser(targetUserPk, em);

                if (targetUser == callingUser) {
                    throw new BadQueryParametersException("Self comment forbidden");
                }

                if (!Studies.canAccessStudy(targetUser, study, em)) {
                    throw new UserNotFoundException("Target user can't access to this study : " + studyInstanceUID);
                }

                comment.setPrivateTargetUser(targetUser);
                targetUser.getComments().add(comment);
            }

            em.persist(comment);

            tx.commit();

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}
