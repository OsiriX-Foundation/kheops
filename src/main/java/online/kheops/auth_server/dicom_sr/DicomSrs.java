package online.kheops.auth_server.dicom_sr;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.DicomSr;
import online.kheops.auth_server.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import java.security.SecureRandom;
import java.util.Random;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.dicom_sr.DicomSrQueries.getDicomSrWithClientId;
import static online.kheops.auth_server.dicom_sr.DicomSrQueries.getDicomSrWithClientSecret;

public class DicomSrs {

    private static final String CLIENT_ID_DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int CLIENT_ID_LENGTH = 22;
    public static final String CLIENT_ID_PATTERN = "[A-Za-z0-9]{" + CLIENT_ID_LENGTH + "}";
//    private static final String CLIENT_ID_PATTERN_STRICT = "^" + CLIENT_ID_PATTERN + "$";
//    private static final Pattern clientIdPattern = Pattern.compile(CLIENT_ID_PATTERN_STRICT);

    private static final String CLIENT_SECRET_DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int CLIENT_SECRET_LENGTH = 32;
//    public static final String CLIENT_SECRET_PATTERN = "[A-Za-z0-9]{" + CLIENT_SECRET_LENGTH + "}";

    private static final Random rdm = new SecureRandom();

    private DicomSrs() {
        throw new IllegalStateException("Utility class");
    }

    private static String newClientId() {
        final StringBuilder secretBuilder = new StringBuilder();
        do {
            while (secretBuilder.length() < CLIENT_ID_LENGTH) {
                int index = rdm.nextInt(CLIENT_ID_DICT.length());
                secretBuilder.append(CLIENT_ID_DICT.charAt(index));
            }
        } while (clientIdExist(secretBuilder.toString()));
        return secretBuilder.toString();
    }

    private static String newClientSecret() {
        final StringBuilder idBuilder = new StringBuilder();

        do {
            idBuilder.setLength(0);
            while (idBuilder.length() < CLIENT_SECRET_LENGTH) {
                int index = rdm.nextInt(CLIENT_SECRET_DICT.length());
                idBuilder.append(CLIENT_SECRET_DICT.charAt(index));
            }
        } while (clientSecretExist(idBuilder.toString()));
        return idBuilder.toString();
    }

    public static DicomSrResponse newDicomSr (User callingUser, String albumId, String name, String url, boolean isPrivate)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final DicomSr dicomSr;

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final String clientId = newClientId();

            final Album album = getAlbum(albumId, em);

            final String clientSecret;
            if(isPrivate) {
                clientSecret = newClientSecret();
                dicomSr = new online.kheops.auth_server.entity.DicomSr(clientId, clientSecret, url, name, album, callingUser);
            } else {
                dicomSr = new online.kheops.auth_server.entity.DicomSr(clientId, url, name, album, callingUser);
                //TODO dicomSRBuilder
            }

            em.persist(dicomSr);

            tx.commit();

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return new DicomSrResponse(dicomSr);
    }

    private static boolean clientIdExist(String clientId) {

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            getDicomSrWithClientId(clientId, em);
            return true;
        } catch (NoResultException e) {
            return false;
        } finally {
            em.close();
        }
    }
    private static boolean clientSecretExist(String clientSecret) {

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            getDicomSrWithClientSecret(clientSecret, em);
            return true;
        } catch (NoResultException e) {
            return false;
        } finally {
            em.close();
        }
    }
}
