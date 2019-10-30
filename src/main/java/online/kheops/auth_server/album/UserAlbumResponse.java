package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.AlbumUser;

import javax.xml.bind.annotation.XmlElement;

public class UserAlbumResponse  implements Comparable<UserAlbumResponse> {
    @XmlElement(name = "email")
    private String email;
    @XmlElement(name = "last_name")
    private String lastName;
    @XmlElement(name = "first_name")
    private String firstName;
    @XmlElement(name = "sub")
    private String sub;
    @XmlElement(name = "is_admin")
    private Boolean isAdmin;

    private UserAlbumResponse() { /*empty*/ }

    public UserAlbumResponse(AlbumUser albumUser) {
        email = albumUser.getUser().getEmail();
        firstName = albumUser.getUser().getFirstName();
        lastName = albumUser.getUser().getLastName();
        isAdmin = albumUser.isAdmin();
        sub = albumUser.getUser().getKeycloakId();
    }

    @Override
    public int compareTo(UserAlbumResponse userAlbumResponse) {
        return email.compareTo(userAlbumResponse.email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UserAlbumResponse) {
            final UserAlbumResponse userAlbumResponse = (UserAlbumResponse) obj;
            return  userAlbumResponse.sub.compareTo(sub) == 0 &&
                    userAlbumResponse.isAdmin == isAdmin &&
                    userAlbumResponse.email.compareTo(email) == 0;
        }
        return false;
    }

    private int hashCode;
    @Override
    public int hashCode() {
        int result = hashCode;
        if(result == 0) {
            result = sub.hashCode();
            result = 31 * result + isAdmin.hashCode();
            result = 31 * result + email.hashCode();
            result = 31 * result + lastName.hashCode();
            result = 31 * result + firstName.hashCode();
            hashCode = result;
        }
        return result;
    }
}