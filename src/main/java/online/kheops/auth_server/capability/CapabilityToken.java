package online.kheops.auth_server.capability;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Pattern;

import static online.kheops.auth_server.capability.Capabilities.capabilitySecretExist;

public class CapabilityToken {

    private String token;

    private static final String TOKEN_DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int TOKEN_LENGTH = 22;
    public static final String TOKEN_PATTERN = "[A-Za-z0-9]{" + TOKEN_LENGTH + "}";
    private static final String TOKEN_PATTERN_STRICT = "^" + TOKEN_PATTERN + "$";
    private static final Pattern tokenPattern = Pattern.compile(TOKEN_PATTERN_STRICT);

    private static final Random rdm = new SecureRandom();

    public CapabilityToken() {
        final StringBuilder secretBuilder = new StringBuilder();
        do {
            while (secretBuilder.length() < TOKEN_LENGTH) {
                int index = rdm.nextInt(TOKEN_DICT.length());
                secretBuilder.append(TOKEN_DICT.charAt(index));
            }
        } while (capabilitySecretExist(secretBuilder.toString()));
        token = secretBuilder.toString();
    }

    public String getToken() { return token; }

    public static Boolean isValidFormat(String token) {
        return tokenPattern.matcher(token).matches();
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

}
