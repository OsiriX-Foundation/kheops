package online.kheops.auth_server.util;

import org.jose4j.keys.AesKey;
import org.jose4j.lang.ByteUtil;

import java.security.Key;

public class JweAesKey {
    private static Key key;
    private static JweAesKey instance = null;


    private JweAesKey() {
        key = new AesKey(ByteUtil.randomBytes(16));
    }

    public static synchronized JweAesKey getInstance() {
        if (instance == null) {
            instance = new JweAesKey();
        }
        return instance;
    }

    public Key getKey() {
        return key;
    }
}
