package online.kheops.auth_server.event;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.study.Studies;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.isMemberOfAlbum;
import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;

public class Events {

    private Events() {
        throw new IllegalStateException("Utility class");
    }

    public enum MutationType {ADD_USER, ADD_ADMIN, REMOVE_USER, PROMOTE_ADMIN, DEMOTE_ADMIN,
        CREATE_ALBUM, LEAVE_ALBUM,
        IMPORT_STUDY, IMPORT_SERIES, REMOVE_STUDY, REMOVE_SERIES,
        EDIT_ALBUM, ADD_FAV, REMOVE_FAV,
        CREATE_REPORT_PROVIDER, EDIT_REPORT_PROVIDER, DELETE_REPORT_PROVIDER, NEW_REPORT,
        CREATE_WEBHOOK, DELETE_WEBHOOK}

    public static void albumPostComment(User callingUser, String albumId, String commentContent, String user)
            throws UserNotFoundException, AlbumNotFoundException {

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

                if (targetUser != callingUser && !isMemberOfAlbum(targetUser, album, em)) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message("Not a member")
                            .detail("Target user is not a member of the album ")
                            .build();
                    throw new UserNotFoundException(errorResponse);
                }

                comment.setPrivateTargetUser(targetUser);
                targetUser.getComments().add(comment);
            } else {
                album.updateLastEventTime();
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

    public static Mutation albumPostMutation(User callingUser, Album album, MutationType mutation) {
        return new Mutation(callingUser, album, mutation);
    }

    public static PairListXTotalCount<EventResponse> getEventsAlbum(User callingUser, String albumId, Integer offset, Integer limit)
            throws AlbumNotFoundException {

        final List<EventResponse> eventResponses = new ArrayList<>();
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);

            final HashMap<String, Boolean> userMember = new HashMap<>();

            for (Event e : EventQueries.getEventsByAlbum(callingUser, album, offset, limit, em)) {
                if (!userMember.containsKey(e.getUser().getKeycloakId())) {
                    userMember.put(e.getUser().getKeycloakId(), isMemberOfAlbum(e.getUser(), album, em));
                }
                if (e instanceof Comment) {
                    if (e.getPrivateTargetUser() != null && !userMember.containsKey(e.getPrivateTargetUser().getKeycloakId())) {
                        userMember.put(e.getPrivateTargetUser().getKeycloakId(), isMemberOfAlbum(e.getPrivateTargetUser(), album, em));
                    }
                    eventResponses.add(new EventResponse((Comment)e, userMember));
                } else if (e instanceof Mutation) {
                    if (((Mutation) e).getToUser() != null && !userMember.containsKey(((Mutation) e).getToUser().getKeycloakId())) {
                        userMember.put(((Mutation) e).getToUser().getKeycloakId(), isMemberOfAlbum(((Mutation) e).getToUser(), album, em));
                    }
                    eventResponses.add(new EventResponse((Mutation) e, userMember));
                }
            }

            XTotalCount = EventQueries.getTotalEventsByAlbum(callingUser, album, em);
        } finally {
            em.close();
        }
        return new PairListXTotalCount<>(XTotalCount, eventResponses);
    }

    public static PairListXTotalCount<EventResponse> getMutationsAlbum(String albumId, Integer offset, Integer limit)
            throws AlbumNotFoundException {

        final List<EventResponse> eventResponses = new ArrayList<>();
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            final Album album = getAlbum(albumId, em);
            final HashMap<String, Boolean> userMember = new HashMap<>();

            for (Mutation m : EventQueries.getMutationByAlbum(album, offset, limit, em)) {
                if (!userMember.containsKey(m.getUser().getKeycloakId())) {
                    userMember.put(m.getUser().getKeycloakId(), isMemberOfAlbum(m.getUser(), album, em));
                }
                if (m.getToUser() != null && !userMember.containsKey(m.getToUser().getKeycloakId())) {
                    userMember.put(m.getToUser().getKeycloakId(), isMemberOfAlbum(m.getToUser(), album, em));
                }
                    eventResponses.add(new EventResponse(m, userMember));
            }

            XTotalCount = EventQueries.getTotalMutationByAlbum(album, em);
        } finally {
            em.close();
        }
        return new PairListXTotalCount<>(XTotalCount, eventResponses);
    }

    public static PairListXTotalCount<EventResponse> getCommentsAlbum(User callingUser, String albumId, Integer offset, Integer limit)
            throws AlbumNotFoundException {

        final List<EventResponse> eventResponses = new ArrayList<>();
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final HashMap<String, Boolean> userMember = new HashMap<>();

        try {
            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);

            for (Comment c : EventQueries.getCommentByAlbum(callingUser, album, offset, limit, em)) {
                if (!userMember.containsKey(c.getUser().getKeycloakId())) {
                    userMember.put(c.getUser().getKeycloakId(), isMemberOfAlbum(c.getUser(), album, em));
                }
                if (c.getPrivateTargetUser() != null && !userMember.containsKey(c.getPrivateTargetUser().getKeycloakId())) {
                    userMember.put(c.getPrivateTargetUser().getKeycloakId(), isMemberOfAlbum(c.getPrivateTargetUser(), album, em));
                }
                eventResponses.add(new EventResponse(c, userMember));
            }
            XTotalCount = EventQueries.getTotalCommentsByAlbum(callingUser, album, em);
        } finally {
            em.close();
        }
        return new PairListXTotalCount<>(XTotalCount, eventResponses);
    }

    public static PairListXTotalCount<EventResponse> getCommentsByStudyUID(User callingUser, String studyInstanceUID, Integer offset, Integer limit) {

        final List<EventResponse> eventResponses = new ArrayList<>();
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final HashMap<String, Boolean> userMember = new HashMap<>();

        try {
            callingUser = em.merge(callingUser);

            for (Comment c : EventQueries.getCommentsByStudy(callingUser, studyInstanceUID, offset, limit, em)) {
                if (!userMember.containsKey(c.getUser().getKeycloakId())) {
                    userMember.put(c.getUser().getKeycloakId(), canAccessStudy(c.getUser(), c.getStudy(), em));
                }
                if (c.getPrivateTargetUser() != null && !userMember.containsKey(c.getPrivateTargetUser().getKeycloakId())) {
                    userMember.put(c.getPrivateTargetUser().getKeycloakId(), canAccessStudy(c.getPrivateTargetUser(), c.getStudy(), em));
                }
                eventResponses.add(new EventResponse(c, userMember));
            }
            XTotalCount = EventQueries.getTotalCommentsByStudy(callingUser, studyInstanceUID, em);
        } finally {
            em.close();
        }
        return new PairListXTotalCount<>(XTotalCount, eventResponses);
    }


    public static void studyPostComment(User callingUser, String studyInstanceUID, String commentContent, String targetUserPk)
            throws UserNotFoundException, StudyNotFoundException, BadQueryParametersException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        if (commentContent.isEmpty()) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("'comment' is empty")
                    .build();
            throw new BadQueryParametersException(errorResponse);
        }

        try {
            tx.begin();

            final boolean isPrivateComment = targetUserPk != null;

            callingUser = em.merge(callingUser);
            final Study study = getStudy(studyInstanceUID, em);

            final Comment comment = new Comment(commentContent, callingUser, study);

            if(isPrivateComment) {
                User targetUser = em.merge(getOrCreateUser(targetUserPk));

                if (targetUser != callingUser && !Studies.canAccessStudy(targetUser, study, em)) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message("No access")
                            .detail("Target user can't access to this study")
                            .build();
                    throw new UserNotFoundException(errorResponse);
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
