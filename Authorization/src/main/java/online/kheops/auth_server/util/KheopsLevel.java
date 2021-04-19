package online.kheops.auth_server.util;

import java.util.logging.Level;

public class KheopsLevel extends Level {

    public static final Level KHEOPS = new KheopsLevel("KHEOPS", Level.INFO.intValue());

    public KheopsLevel(String name, int value) {
        super(name, value);
    }

}
