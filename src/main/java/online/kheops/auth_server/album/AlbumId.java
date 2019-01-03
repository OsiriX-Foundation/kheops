package online.kheops.auth_server.album;

import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Pattern;

import static online.kheops.auth_server.album.Albums.albumExist;

public class AlbumId {

    private String id;

    private static final String DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int ID_LENGTH = 10;
    public static final String ID_PATTERN = "[A-Za-z0-9]{" + ID_LENGTH + "}";
    private static final String ID_PATTERN_STRICT = "^" + ID_PATTERN + "$";
    private static final Pattern pattern = Pattern.compile(ID_PATTERN_STRICT);
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

    public AlbumId(String id) throws AlbumIdInvalidFormatException {

        if (isValidFormat(id)) {
            this.id = id;
        } else {
            throw new AlbumIdInvalidFormatException();
        }
    }

    public boolean isValidFormat(String id) {
        return pattern.matcher(id).matches();
    }

    public String getId() {
        return id;
    }
}
