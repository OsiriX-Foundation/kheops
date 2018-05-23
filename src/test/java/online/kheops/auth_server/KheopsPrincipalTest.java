package online.kheops.auth_server;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KheopsPrincipalTest {

    private final static long principalDBID = 123456789L;
    private final static String principalName = "123456789";

    private KheopsPrincipal kheopsPrincipal;

    @BeforeAll
    void beforeAll() {
        kheopsPrincipal = new KheopsPrincipal(principalDBID);
    }

    @Test
    void getDBID() {
        assertEquals(principalDBID, kheopsPrincipal.getDBID());
    }

    @Test
    void getName() {
        assertEquals(principalName, kheopsPrincipal.getName());
    }

}