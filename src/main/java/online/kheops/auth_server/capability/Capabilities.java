package online.kheops.auth_server.capability;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.Users;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.ws.rs.NotFoundException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.getAlbumUser;
import static online.kheops.auth_server.capability.CapabilitiesQueries.*;
import static online.kheops.auth_server.capability.CapabilitiesResponses.CapabilityToCapabilitiesResponses;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesByStudyUIDandSeriesUIDFromInbox;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.study.StudyQueries.findStudyByStudyUIDandUser;

public class Capabilities {
    private static final String DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int TOKEN_LENGTH = 22;
    private static final Pattern pattern = Pattern.compile("^[A-Za-z0-9]{" + TOKEN_LENGTH + "}$");
    private static final Random rdm = new SecureRandom();

    private Capabilities() {
        throw new IllegalStateException("Utility class");
    }

    public static String newCapabilityToken() {
        StringBuilder secretBuilder = new StringBuilder();

        while (secretBuilder.length() < TOKEN_LENGTH) {
            int index = rdm.nextInt(DICT.length());
            secretBuilder.append(DICT.charAt(index));
        }
        return secretBuilder.toString();
    }

    public static Boolean isValidFormat(String token) {
        return pattern.matcher(token).matches();
    }

    public static CapabilitiesResponses.CapabilityResponse createUserCapability(Long callingUserPk, String title, String expirationDate)
            throws UserNotFoundException, DateTimeParseException {
        // parse the given expiration date
        LocalDateTime expirationDateTime;

        CapabilitiesResponses.CapabilityResponse capabilityResponse;

        if (expirationDate != null) {

            OffsetDateTime offsetDateTime = OffsetDateTime.parse(expirationDate);
            expirationDateTime = LocalDateTime.ofInstant(offsetDateTime.toInstant(), ZoneOffset.UTC);

        } else {
            expirationDateTime = LocalDateTime.now(ZoneOffset.UTC).plusMonths(3);
        }

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final User user = Users.getUser(callingUserPk, em);
            final Capability capability = new Capability(user, expirationDateTime, title);

            em.persist(capability);
            em.persist(user);

            capabilityResponse = CapabilityToCapabilitiesResponses(capability);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponses.CapabilityResponse createAlbumCapability(Long callingUserPk, String title, String expirationDate, Long albumPk)
            throws UserNotFoundException, DateTimeParseException, AlbumNotFoundException, NewCapabilityForbidden {
        // parse the given expiration date
        LocalDateTime expirationDateTime;

        CapabilitiesResponses.CapabilityResponse capabilityResponse;

        if (expirationDate != null) {

            OffsetDateTime offsetDateTime = OffsetDateTime.parse(expirationDate);
            expirationDateTime = LocalDateTime.ofInstant(offsetDateTime.toInstant(), ZoneOffset.UTC);

        } else {
            expirationDateTime = LocalDateTime.now(ZoneOffset.UTC).plusMonths(3);
        }

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final User user = Users.getUser(callingUserPk, em);

            final Album album = getAlbum(albumPk, em);
            final AlbumUser albumUser = getAlbumUser(album, user, em);
            if (!albumUser.isAdmin()) {
                throw new NewCapabilityForbidden("Only an admin can générate a capability token for an album");
            }
            final Capability capability = new Capability(user, expirationDateTime, title, album);

            em.persist(capability);
            em.persist(user);

            capabilityResponse = CapabilityToCapabilitiesResponses(capability);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponses.CapabilityResponse createSeriesCapability(Long callingUserPk, String title, String expirationDate, String studyInstanceUID, String seriesInstanceUID)
            throws UserNotFoundException, DateTimeParseException, SeriesNotFoundException{
        // parse the given expiration date
        LocalDateTime expirationDateTime;

        CapabilitiesResponses.CapabilityResponse capabilityResponse;

        if (expirationDate != null) {

            OffsetDateTime offsetDateTime = OffsetDateTime.parse(expirationDate);
            expirationDateTime = LocalDateTime.ofInstant(offsetDateTime.toInstant(), ZoneOffset.UTC);

        } else {
            expirationDateTime = LocalDateTime.now(ZoneOffset.UTC).plusMonths(3);
        }

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final User user = Users.getUser(callingUserPk, em);

            Series series;
            try {
                series = findSeriesByStudyUIDandSeriesUIDFromInbox(user, studyInstanceUID, seriesInstanceUID, em);
            } catch (NotFoundException e) {
                throw new SeriesNotFoundException("Unknown series");
            }
            final Capability capability = new Capability(user, expirationDateTime, title, series);

            em.persist(capability);
            em.persist(user);

            capabilityResponse = CapabilityToCapabilitiesResponses(capability);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponses.CapabilityResponse createStudyCapability(Long callingUserPk, String title, String expirationDate, String studyInstanceUID)
            throws UserNotFoundException, DateTimeParseException, StudyNotFoundException {
        // parse the given expiration date
        LocalDateTime expirationDateTime;

        CapabilitiesResponses.CapabilityResponse capabilityResponse;

        if (expirationDate != null) {

            OffsetDateTime offsetDateTime = OffsetDateTime.parse(expirationDate);
            expirationDateTime = LocalDateTime.ofInstant(offsetDateTime.toInstant(), ZoneOffset.UTC);

        } else {
            expirationDateTime = LocalDateTime.now(ZoneOffset.UTC).plusMonths(3);
        }

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final User user = Users.getUser(callingUserPk, em);

            final Study study;
            try {
                study = findStudyByStudyUIDandUser(studyInstanceUID, user, em);
            } catch (NoResultException e) {
                throw new StudyNotFoundException("Unknown study : "+studyInstanceUID);
            }

            final Capability capability = new Capability(user, expirationDateTime, title, study);

            em.persist(capability);
            em.persist(user);

            capabilityResponse = CapabilityToCapabilitiesResponses(capability);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponses.CapabilityResponse revokeCapability(Long callingUserPk, String secret)
    throws UserNotFoundException, CapabilityNotFound {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        CapabilitiesResponses.CapabilityResponse capabilityResponse;

        try {
            tx.begin();

            final User user = Users.getUser(callingUserPk, em);
            final Capability capability = getCapability(user, secret, em);

            capability.setRevoked(true);
            em.persist(capability);

            capabilityResponse = CapabilityToCapabilitiesResponses(capability);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return  capabilityResponse;
    }

    public static List<CapabilitiesResponses.CapabilityResponse> getCapabilities(Long callingUserPk, boolean showRevoked)
            throws UserNotFoundException {
        List<CapabilitiesResponses.CapabilityResponse> capabilityResponses = new ArrayList<>();

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final User user = Users.getUser(callingUserPk, em);

            List<Capability> capabilities;
            if(showRevoked) {
                capabilities = findCapabilitiesByUserWithRevoke(user, em);
            } else {
                capabilities = findCapabilitiesByUserWitoutRevoke(user, em);
            }

            for (Capability capability: capabilities) {
                CapabilitiesResponses.CapabilityResponse capabilityResponse = CapabilityToCapabilitiesResponses(capability);
                capabilityResponses.add(capabilityResponse);
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponses;
    }

    public static Capability getCapability(User user, String secret, EntityManager em) throws CapabilityNotFound {
        try {
            return findCapabilityByCapabilityTokenandUser(user, secret, em);
        } catch (NoResultException e) {
            throw new CapabilityNotFound();
        }
    }
}
