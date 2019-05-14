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

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.isMemberOfAlbum;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.user.Users.getOrCreateUser;

public class Events {

    private Events() {
        throw new IllegalStateException("Utility class");
    }

    public enum MutationType {ADD_USER, ADD_ADMIN, REMOVE_USER, PROMOTE_ADMIN, DEMOTE_ADMIN,
        CREATE_ALBUM, LEAVE_ALBUM,
        IMPORT_STUDY, IMPORT_SERIES, REMOVE_STUDY, REMOVE_SERIES,
        EDIT_ALBUM, ADD_FAV, REMOVE_FAV,
        CREATE_REPORT_PROVIDER, EDIT_REPORT_PROVIDER, DELETE_REPORT_PROVIDER, NEW_REPORT}

    public static void albumPostComment(User callingUser, String albumId, String commentContent, String user)
            throws UserNotFoundException, AlbumNotFoundException, BadQueryParametersException{

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final boolean isPrivateComment = user != null;

            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);

            final Comment comment = new Comment(commentContent, callingUser, album);

            if(isPrivateComment) {
                final User targetUser = em.merge(getOrCreateUser(user));

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

    public static Mutation reportProviderMutation(User callingUser, Album album, ReportProvider reportProvider, Events.MutationType mutationType) {
        return new Mutation(callingUser, album, reportProvider, mutationType);
    }

    public static Mutation newReport(User callingUser, Album album, ReportProvider reportProvider, Events.MutationType mutationType, Series series) {
        return new Mutation(callingUser, album, reportProvider, mutationType, series);
    }

    public static Mutation albumPostNewAlbumMutation(User callingUser, Album album) {
        return new Mutation(callingUser, album, Events.MutationType.CREATE_ALBUM);
    }

    public static Mutation albumPostSeriesMutation(User callingUser, Album album, MutationType mutationType, Series series) {
        return new Mutation(callingUser, album, mutationType, series);
    }

    public static Mutation albumPostSeriesMutation(Capability capability, Album album, MutationType mutationType, Series series) {
        return new Mutation(capability, album, mutationType, series);
    }

    public static Mutation albumPostStudyMutation(User callingUser, Album album, MutationType mutationType, Study study) {
        return new Mutation(callingUser, album, mutationType, study);
    }

    public static Mutation albumPostStudyMutation(Capability capability, Album album, MutationType mutationType, Study study) {
        return new Mutation(capability, album, mutationType, study);
    }

    public static Mutation albumPostEditMutation(User callingUser, Album album) {
        return new Mutation(callingUser, album, MutationType.EDIT_ALBUM);
    }

    public static PairListXTotalCount<EventResponse> getEventsAlbum(User callingUser, String albumId, Integer offset, Integer limit)
            throws AlbumNotFoundException {

        final List<EventResponse> eventResponses = new ArrayList<>();
        final PairListXTotalCount<EventResponse> pair;
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);

            for (Event e : EventQueries.getEventsByAlbum(callingUser, album, offset, limit, em)) {
                if (e instanceof Comment) {
                    eventResponses.add(new EventResponse((Comment)e));
                } else if (e instanceof Mutation) {
                    eventResponses.add(new EventResponse((Mutation) e));
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

    public static PairListXTotalCount<EventResponse> getMutationsAlbum(String albumId, Integer offset, Integer limit)
            throws AlbumNotFoundException {

        final List<EventResponse> eventResponses = new ArrayList<>();
        final PairListXTotalCount<EventResponse> pair;
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final Album album = getAlbum(albumId, em);

            for (Mutation m : EventQueries.getMutationByAlbum(album, offset, limit, em)) {
                    eventResponses.add(new EventResponse(m));
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

    public static PairListXTotalCount<EventResponse> getCommentsAlbum(User callingUser, String albumId, Integer offset, Integer limit)
            throws AlbumNotFoundException {

        final List<EventResponse> eventResponses = new ArrayList<>();
        final PairListXTotalCount<EventResponse> pair;
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);

            for (Comment c : EventQueries.getCommentByAlbum(callingUser, album, offset, limit, em)) {
                eventResponses.add(new EventResponse(c));
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

    public static PairListXTotalCount<EventResponse>  getCommentsByStudyUID(User callingUser, String studyInstanceUID, Integer offset, Integer limit) {

        final List<EventResponse> eventResponses = new ArrayList<>();
        final PairListXTotalCount<EventResponse> pair;
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            for (Comment c : EventQueries.getCommentsByStudy(callingUser, studyInstanceUID, offset, limit, em)) {
                eventResponses.add(new EventResponse(c));
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


    public static void studyPostComment(User callingUser, String studyInstanceUID, String commentContent, String targetUserPk)
            throws UserNotFoundException, StudyNotFoundException, BadQueryParametersException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        if (commentContent.isEmpty()) {
            throw new BadQueryParametersException("Comment is empty");
        }

        try {
            tx.begin();

            final boolean isPrivateComment = targetUserPk != null;

            callingUser = em.merge(callingUser);
            final Study study = getStudy(studyInstanceUID, em);

            final Comment comment = new Comment(commentContent, callingUser, study);

            if(isPrivateComment) {
                User targetUser = em.merge(getOrCreateUser(targetUserPk));

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
