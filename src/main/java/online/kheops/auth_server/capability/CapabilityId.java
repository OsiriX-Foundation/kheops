package online.kheops.auth_server.capability;

import java.security.SecureRandom;
import java.util.Random;

import static online.kheops.auth_server.capability.Capabilities.capabilityIDExist;

public class CapabilityId {

    private String id;

    private static final String ID_DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int ID_LENGTH = 10;
    public static final String ID_PATTERN = "[A-Za-z0-9]{" + ID_LENGTH + "}";

    private static final Random rdm = new SecureRandom();

    public CapabilityId() {
        final StringBuilder idBuilder = new StringBuilder();

        do {
            idBuilder.setLength(0);
            while (idBuilder.length() < ID_LENGTH) {
                int index = rdm.nextInt(ID_DICT.length());
                idBuilder.append(ID_DICT.charAt(index));
            }
        } while (capabilityIDExist(idBuilder.toString()));
        id = idBuilder.toString();
    }

    public String getId() {
        return id;
    }

}
