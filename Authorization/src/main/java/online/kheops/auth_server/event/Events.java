package online.kheops.auth_server.event;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.study.Studies;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.isMemberOfAlbum;
import static online.kheops.auth_server.event.EventQueries.getMutationByAlbum;
import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.user.Users.getUser;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;

public class Events {

    private Events() {
        throw new IllegalStateException("Utility class");
    }

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
                final User targetUser = getUser(user, em);

                if (targetUser != callingUser && !isMemberOfAlbum(targetUser, album, em)) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message("Not a member")
                            .detail("Target user is not a member of the album ")
                            .build();
                    throw new UserNotFoundException(errorResponse);
                }

                comment.setPrivateTargetUser(targetUser);
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

    public static Mutation reportProviderMutation(User callingUser, Album album, ReportProvider reportProvider, MutationType mutationType) {
        return new Mutation(callingUser, album, reportProvider, mutationType);
    }

    public static Mutation newReport(User callingUser, Album album, ReportProvider reportProvider, MutationType mutationType, Series series) {
        return new Mutation(callingUser, album, reportProvider, mutationType, series);
    }

    public static Mutation albumPostNewAlbumMutation(User callingUser, Album album) {
        return new Mutation(callingUser, album, MutationType.CREATE_ALBUM);
    }

    public static Mutation albumPostSeriesMutation(User callingUser, Album album, MutationType mutationType, Series series) {
        return new Mutation(callingUser, album, mutationType, series);
    }

    public static Mutation albumPostSeriesMutation(Capability capability, Album album, MutationType mutationType, Series series) {
        return new Mutation(capability, album, mutationType, series);
    }

    public static Mutation albumPostStudyMutation(User callingUser, Album album, MutationType mutationType, Study study, List<Series> seriesList) {
        return new Mutation(callingUser, album, mutationType, study, seriesList);
    }

    public static Mutation albumPostStudyMutation(Capability capability, Album album, MutationType mutationType, Study study, List<Series> seriesList) {
        return new Mutation(capability, album, mutationType, study, seriesList);
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
            for(AlbumUser albumUser : album.getAlbumUser()) {
                userMember.put(albumUser.getUser().getSub(), albumUser.isAdmin());
            }

            for (Event e : EventQueries.getEventsByAlbum(callingUser, album, offset, limit, em)) {
                eventResponses.add(new EventResponse(e, userMember, em));
            }

            XTotalCount = EventQueries.getTotalEventsByAlbum(callingUser, album, em);
        } finally {
            em.close();
        }
        return new PairListXTotalCount<>(XTotalCount, eventResponses);
    }

    public static PairListXTotalCount<EventResponse> getMutationsAlbum(String albumId, MultivaluedMap<String, String> queryParameters, Integer offset, Integer limit)
            throws AlbumNotFoundException, BadQueryParametersException {

        final List<EventResponse> eventResponses = new ArrayList<>();
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            MutationQueryParams mutationQueryParams = new MutationQueryParams(queryParameters, albumId, em);

            final Album album = getAlbum(albumId, em);
            final HashMap<String, Boolean> userMember = new HashMap<>();

            for (AlbumUser albumUser : album.getAlbumUser()) {
                userMember.put(albumUser.getUser().getSub(), albumUser.isAdmin());
            }

            final List<Mutation> mutationLst = getMutationByAlbum(albumId, mutationQueryParams, offset, limit, em);

            for (Mutation m : mutationLst) {
                eventResponses.add(new EventResponse(m, userMember, em));
            }

            XTotalCount = EventQueries.getTotalMutationByAlbum(albumId, mutationQueryParams, em);
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

        try {
            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);

            final HashMap<String, Boolean> userMember = new HashMap<>();
            for(AlbumUser albumUser : album.getAlbumUser()) {
                userMember.put(albumUser.getUser().getSub(), albumUser.isAdmin());
            }

            for (Comment c : EventQueries.getCommentByAlbum(callingUser, album, offset, limit, em)) {
                eventResponses.add(new EventResponse(c, userMember, em));
            }
            XTotalCount = EventQueries.getTotalCommentsByAlbum(callingUser, album, em);
        } finally {
            em.close();
        }
        return new PairListXTotalCount<>(XTotalCount, eventResponses);
    }

    public static PairListXTotalCount<EventResponse> getCommentsByStudyUID(KheopsPrincipal principal, String studyInstanceUID, Integer offset, Integer limit) {

        final List<EventResponse> eventResponses = new ArrayList<>();
        final long XTotalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final HashMap<String, Boolean> userMember = new HashMap<>();

        try {
            User callingUser = em.merge(principal.getUser());

            final List<Comment> comments;
            final boolean isAlbumCapabilityToken = principal.getScope().compareTo(ScopeType.ALBUM) == 0;
            if (isAlbumCapabilityToken) {
                XTotalCount = EventQueries.getTotalPublicCommentsByStudy(studyInstanceUID, em);
                comments = EventQueries.getPublicCommentsByStudy(studyInstanceUID, offset, limit, em);
            } else {
                XTotalCount = EventQueries.getTotalCommentsByStudy(callingUser, studyInstanceUID, em);
                comments = EventQueries.getCommentsByStudy(callingUser, studyInstanceUID, offset, limit, em);

            }

            for (Comment c : comments) {
                if (!userMember.containsKey(c.getUser().getSub()) && canAccessStudy(c.getUser(), c.getStudy(), em)) {
                    userMember.put(c.getUser().getSub(), null);
                }
                if (c.getPrivateTargetUser() != null && !isAlbumCapabilityToken && !userMember.containsKey(c.getPrivateTargetUser().getSub()) && canAccessStudy(c.getPrivateTargetUser(), c.getStudy(), em)) {
                    userMember.put(c.getPrivateTargetUser().getSub(), null);
                }
                eventResponses.add(new EventResponse(c, userMember, em));
            }

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
                User targetUser = getUser(targetUserPk, em);

                if (targetUser != callingUser && !Studies.canAccessStudy(targetUser, study, em)) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message("No access")
                            .detail("Target user can't access to this study")
                            .build();
                    throw new UserNotFoundException(errorResponse);
                }

                comment.setPrivateTargetUser(targetUser);
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
