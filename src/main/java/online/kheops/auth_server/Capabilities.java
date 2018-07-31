package online.kheops.auth_server;

import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Pattern;

public class Capabilities {
    private final static String DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private final static int TOKEN_LENGTH = 22;
    private final static Pattern pattern = Pattern.compile("^[A-Za-z0-9]{" + TOKEN_LENGTH + "}$");
    private static final Random rdm = new SecureRandom();

    public static String newCapabilityToken() {
        StringBuilder secretBuilder = new StringBuilder();

        while (secretBuilder.length() < TOKEN_LENGTH) {
            int index = rdm.nextInt(DICT.length());
            secretBuilder.append(DICT.charAt(index));
        }
        return secretBuilder.toString();
    }

    public static Boolean isValidFormat(String token) {
        if (pattern.matcher(token).matches()) {
            return true;
        } else {
            return false;
        }
    }
}
