package online.kheops.auth_server.capability;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.getAlbumUser;
import static online.kheops.auth_server.capability.CapabilitiesQueries.*;
import static online.kheops.auth_server.capability.CapabilitiesQueries.findAllCapabilitiesByAlbum;
import static online.kheops.auth_server.capability.CapabilityToken.hashCapability;
import static online.kheops.auth_server.user.UserQueries.findUserByPk;

public class Capabilities {

    private Capabilities() {
        throw new IllegalStateException("Utility class");
    }

    public static CapabilitiesResponse generateCapability(CapabilityParameters capabilityParameters)
            throws UserNotFoundException, AlbumNotFoundException, NewCapabilityForbidden , CapabilityBadRequestException, UserNotMemberException {
        return capabilityParameters.getScopeType().generateCapability(capabilityParameters);
    }

    public static CapabilitiesResponse createUserCapability(CapabilityParameters capabilityParameters)
            throws CapabilityBadRequestException {

        final CapabilitiesResponse capabilityResponse;

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

            capabilityResponse = new CapabilitiesResponse(capability, true, false);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponse createAlbumCapability(CapabilityParameters capabilityParameters)
            throws AlbumNotFoundException, NewCapabilityForbidden, CapabilityBadRequestException, UserNotMemberException {

        final CapabilitiesResponse capabilityResponse;

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

            capabilityResponse = new CapabilitiesResponse(capability, true, false);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return capabilityResponse;
    }
    
    public static CapabilitiesResponse revokeCapability(User callingUser, String capabilityId)
            throws CapabilityNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        final CapabilitiesResponse capabilityResponse;

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Capability capability = getCapability(callingUser, capabilityId, em);

            capability.setRevoked(true);
            em.persist(capability);

            capabilityResponse = new CapabilitiesResponse(capability, false, false);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return  capabilityResponse;
    }

    public static PairListXTotalCount<CapabilitiesResponse> getCapabilities(User callingUser, boolean valid, Integer limit, Integer offset) {

        final List<CapabilitiesResponse> capabilityResponses = new ArrayList<>();
        final long totalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            callingUser = em.merge(callingUser);

            final List<Capability> capabilities;
            if(valid) {
                capabilities = findCapabilitiesByUserValidOnly(callingUser, limit, offset, em);
                totalCount = countCapabilitiesByUserValidOnly(callingUser, em);
            } else {
                capabilities = findAllCapabilitiesByUser(callingUser, limit, offset, em);
                totalCount = countAllCapabilitiesByUser(callingUser, em);
            }

            for (Capability capability: capabilities) {
                capabilityResponses.add(new CapabilitiesResponse(capability, false, false));
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return new PairListXTotalCount<>(totalCount, capabilityResponses);
    }

    public static PairListXTotalCount<CapabilitiesResponse> getCapabilities(String albumId, boolean valid, Integer limit, Integer offset) {

        final List<CapabilitiesResponse> capabilityResponses = new ArrayList<>();
        final long totalCount;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            final List<Capability> capabilities;
            if(valid) {
                capabilities = findCapabilitiesByAlbumValidOnly(albumId, limit, offset, em);
                totalCount = countCapabilitiesByAlbumValidOnly(albumId, em);
            } else {
                capabilities = findAllCapabilitiesByAlbum(albumId, limit, offset, em);
                totalCount = countAllCapabilitiesByAlbum(albumId, em);
            }

            for (Capability capability: capabilities) {
                capabilityResponses.add(new CapabilitiesResponse(capability, false, false));
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return new PairListXTotalCount<>(totalCount, capabilityResponses);

    }

    public static CapabilitiesResponse getCapabilityInfo(String capabilityToken)
            throws CapabilityNotFoundException {

        final CapabilitiesResponse capabilityResponse;

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            Capability capability = getCapability(capabilityToken, em);
            capabilityResponse = new CapabilitiesResponse(capability, false, true);
        } finally {
            em.close();
        }
        return capabilityResponse;
    }

    public static CapabilitiesResponse getCapability(String capabilityTokenID, long callingUserPk)
            throws CapabilityNotFoundException {

        final CapabilitiesResponse capabilityResponse;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final User user = findUserByPk(callingUserPk, em);

        try {
            Capability capability = getCapability(user, capabilityTokenID, em);
            capabilityResponse = new CapabilitiesResponse(capability, false, false);
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
