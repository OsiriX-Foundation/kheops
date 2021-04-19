package online.kheops.auth_server.util;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

public class Tools {

    private  Tools() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean checkValidUID(String uid) {
        try {
            new Oid(uid);
            return true;
        } catch (GSSException exception) {
            return false;
        }
    }
}
