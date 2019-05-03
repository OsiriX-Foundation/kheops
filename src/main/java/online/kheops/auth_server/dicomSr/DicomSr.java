package online.kheops.auth_server.dicomSr;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.capability.CapabilityNotFoundException;
import online.kheops.auth_server.entity.DicomSR;
import online.kheops.auth_server.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Pattern;

import static online.kheops.auth_server.dicomSr.DicomSrQueries.getDicomSrWithClientId;
import static online.kheops.auth_server.dicomSr.DicomSrQueries.getDicomSrWithClientSecret;

public class DicomSr {

    private static final String CLIENT_ID_DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int CLIENT_ID_LENGTH = 22;
    public static final String CLIENT_ID_PATTERN = "[A-Za-z0-9]{" + CLIENT_ID_LENGTH + "}";
//    private static final String CLIENT_ID_PATTERN_STRICT = "^" + CLIENT_ID_PATTERN + "$";
//    private static final Pattern clientIdPattern = Pattern.compile(CLIENT_ID_PATTERN_STRICT);

    private static final String CLIENT_SECRET_DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int CLIENT_SECRET_LENGTH = 32;
//    public static final String CLIENT_SECRET_PATTERN = "[A-Za-z0-9]{" + CLIENT_SECRET_LENGTH + "}";

    private static final Random rdm = new SecureRandom();

    private DicomSr() {
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

    public static DicomSrResponse newDicomSr (User callingUser, String albumId, String name, String url, boolean isPrivate) {
        DicomSR dicomSR;

        //TODO

        return new DicomSrResponse(dicomSR);
    }
}
