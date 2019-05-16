package online.kheops.auth_server.album;

import java.security.SecureRandom;
import java.util.Random;

import static online.kheops.auth_server.album.Albums.albumExist;

public class AlbumId {

    private String id;

    private static final String DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int ID_LENGTH = 10;
    public static final String ID_PATTERN = "[A-Za-z0-9]{" + ID_LENGTH + "}";
    private static final Random rdm = new SecureRandom();

    public AlbumId() {
        StringBuilder idBuilder = new StringBuilder();

        do {
            idBuilder.setLength(0);
            while (idBuilder.length() < ID_LENGTH) {
                int index = rdm.nextInt(DICT.length());
                idBuilder.append(DICT.charAt(index));
            }
        } while (albumExist(idBuilder.toString()));
        id = idBuilder.toString();
    }

    public String getId() {
        return id;
    }
}
