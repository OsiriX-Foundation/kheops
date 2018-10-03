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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.getAlbumUser;
import static online.kheops.auth_server.capability.CapabilitiesQueries.*;
import static online.kheops.auth_server.capability.CapabilitiesResponses.capabilityToCapabilitiesResponses;
import static online.kheops.auth_server.series.SeriesQueries.findSeriesByStudyUIDandSeriesUIDFromInbox;
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

    public static CapabilitiesResponses.CapabilityResponse generateCapability(CapabilityParameters capabilityParameters)
            throws UserNotFoundException, AlbumNotFoundException, NewCapabilityForbidden, SeriesNotFoundException, StudyNotFoundException , CapabilityBadRequest{
        return capabilityParameters.getScopeType().generateCapability(capabilityParameters);
    }

    public static CapabilitiesResponses.CapabilityResponse createUserCapability(CapabilityParameters capabilityParameters)
            throws UserNotFoundException, CapabilityBadRequest {

        CapabilitiesResponses.CapabilityResponse capabilityResponse;

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final User user = Users.getUser(capabilityParameters.getCallingUserPk(), em);
            //final Capability capability = new Capability(user, capabilityParameters.getExpirationDate(), capabilityParameters.getStartDate(), capabilityParameters.getTitle(), capabilityParameters.isReadPermission(), capabilityParameters.isWritePermission());
            final Capability capability = new Capability.CapabilityBuilder()
                    .user(user)
                    .expirationTime(capabilityParameters.getExpirationTime())
                    .notBeforeTime(capabilityParameters.getNotBeforeTime())
                    .title(capabilityParameters.getTitle())
                    .readPermission(capabilityParameters.isReadPermission())
                    .writePermission(capabilityParameters.isWritePermission())
                    .scopeType(ScopeType.USER)
                    .build();
            em.persist(capability);
            em.persist(user);

            capabilityResponse = capabilityToCapabilitiesResponses(capability);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponses.CapabilityResponse createAlbumCapability(CapabilityParameters capabilityParameters)
            throws UserNotFoundException, AlbumNotFoundException, NewCapabilityForbidden, CapabilityBadRequest {

        CapabilitiesResponses.CapabilityResponse capabilityResponse;

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final User user = Users.getUser(capabilityParameters.getCallingUserPk(), em);

            final Album album = getAlbum(capabilityParameters.getAlbumPk(), em);
            final AlbumUser albumUser = getAlbumUser(album, user, em);
            if (!albumUser.isAdmin()) {
                throw new NewCapabilityForbidden("Only an admin can generate a capability token for an album");
            }
            //final Capability capability = new Capability(user, capabilityParameters.getExpirationDate(), capabilityParameters.getStartDate(), capabilityParameters.getTitle(), album, capabilityParameters.isReadPermission(), capabilityParameters.isWritePermission());
            final Capability capability = new Capability.CapabilityBuilder()
                    .user(user)
                    .expirationTime(capabilityParameters.getExpirationTime())
                    .notBeforeTime(capabilityParameters.getNotBeforeTime())
                    .title(capabilityParameters.getTitle())
                    .readPermission(capabilityParameters.isReadPermission())
                    .writePermission(capabilityParameters.isWritePermission())
                    .scopeType(ScopeType.ALBUM)
                    .album(album)
                    .build();
            em.persist(capability);
            em.persist(user);

            capabilityResponse = capabilityToCapabilitiesResponses(capability);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponses.CapabilityResponse createSeriesCapability(CapabilityParameters capabilityParameters)
            throws UserNotFoundException, SeriesNotFoundException, CapabilityBadRequest {

        CapabilitiesResponses.CapabilityResponse capabilityResponse;

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final User user = Users.getUser(capabilityParameters.getCallingUserPk(), em);

            Series series;
            try {
                series = findSeriesByStudyUIDandSeriesUIDFromInbox(user, capabilityParameters.getStudyInstanceUID(), capabilityParameters.getSeriesInstanceUID(), em);
            } catch (NotFoundException e) {
                throw new SeriesNotFoundException("Unknown series");
            }
            //final Capability capability = new Capability(user, capabilityParameters.getExpirationDate(), capabilityParameters.getStartDate(), capabilityParameters.getTitle(), series, capabilityParameters.isReadPermission(), capabilityParameters.isWritePermission());
            final Capability capability = new Capability.CapabilityBuilder()
                    .user(user)
                    .expirationTime(capabilityParameters.getExpirationTime())
                    .notBeforeTime(capabilityParameters.getNotBeforeTime())
                    .title(capabilityParameters.getTitle())
                    .readPermission(capabilityParameters.isReadPermission())
                    .writePermission(capabilityParameters.isWritePermission())
                    .scopeType(ScopeType.SERIES)
                    .study(series.getStudy())
                    .series(series)
                    .build();

            em.persist(capability);
            em.persist(user);

            capabilityResponse = capabilityToCapabilitiesResponses(capability);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponses.CapabilityResponse createStudyCapability(CapabilityParameters capabilityParameters)
            throws UserNotFoundException, StudyNotFoundException, CapabilityBadRequest{

        CapabilitiesResponses.CapabilityResponse capabilityResponse;

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final User user = Users.getUser(capabilityParameters.getCallingUserPk(), em);

            final Study study;
            try {
                study = findStudyByStudyUIDandUser(capabilityParameters.getStudyInstanceUID(), user, em);
            } catch (NoResultException e) {
                throw new StudyNotFoundException("Unknown study : "+capabilityParameters.getStudyInstanceUID());
            }

            //final Capability capability = new Capability(user, capabilityParameters.getExpirationDate(), capabilityParameters.getStartDate(), capabilityParameters.getTitle(), study, capabilityParameters.isReadPermission(), capabilityParameters.isWritePermission());
            final Capability capability = new Capability.CapabilityBuilder()
                    .user(user)
                    .expirationTime(capabilityParameters.getExpirationTime())
                    .notBeforeTime(capabilityParameters.getNotBeforeTime())
                    .title(capabilityParameters.getTitle())
                    .readPermission(capabilityParameters.isReadPermission())
                    .writePermission(capabilityParameters.isWritePermission())
                    .scopeType(ScopeType.STUDY)
                    .study(study)
                    .build();

            em.persist(capability);
            em.persist(user);

            capabilityResponse = capabilityToCapabilitiesResponses(capability);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponses.CapabilityResponse revokeCapability(Long callingUserPk, long capabilityId)
    throws UserNotFoundException, CapabilityNotFound {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        CapabilitiesResponses.CapabilityResponse capabilityResponse;

        try {
            tx.begin();

            final User user = Users.getUser(callingUserPk, em);
            final Capability capability = getCapability(user, capabilityId, em);

            capability.setRevoked(true);
            em.persist(capability);

            capabilityResponse = capabilityToCapabilitiesResponses(capability);

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
                CapabilitiesResponses.CapabilityResponse capabilityResponse = capabilityToCapabilitiesResponses(capability);
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

    public static Capability getCapability(User user, long capabilityId, EntityManager em) throws CapabilityNotFound {
        try {
            return findCapabilityByCapabilityTokenandUser(user, capabilityId, em);
        } catch (NoResultException e) {
            throw new CapabilityNotFound();
        }
    }

    public static Capability getCapability(String secret, EntityManager em) throws CapabilityNotFound {
        try {
            return findCapabilityByCapabilityToken(secret, em);
        } catch (NoResultException e) {
            throw new CapabilityNotFound();
        }
    }
}
