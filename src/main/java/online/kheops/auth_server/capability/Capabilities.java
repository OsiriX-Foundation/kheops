package online.kheops.auth_server.capability;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.user.UserNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.getAlbumUser;
import static online.kheops.auth_server.capability.CapabilitiesQueries.*;
import static online.kheops.auth_server.user.UserQueries.findUserByPk;

public class Capabilities {

    private static final String TOKEN_DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int TOKEN_LENGTH = 22;
    public static final String TOKEN_PATTERN = "[A-Za-z0-9]{" + TOKEN_LENGTH + "}";
    private static final String TOKEN_PATTERN_STRICT = "^" + TOKEN_PATTERN + "$";
    private static final Pattern tokenPattern = Pattern.compile(TOKEN_PATTERN_STRICT);

    private static final String ID_DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int ID_LENGTH = 10;
    public static final String ID_PATTERN = "[A-Za-z0-9]{" + ID_LENGTH + "}";

    private static final Random rdm = new SecureRandom();

    private Capabilities() {
        throw new IllegalStateException("Utility class");
    }

    public static String newCapabilityToken() {
        final StringBuilder secretBuilder = new StringBuilder();
        do {
            while (secretBuilder.length() < TOKEN_LENGTH) {
                int index = rdm.nextInt(TOKEN_DICT.length());
                secretBuilder.append(TOKEN_DICT.charAt(index));
            }
        } while (capabilitySecretExist(secretBuilder.toString()));
        return secretBuilder.toString();
    }

    public static String newCapabilityID() {
        final StringBuilder idBuilder = new StringBuilder();

        do {
            idBuilder.setLength(0);
            while (idBuilder.length() < ID_LENGTH) {
                int index = rdm.nextInt(ID_DICT.length());
                idBuilder.append(ID_DICT.charAt(index));
            }
        } while (capabilityIDExist(idBuilder.toString()));
        return idBuilder.toString();
    }

    public static String hashCapability(String capability) {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
        byte[] encodedhash = digest.digest(capability.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    private static String bytesToHex(byte[] hash) {
        final StringBuilder hexString = new StringBuilder();
        for (byte hash1 : hash) {
            String hex = Integer.toHexString(0xff & hash1);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static Boolean isValidFormat(String token) {
        return tokenPattern.matcher(token).matches();
    }

    public static CapabilitiesResponse.Response generateCapability(CapabilityParameters capabilityParameters)
            throws UserNotFoundException, AlbumNotFoundException, NewCapabilityForbidden , CapabilityBadRequestException, UserNotMemberException {
        return capabilityParameters.getScopeType().generateCapability(capabilityParameters);
    }

    public static CapabilitiesResponse.Response createUserCapability(CapabilityParameters capabilityParameters)
            throws CapabilityBadRequestException {

        final CapabilitiesResponse.Response capabilityResponse;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final User user = em.merge(capabilityParameters.getCallingUser());
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

            capabilityResponse = new CapabilitiesResponse(capability, true, false).getResponse();

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponse.Response createAlbumCapability(CapabilityParameters capabilityParameters)
            throws AlbumNotFoundException, NewCapabilityForbidden, CapabilityBadRequestException, UserNotMemberException {

        final CapabilitiesResponse.Response capabilityResponse;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            final User user = em.merge(capabilityParameters.getCallingUser());

            final Album album = getAlbum(capabilityParameters.getAlbumId(), em);
            final AlbumUser albumUser = getAlbumUser(album, user, em);
            if (!albumUser.isAdmin()) {
                throw new NewCapabilityForbidden("Only an admin can generate a capability token for an album");
            }
            final Capability capability = new Capability.CapabilityBuilder()
                    .user(user)
                    .expirationTime(capabilityParameters.getExpirationTime())
                    .notBeforeTime(capabilityParameters.getNotBeforeTime())
                    .title(capabilityParameters.getTitle())
                    .readPermission(capabilityParameters.isReadPermission())
                    .writePermission(capabilityParameters.isWritePermission())
                    .appropriatePermission(capabilityParameters.isAppropriatePermission())
                    .downloadPermission(capabilityParameters.isDownloadPermission())
                    .scopeType(ScopeType.ALBUM)
                    .album(album)
                    .build();

            em.persist(capability);

            capabilityResponse = new CapabilitiesResponse(capability, true, false).getResponse();

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }
    
    public static CapabilitiesResponse.Response revokeCapability(User callingUser, String capabilityId)
            throws CapabilityNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        final CapabilitiesResponse.Response capabilityResponse;

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Capability capability = getCapability(callingUser, capabilityId, em);

            capability.setRevoked(true);
            em.persist(capability);

            capabilityResponse = new CapabilitiesResponse(capability, false, false).getResponse();

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return  capabilityResponse;
    }

    public static List<CapabilitiesResponse.Response> getCapabilities(User callingUser, boolean valid) {

        final List<CapabilitiesResponse.Response> capabilityResponses = new ArrayList<>();

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final List<Capability> capabilities;
            if(valid) {
                capabilities = findCapabilitiesByUserValidOnly(callingUser, em);
            } else {
                capabilities = findAllCapabilitiesByUser(callingUser, em);
            }

            for (Capability capability: capabilities) {
                capabilityResponses.add(new CapabilitiesResponse(capability, false, false).getResponse());
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

    public static List<CapabilitiesResponse.Response> getCapabilities(String albumId, boolean valid) {

        final List<CapabilitiesResponse.Response> capabilityResponses = new ArrayList<>();

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final List<Capability> capabilities;
            if(valid) {
                capabilities = findCapabilitiesByAlbumValidOnly(albumId, em);
            } else {
                capabilities = findAllCapabilitiesByAlbum(albumId, em);
            }

            for (Capability capability: capabilities) {
                capabilityResponses.add(new CapabilitiesResponse(capability, false, false).getResponse());
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

    public static CapabilitiesResponse.Response getCapabilityInfo(String capabilityToken)
            throws CapabilityNotFoundException {

        final CapabilitiesResponse.Response capabilityResponse;

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            Capability capability = getCapability(capabilityToken, em);
            capabilityResponse = new CapabilitiesResponse(capability, false, true).getResponse();
        } finally {
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponse.Response getCapability(String capabilityTokenID, long callingUserPk)
            throws CapabilityNotFoundException {

        final CapabilitiesResponse.Response capabilityResponse;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final User user = findUserByPk(callingUserPk, em);

        try {
            Capability capability = getCapability(user, capabilityTokenID, em);
            capabilityResponse = new CapabilitiesResponse(capability, false, false).getResponse();
        } finally {
            em.close();
        }
        return capabilityResponse;
    }

    public static boolean capabilityIDExist(String capabilityId) {

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            findCapabilityByCapabilityID(capabilityId, em);
            return true;
        } catch (CapabilityNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
    }

    public static boolean capabilitySecretExist(String capabilitySecret) {

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            getCapability(capabilitySecret, em);
            return true;
        } catch (CapabilityNotFoundException e) {
            return false;
        } finally {
            em.close();
        }
    }

    public static Capability getCapability(User user, String capabilityId, EntityManager em)
            throws CapabilityNotFoundException {

            return findCapabilityByIdandUser(user, capabilityId, em);
    }

    public static Capability getCapability(String secret, EntityManager em)
            throws CapabilityNotFoundException {

            final String hashSecret = hashCapability(secret);
            return findCapabilityByCapabilityToken(hashSecret, em);
    }
}
