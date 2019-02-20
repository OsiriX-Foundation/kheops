package online.kheops.auth_server.sharing;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.series.SeriesForbiddenException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.series.SeriesQueries.*;
import static online.kheops.auth_server.study.Studies.getOrCreateStudy;
import static online.kheops.auth_server.user.Users.getUser;

public class Sending {

    private static final Logger LOG = Logger.getLogger(Sending.class.getName());

    private Sending() {
        throw new IllegalStateException("Utility class");
    }

    public static void deleteStudyFromInbox(User callingUser, String studyInstanceUID)
            throws SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final List<Series> seriesList = findSeriesListByStudyUIDFromInbox(callingUser, studyInstanceUID, em);

            if (seriesList.isEmpty()) {
                throw new SeriesNotFoundException("No access to any series with the given studyInstanceUID");
            }

            for (final Series series: seriesList) {
                callingUser.getInbox().removeSeries(series, em);
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteSeriesFromInbox(User callingUser, String studyInstanceUID, String seriesInstanceUID)
            throws SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final Series series;
            try {
                series = findSeriesByStudyUIDandSeriesUIDFromInbox(callingUser, studyInstanceUID, seriesInstanceUID, em);
            } catch (NoResultException e) {
                throw new SeriesNotFoundException("User does not have access to any series with a study with the given studyInstanceUID");
            }

            callingUser.getInbox().removeSeries(series, em);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteStudyFromAlbum(User callingUser, String albumId, String studyInstanceUID)
            throws AlbumNotFoundException, SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Album callingAlbum = getAlbum(albumId, em);

            final List<Series> availableSeries = findSeriesListByStudyUIDFromAlbum(callingUser, callingAlbum, studyInstanceUID, em);

            if (availableSeries.isEmpty()) {
                throw new SeriesNotFoundException("No study with the given StudyInstanceUID in the album");
            }

            for (Series series: availableSeries) {
                callingAlbum.removeSeries(series, em);
            }

            final Study study = availableSeries.get(0).getStudy();
            final Mutation mutation = Events.albumPostStudyMutation(callingUser, callingAlbum, Events.MutationType.REMOVE_STUDY, study);

            em.persist(mutation);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteSeriesFromAlbum(User callingUser, String albumId, String studyInstanceUID, String seriesInstanceUID)
            throws AlbumNotFoundException, SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Album callingAlbum = getAlbum(albumId, em);

            final Series availableSeries;
            try {
                availableSeries = findSeriesByStudyUIDandSeriesUIDFromAlbum(callingUser, callingAlbum, studyInstanceUID, seriesInstanceUID, em);
            } catch (NoResultException e) {
                throw new SeriesNotFoundException("No study with the given StudyInstanceUID in the album");
            }

            callingAlbum.removeSeries(availableSeries, em);
            final Mutation mutation = Events.albumPostSeriesMutation(callingUser, callingAlbum, Events.MutationType.REMOVE_SERIES, availableSeries);

            em.persist(mutation);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void putSeriesInAlbum(long callingUserPk, String albumId, String studyInstanceUID, String seriesInstanceUID)
            throws UserNotFoundException, AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album targetAlbum = getAlbum(albumId, em);

            Series availableSeries;
            try {
                availableSeries = findSeriesByStudyUIDandSeriesUID(studyInstanceUID, seriesInstanceUID, em);

                //Todo series already exist ?? if upload with capability token => verify if orphelin => if not => return forbidden
            } catch (NoResultException e2) {
                // from here the series does not exists
                // find if the study already exists
                final Study study = getOrCreateStudy(studyInstanceUID, em);

                availableSeries = new Series(seriesInstanceUID);
                study.getSeries().add(availableSeries);
                availableSeries.setStudy(study);
                em.persist(availableSeries);
            }

            if (targetAlbum.containsSeries(availableSeries, em)) {
                return;
            }

            final AlbumSeries albumSeries = new AlbumSeries(targetAlbum, availableSeries);
            availableSeries.addAlbumSeries(albumSeries);
            targetAlbum.addSeries(albumSeries);
            em.persist(albumSeries);
            final Mutation mutation = Events.albumPostSeriesMutation(callingUser, targetAlbum, Events.MutationType.IMPORT_SERIES, availableSeries);
            em.persist(mutation);
            //todo if the series is upload with a token...
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void putStudyInAlbum(long callingUserPk, String albumId, String studyInstanceUID, String fromAlbumId, Boolean fromInbox)
            throws UserNotFoundException, AlbumNotFoundException, SeriesNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);
            final Album targetAlbum = getAlbum(albumId, em);

            final List<Series> availableSeries = getSeriesList(callingUser, studyInstanceUID, fromAlbumId, fromInbox, em);

            boolean allSeriesAlreadyExist = true;
            for (Series series: availableSeries) {
                if (!targetAlbum.containsSeries(series, em)) {
                    final AlbumSeries albumSeries = new AlbumSeries(targetAlbum, series);
                    series.addAlbumSeries(albumSeries);
                    targetAlbum.addSeries(albumSeries);
                    em.persist(albumSeries);
                    allSeriesAlreadyExist = false;
                }
            }

            if (allSeriesAlreadyExist) {
                return;
            }
                final Study study = availableSeries.get(0).getStudy();
                final Mutation mutation = Events.albumPostStudyMutation(callingUser, targetAlbum, Events.MutationType.IMPORT_STUDY, study);

                em.persist(mutation);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void shareStudyWithUser(long callingUserPk, String targetUsername, String studyInstanceUID, String fromAlbumId, Boolean fromInbox)
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

            final List<Series> availableSeries = getSeriesList(callingUser, studyInstanceUID, fromAlbumId, fromInbox, em);

            final Album inbox = targetUser.getInbox();
            for (Series series : availableSeries) {
                if (!inbox.containsSeries(series, em)) {
                    final AlbumSeries albumSeries = new AlbumSeries(inbox, series);
                    series.addAlbumSeries(albumSeries);
                    inbox.addSeries(albumSeries);
                    em.persist(albumSeries);
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

    public static void shareSeriesWithUser(long callingUserPk, String targetUsername, String studyInstanceUID, String seriesInstanceUID)
            throws UserNotFoundException, SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User targetUser = getUser(targetUsername, em);

            if (targetUser.getPk() == callingUserPk) { // the user is requesting access to a new series
                //return Response.status(Response.Status.FORBIDDEN).entity("Use studies/{StudyInstanceUID}/series/{SeriesInstanceUID} for request access to a new series").build();
                return;
            }

            final Series series;
            try {
                series = findSeriesByStudyUIDandSeriesUID(studyInstanceUID, seriesInstanceUID, em);
            } catch (NoResultException exception) {
                throw new SeriesNotFoundException("Unknown series");
            }

            final Album inbox = targetUser.getInbox();
            if(inbox.containsSeries(series, em)) {
                //target user has already access to the series
                return;
            }

            final AlbumSeries albumSeries = new AlbumSeries(inbox, series);
            series.addAlbumSeries(albumSeries);
            inbox.addSeries(albumSeries);
            em.persist(albumSeries);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void appropriateSeries(long callingUserPk, String studyInstanceUID, String seriesInstanceUID)
            throws UserNotFoundException, SeriesForbiddenException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(callingUserPk, em);

            try {
                final Series storedSeries = findSeriesByStudyUIDandSeriesUID(studyInstanceUID, seriesInstanceUID, em);

                if(isOrphan(storedSeries, em)) {
                    //here the series exists but she is orphan
                    final Album inbox = callingUser.getInbox();
                    final AlbumSeries inboxSeries = new AlbumSeries(inbox, storedSeries);
                    storedSeries.addAlbumSeries(inboxSeries);
                    inbox.addSeries(inboxSeries);
                    em.persist(inboxSeries);
                    tx.commit();
                    LOG.info(() -> "Claim accepted because the series is inside an album where the calling user (" + callingUser.getKeycloakId() + ") is member, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:" + seriesInstanceUID);
                    return; //appropriate OK
                } else {
                    try {
                        //here the series is not orphan
                        findSeriesBySeriesAndUserInbox(callingUser, storedSeries, em);
                        return; //the series is already in the inbox
                    } catch (NoResultException e) {
                        try {
                            final Series series = findSeriesBySeriesAndAlbumWithSendPermission(callingUser, storedSeries, em);
                            final Album inbox = callingUser.getInbox();
                            final AlbumSeries inboxSeries = new AlbumSeries(inbox, series);
                            series.addAlbumSeries(inboxSeries);
                            inbox.addSeries(inboxSeries);

                            em.persist(inboxSeries);
                            tx.commit();
                            return;
                        } catch (NoResultException e2) {
                            throw new SeriesForbiddenException("TODO the series already exist");//TODO
                        }
                    }
                }

            } catch (NoResultException ignored) {/*empty*/}

            // from here the series does not exists
            // find if the study already exists
            final Study study = getOrCreateStudy(studyInstanceUID, em);

            final Series series = new Series(seriesInstanceUID);
            study.getSeries().add(series);
            final Album inbox = callingUser.getInbox();
            final AlbumSeries inboxSeries = new AlbumSeries(inbox, series);
            series.addAlbumSeries(inboxSeries);
            inbox.addSeries(inboxSeries);

            em.persist(series);
            em.persist(inboxSeries);
            LOG.info(() -> "finished claiming, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:" + seriesInstanceUID + " to " + callingUser.getKeycloakId());

            tx.commit();
            LOG.info(() -> "sending, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:" + seriesInstanceUID + " to " + callingUser.getKeycloakId());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void appropriateStudy(long callingUserPk, String studyInstanceUID, String albumId)
            throws UserNotFoundException, AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();


            final User callingUser = getUser(callingUserPk, em);
            final Album album = getAlbum(albumId, em);
            final Album inbox = callingUser.getInbox();

            final List<Series> seriesLst = findSeriesListByStudyUIDFromAlbum(callingUser, album, studyInstanceUID, em);

            for (Series series : seriesLst) {
                if(!inbox.containsSeries(series, em)) {
                    final AlbumSeries inboxSeries = new AlbumSeries(inbox, series);
                    series.addAlbumSeries(inboxSeries);
                    inbox.addSeries(inboxSeries);

                    em.persist(inboxSeries);
                }
            }

            tx.commit();
            LOG.info(() -> "sending, StudyInstanceUID:" + studyInstanceUID + " to " + callingUser.getKeycloakId() + "from album :" + albumId);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static Set<String> availableSeriesUIDs(long userPk, String studyInstanceUID, String fromAlbumId, Boolean fromInbox)
            throws UserNotFoundException, AlbumNotFoundException , StudyNotFoundException {
        Set<String> availableSeriesUIDs;

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = getUser(userPk, em);

            if (fromAlbumId != null) {
                final Album album = getAlbum(fromAlbumId, em);
                availableSeriesUIDs = findAllSeriesInstanceUIDbyStudyUIDfromAlbum(callingUser, album, studyInstanceUID, em);
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

    public static  List<Series> getSeriesList(User callingUser, String studyInstanceUID, String fromAlbumId, Boolean fromInbox, EntityManager em)
            throws AlbumNotFoundException , SeriesNotFoundException{
        List<Series> availableSeries;

        if (fromAlbumId != null) {
            final Album callingAlbum = getAlbum(fromAlbumId, em);

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
