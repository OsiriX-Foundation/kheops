package online.kheops.auth_server;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public abstract class PersistenceUtils {

    private PersistenceUtils() {}

    private static String user;
    private static String password;
    private static String url;

    public static void setUser(String user) {
        PersistenceUtils.user = user;
    }

    public static void setPassword(String password) {
        PersistenceUtils.password = password;
    }

    public static void setUrl(String url) {
        PersistenceUtils.url = url;
    }

    public static EntityManagerFactory createEntityManagerFactory() {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.user", user);
        properties.put("javax.persistence.jdbc.password", password);
        properties.put("javax.persistence.jdbc.url", url + "?useUnicode=yes&amp;characterEncoding=UTF-8");

        return Persistence.createEntityManagerFactory("online.kheops", properties);
    }
}
