package online.kheops.auth_server.sharing;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumForbiddenException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.series.SeriesForbiddenException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;

import javax.persistence.*;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.getAlbumUser;
import static online.kheops.auth_server.series.SeriesQueries.*;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.user.Users.getUser;

public class Sending {

    private static final Logger LOG = Logger.getLogger(Sending.class.getName());

    private Sending() {
        throw new IllegalStateException("Utility class");
    }

    public static void deleteStudyFromInbox(long callingUserPk, String studyInstanceUID)
            throws UserNotFoundException, SeriesNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            User callingUser = getUser(callingUserPk, em);

            final List<Series> seriesList = findSeriesListByStudyUIDFromInbox(callingUser, studyInstanceUID, em);

            if (seriesList.isEmpty()) {
                throw new SeriesNotFoundException("No access to any series with the given studyInstanceUID");
            }

            for (Series series: seriesList) {
                callingUser.getInbox().removeSeries(series);
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteSeriesFromInbox(long callingUserPk, String studyInstanceUID, String seriesInstanceUID)
            throws UserNotFoundException, SeriesNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            User callingUser = getUser(callingUserPk, em);

            Series series;
            try {
                series = findSeriesByStudyUIDandSeriesUIDFromInbox(callingUser, studyInstanceUID, seriesInstanceUID, em);
            } catch (NoResultException e) {
                throw new SeriesNotFoundException("User does not have access to any series with a study with the given studyInstanceUID");
            }

            callingUser.getInbox().removeSeries(series);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteStudyFromAlbum(long callingUserPk, long albumPk, String studyInstanceUID)
            throws UserNotFoundException, AlbumNotFoundException, SeriesNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album callingAlbum = getAlbum(albumPk, em);

            final List<Series> availableSeries = findSeriesListByStudyUIDFromAlbum(callingUser, callingAlbum, studyInstanceUID, em);

            if (availableSeries.isEmpty()) {
                throw new SeriesNotFoundException("No study with the given StudyInstanceUID in the album");
            }

            for (Series series: availableSeries) {
                callingAlbum.removeSeries(series);
                em.persist(series);
            }

            final Study study = availableSeries.get(0).getStudy();
            final Mutation mutation = Events.albumPostStudyMutation(callingUser, callingAlbum, Events.MutationType.REMOVE_STUDY, study);

            em.persist(mutation);
            em.persist(study);
            em.persist(callingAlbum);
            em.persist(callingUser);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteSeriesFromAlbum(long callingUserPk, long albumPk, String studyInstanceUID, String seriesInstanceUID)
            throws UserNotFoundException, AlbumNotFoundException, SeriesNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album callingAlbum = getAlbum(albumPk, em);

            Series availableSeries;
            try {
                availableSeries = findSeriesByStudyUIDandSeriesUIDFromAlbum(callingUser, callingAlbum, studyInstanceUID, seriesInstanceUID, em);
            } catch (NoResultException e) {
                throw new SeriesNotFoundException("No study with the given StudyInstanceUID in the album");
            }

            callingAlbum.removeSeries(availableSeries);
            final Mutation mutation = Events.albumPostSeriesMutation(callingUser, callingAlbum, Events.MutationType.REMOVE_SERIES, availableSeries);

            em.persist(mutation);
            em.persist(availableSeries);
            em.persist(callingAlbum);
            em.persist(callingUser);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void putSeriesInAlbum(long callingUserPk, long albumPk, String studyInstanceUID, String seriesInstanceUID)
            throws UserNotFoundException, AlbumNotFoundException, SeriesNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album targetAlbum = getAlbum(albumPk, em);

            final Series availableSeries;
            try {
                availableSeries = findSeriesByStudyUIDandSeriesUID(studyInstanceUID, seriesInstanceUID, em);
            } catch (NotFoundException e2) {
                throw new SeriesNotFoundException("No series with the given StudyInstanceUID and SeriesInstanceUID");
            }

            if (targetAlbum.getSeries().contains(availableSeries)) {
                return;
            }

            targetAlbum.addSeries(availableSeries);
            final Mutation mutation = Events.albumPostSeriesMutation(callingUser, targetAlbum, Events.MutationType.IMPORT_SERIES, availableSeries);

            em.persist(mutation);
            em.persist(availableSeries);
            em.persist(targetAlbum);
            em.persist(callingUser);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void putStudyInAlbum(long callingUserPk, long albumPk, String studyInstanceUID, Long fromAlbumPk, Boolean fromInbox)
            throws UserNotFoundException, AlbumNotFoundException, SeriesNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album targetAlbum = getAlbum(albumPk, em);

            final List<Series> availableSeries = getSeriesList(callingUser, studyInstanceUID, fromAlbumPk, fromInbox, em);

            Boolean allSeriesAlreadyExist = true;
            for (Series series: availableSeries) {
                if (!targetAlbum.getSeries().contains(series)) {
                    targetAlbum.addSeries(series);
                    em.persist(series);
                    allSeriesAlreadyExist = false;
                }
            }

            if (allSeriesAlreadyExist) {
                return;
            }
                final Study study = availableSeries.get(0).getStudy();
                final Mutation mutation = Events.albumPostStudyMutation(callingUser, targetAlbum, Events.MutationType.IMPORT_STUDY, study);

                em.persist(mutation);
                em.persist(targetAlbum);
                em.persist(callingUser);


            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void shareStudyWithUser(long callingUserPk, String targetUsername, String studyInstanceUID, Long fromAlbumPk, Boolean fromInbox)
            throws UserNotFoundException, AlbumNotFoundException, SeriesNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final User targetUser = getUser(targetUsername, em);

            if (callingUserPk == targetUser.getPk()) {
                //return Response.status(Response.Status.BAD_REQUEST).entity("Can't send a study to yourself").build();
                return;
            }

            final List<Series> availableSeries = getSeriesList(callingUser, studyInstanceUID, fromAlbumPk, fromInbox, em);

            final Album inbox = targetUser.getInbox();
            for (Series series : availableSeries) {
                if (!inbox.getSeries().contains(series)) {
                    inbox.addSeries(series);
                    em.persist(series);
                }
            }
            em.persist(inbox);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void shareSeriesWithUser(long callingUserPk, String targetUsername, String studyInstanceUID, String seriesInstanceUID)
            throws UserNotFoundException, SeriesNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User targetUser = getUser(targetUsername, em);

            if (targetUser.getPk() == callingUserPk) { // the user is requesting access to a new series
                //return Response.status(Response.Status.FORBIDDEN).entity("Use studies/{StudyInstanceUID}/series/{SeriesInstanceUID} for request access to a new series").build();
                return;
            }

            Series series;
            try {
                series = findSeriesByStudyUIDandSeriesUID(studyInstanceUID, seriesInstanceUID, em);
            } catch (NoResultException exception) {
                throw new SeriesNotFoundException("Unknown series");
            }

            final Album inbox = targetUser.getInbox();
            if(inbox.getSeries().contains(series)) {
                //return Response.status(Response.Status.OK).build();
                return;
            }

            inbox.addSeries(series);

            em.persist(inbox);
            em.persist(series);
            em.persist(targetUser);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void appropriateSeries(long callingUserPk, String studyInstanceUID, String seriesInstanceUID)
            throws UserNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);

            try {
                final Series storedSeries = findSeriesByStudyUIDandSeriesUID(studyInstanceUID, seriesInstanceUID, em);

                // here the series exists but she is orphan or the calling can send the series from an album
                final Album inbox = callingUser.getInbox();
                inbox.addSeries(storedSeries);
                em.persist(inbox);
                em.persist(storedSeries);
                em.persist(callingUser);
                tx.commit();
                LOG.info("Claim accepted because the series is inside an album where the calling user (" + callingUser.getGoogleId() + ") is member, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:" + seriesInstanceUID);
                return;

            } catch (NoResultException ignored) {/*empty*/}

            // from here the series does not exists
            // find if the study already exists
            Study study;
            try {
                study = getStudy(studyInstanceUID, em);
            } catch (StudyNotFoundException ignored) {
                // the study doesn't exist, we need to create it
                study = new Study();
                study.setStudyInstanceUID(studyInstanceUID);
                em.persist(study);
            }

            final Series series = new Series(seriesInstanceUID);
            study.getSeries().add(series);
            Album inbox = callingUser.getInbox();
            inbox.addSeries(series);

            em.persist(inbox);
            em.persist(study);
            em.persist(series);
            em.persist(callingUser);
            LOG.info("finished claiming, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:" + seriesInstanceUID + " to " + callingUser.getGoogleId());

            tx.commit();
            LOG.info("sending, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:" + seriesInstanceUID + " to " + callingUser.getGoogleId());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static Set<String> availableSeriesUIDs(long userPk, String studyInstanceUID, Long fromAlbumPk, Boolean fromInbox)
            throws UserNotFoundException, AlbumNotFoundException , StudyNotFoundException {
        Set<String> availableSeriesUIDs;

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(userPk, em);

            if (fromAlbumPk != null) {
                final Album album = getAlbum(fromAlbumPk, em);
                availableSeriesUIDs = findAllSeriesInstanceUIDbySeriesIUIDfromAlbum(callingUser, album, studyInstanceUID, em);
            } else if (fromInbox) {
                availableSeriesUIDs = findAllSeriesInstanceUIDbySeriesIUIDfromInbox(callingUser, studyInstanceUID, em);
            } else {
                availableSeriesUIDs = findAllSeriesInstanceUIDbySeriesIUIDfromAlbumandInbox(callingUser, studyInstanceUID, em);
            }

            tx.commit();
        } catch (NoResultException e) {
            throw new StudyNotFoundException("StudyInstanceUID : "+studyInstanceUID+" not found");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return availableSeriesUIDs;
    }

    public static  List<Series> getSeriesList(User callingUser, String studyInstanceUID, Long fromAlbumPk, Boolean fromInbox, EntityManager em)
            throws AlbumNotFoundException , SeriesNotFoundException{
        List<Series> availableSeries;

        if (fromAlbumPk != null) {
            final Album callingAlbum = getAlbum(fromAlbumPk, em);

            availableSeries = findSeriesListByStudyUIDFromAlbum(callingUser, callingAlbum, studyInstanceUID, em);
        } else if (fromInbox) {
            availableSeries = findSeriesListByStudyUIDFromInbox(callingUser, studyInstanceUID, em);
        } else {
            availableSeries = findSeriesListByStudyUIDFromAlbumAndInbox(callingUser, studyInstanceUID, em);
        }

        if (availableSeries.isEmpty()) {
            throw new SeriesNotFoundException("No study with the given StudyInstanceUID");
        }
        return availableSeries;
    }
}
