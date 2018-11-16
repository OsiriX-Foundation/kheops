package online.kheops.auth_server.user;

import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.User;
import org.jooq.Record;

import javax.xml.bind.annotation.XmlElement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class UserResponses {

    private UserResponses() {
        throw new IllegalStateException("Utility class");
    }

    public static class UserResponse {
        @XmlElement(name = "email")
        public String email;
        @XmlElement(name = "sub")
        public String sub;

    }


    public static UserResponse userToUserResponse(User user) {
        final UserResponse userResponse = new UserResponse();

        userResponse.email = user.getGoogleEmail();
        userResponse.sub = user.getGoogleId();

        return userResponse;
    }

}
