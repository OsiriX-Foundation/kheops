package online.kheops.auth_server.sharing;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.report_provider.ClientIdNotFoundException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.BadRequestException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.report_provider.ReportProviders.getReportProvider;
import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.series.Series.isSeriesInInbox;
import static online.kheops.auth_server.series.SeriesQueries.*;
import static online.kheops.auth_server.study.Studies.getOrCreateStudy;
import static online.kheops.auth_server.user.Users.getOrCreateUser;

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
            final Series series = findSeriesByStudyUIDandSeriesUIDFromInbox(callingUser, studyInstanceUID, seriesInstanceUID, em);
            callingUser.getInbox().removeSeries(series, em);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteStudyFromAlbum(KheopsPrincipalInterface kheopsPrincipal, String albumId, String studyInstanceUID)
            throws AlbumNotFoundException, SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
            final Album callingAlbum = getAlbum(albumId, em);

            final List<Series> availableSeries = findSeriesListByStudyUIDFromAlbum(callingUser, callingAlbum, studyInstanceUID, em);

            if (availableSeries.isEmpty()) {
                throw new SeriesNotFoundException("No study with the given StudyInstanceUID in the album");
            }

            for (Series series: availableSeries) {
                callingAlbum.removeSeries(series, em);
            }

            final Study study = availableSeries.get(0).getStudy();
            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().get());
                mutation = Events.albumPostStudyMutation(capability, callingAlbum, Events.MutationType.REMOVE_STUDY, study);
            } else {
                mutation = Events.albumPostStudyMutation(callingUser, callingAlbum, Events.MutationType.REMOVE_STUDY, study);
            }
            em.persist(mutation);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void deleteSeriesFromAlbum(KheopsPrincipalInterface kheopsPrincipal, String albumId, String studyInstanceUID, String seriesInstanceUID)
            throws AlbumNotFoundException, SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
            final Album callingAlbum = getAlbum(albumId, em);

            final Series availableSeries = findSeriesByStudyUIDandSeriesUIDFromAlbum(callingAlbum, studyInstanceUID, seriesInstanceUID, em);

            callingAlbum.removeSeries(availableSeries, em);
            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().get());
                mutation = Events.albumPostSeriesMutation(capability, callingAlbum, Events.MutationType.REMOVE_SERIES, availableSeries);
            } else {
                mutation = Events.albumPostSeriesMutation(callingUser, callingAlbum, Events.MutationType.REMOVE_SERIES, availableSeries);
            }

            em.persist(mutation);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void putSeriesInAlbum(KheopsPrincipalInterface kheopsPrincipal, String albumId, String studyInstanceUID, String seriesInstanceUID)
            throws AlbumNotFoundException, ClientIdNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
            final Album targetAlbum = getAlbum(albumId, em);

            Series availableSeries;
            try {
                availableSeries = getSeries(studyInstanceUID, seriesInstanceUID, em);

                //Todo series already exist ?? if upload with capability token => verify if orphelin => if not => return forbidden
            } catch (SeriesNotFoundException e2) {
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

            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().get());
                mutation = Events.albumPostSeriesMutation(capability, targetAlbum, Events.MutationType.IMPORT_SERIES, availableSeries);
            } else if(kheopsPrincipal.getClientId().isPresent()) {
                final ReportProvider reportProvider = getReportProvider(kheopsPrincipal.getClientId().get());
                mutation = Events.newReport(callingUser, targetAlbum, reportProvider, Events.MutationType.NEW_REPORT, availableSeries);
            } else {
                mutation = Events.albumPostSeriesMutation(callingUser, targetAlbum, Events.MutationType.IMPORT_SERIES, availableSeries);
            }
            em.persist(mutation);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void putStudyInAlbum(KheopsPrincipalInterface kheopsPrincipal, String albumId, String studyInstanceUID, String fromAlbumId, Boolean fromInbox)
            throws AlbumNotFoundException, SeriesNotFoundException {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User callingUser = em.merge(kheopsPrincipal.getUser());
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
            final Mutation mutation;
            if (kheopsPrincipal.getCapability().isPresent() && kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Capability capability = em.merge(kheopsPrincipal.getCapability().get());
                mutation = Events.albumPostStudyMutation(capability, targetAlbum, Events.MutationType.IMPORT_STUDY, study);
            } else {
                mutation = Events.albumPostStudyMutation(callingUser, targetAlbum, Events.MutationType.IMPORT_STUDY, study);
            }
                em.persist(mutation);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void shareStudyWithUser(User callingUser, String targetUsername, String studyInstanceUID, String fromAlbumId, Boolean fromInbox)
            throws UserNotFoundException, AlbumNotFoundException, SeriesNotFoundException, BadRequestException {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final User targetUser = em.merge(getOrCreateUser(targetUsername));

            if (callingUser == targetUser) {
                if(fromAlbumId != null) {
                    appropriateStudy(callingUser, studyInstanceUID, fromAlbumId);
                    return;
                }
                throw new BadRequestException("CallingUser ant targetUser are the same : it's for appropriate a study. But queryParam 'album' is null");
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

    public static void shareSeriesWithUser(User callingUser, String targetUsername, String studyInstanceUID, String seriesInstanceUID)
            throws UserNotFoundException, SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User targetUser = em.merge(getOrCreateUser(targetUsername));
            callingUser = em.merge(callingUser);

            if (targetUser == callingUser) { // the user is requesting access to a new series
                appropriateSeries(callingUser, studyInstanceUID, seriesInstanceUID);
            }

            final Series series = getSeries(studyInstanceUID, seriesInstanceUID, em);
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

    public static void appropriateSeries(User callingUser, String studyInstanceUID, String seriesInstanceUID)
            throws SeriesNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            try {
                final Series storedSeries = getSeries(studyInstanceUID, seriesInstanceUID, em);

                if(isOrphan(storedSeries, em)) {
                    //here the series exists but she is orphan
                    final Album inbox = callingUser.getInbox();
                    final AlbumSeries inboxSeries = new AlbumSeries(inbox, storedSeries);
                    storedSeries.addAlbumSeries(inboxSeries);
                    inbox.addSeries(inboxSeries);
                    em.persist(inboxSeries);
                    tx.commit();

                    final User finalCallingUser1 = callingUser;
                    LOG.info(() -> "Claim accepted because the series is inside an album where the calling user (" + finalCallingUser1.getKeycloakId() + ") is member, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:" + seriesInstanceUID);
                    return; //appropriate OK
                } else if(isSeriesInInbox(callingUser, storedSeries, em)) {
                        return;
                } else {
                    try {
                        final Series series = findSeriesBySeriesAndAlbumWithSendPermission(callingUser, storedSeries, em);
                        final Album inbox = callingUser.getInbox();
                        final AlbumSeries inboxSeries = new AlbumSeries(inbox, series);
                        series.addAlbumSeries(inboxSeries);
                        inbox.addSeries(inboxSeries);

                        em.persist(inboxSeries);
                        tx.commit();
                        return;
                    } catch (SeriesNotFoundException e2) {
                        throw new SeriesNotFoundException(e2.getMessage());
                    }
                }

            } catch (SeriesNotFoundException ignored) {/*empty*/}

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

            final User finalCallingUser = callingUser;
            LOG.info(() -> "finished claiming, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:" + seriesInstanceUID + " to " + finalCallingUser.getKeycloakId());

            tx.commit();
            LOG.info(() -> "sending, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:" + seriesInstanceUID + " to " + finalCallingUser.getKeycloakId());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static void appropriateStudy(User callingUser, String studyInstanceUID, String albumId)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();


            callingUser = em.merge(callingUser);
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
            final User finalCallingUser = callingUser;
            LOG.info(() -> "sending, StudyInstanceUID:" + studyInstanceUID + " to " + finalCallingUser.getKeycloakId() + "from album :" + albumId);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public static Set<String> availableSeriesUIDs(User callingUser, String studyInstanceUID, String fromAlbumId, Boolean fromInbox)
            throws AlbumNotFoundException , StudyNotFoundException {
        Set<String> availableSeriesUIDs;

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            if (fromAlbumId != null) {
                final Album album = getAlbum(fromAlbumId, em);
                availableSeriesUIDs = findAllSeriesInstanceUIDbyStudyUIDfromAlbum(callingUser, album, studyInstanceUID, em);
            } else if (fromInbox) {
                availableSeriesUIDs = findAllSeriesInstanceUIDbySeriesIUIDfromInbox(callingUser, studyInstanceUID, em);
            } else {
                availableSeriesUIDs = findAllSeriesInstanceUIDbySeriesIUIDfromAlbumandInbox(callingUser, studyInstanceUID, em);
            }

            tx.commit();
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
        final List<Series> availableSeries;

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
