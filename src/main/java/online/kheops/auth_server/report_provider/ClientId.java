package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.EntityManagerListener;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import java.security.SecureRandom;
import java.util.Random;

import static online.kheops.auth_server.report_provider.ReportProviderQueries.getReportProviderWithClientId;

public class ClientId {

    private static final String CLIENT_ID_DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int CLIENT_ID_LENGTH = 22;
    public static final String CLIENT_ID_PATTERN = "[A-Za-z0-9]{" + CLIENT_ID_LENGTH + "}";

    private String id;

    private static final Random rdm = new SecureRandom();

    public ClientId() {
        final StringBuilder secretBuilder = new StringBuilder();
        do {
            while (secretBuilder.length() < CLIENT_ID_LENGTH) {
                int index = rdm.nextInt(CLIENT_ID_DICT.length());
                secretBuilder.append(CLIENT_ID_DICT.charAt(index));
            }
        } while (clientIdExist(secretBuilder.toString()));
        id = secretBuilder.toString();
    }


    private static boolean clientIdExist(String clientId) {

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            getReportProviderWithClientId(clientId, em);
            return true;
        } catch (NoResultException e) {
            return false;
        } finally {
            em.close();
        }
    }

    public String getClientId() { return id; }
}
