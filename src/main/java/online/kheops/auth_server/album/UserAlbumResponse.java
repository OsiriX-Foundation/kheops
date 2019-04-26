package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.AlbumUser;

import javax.xml.bind.annotation.XmlElement;

public class UserAlbumResponse  implements Comparable<UserAlbumResponse> {
    @XmlElement(name = "user_name")
    private String userName;
    @XmlElement(name = "user_id")
    private String userId;
    @XmlElement(name = "is_admin")
    private Boolean isAdmin;

    private UserAlbumResponse() { /*empty*/ }

    public UserAlbumResponse(AlbumUser albumUser) {
        userName = albumUser.getUser().getEmail();
        isAdmin = albumUser.isAdmin();
        userId = albumUser.getUser().getKeycloakId();
    }

    @Override
    public int compareTo(UserAlbumResponse userAlbumResponse) {
        return userName.compareTo(userAlbumResponse.userName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UserAlbumResponse) {
            final UserAlbumResponse userAlbumResponse = (UserAlbumResponse) obj;
            return  userAlbumResponse.userId.compareTo(userId) == 0 &&
                    userAlbumResponse.isAdmin == isAdmin &&
                    userAlbumResponse.userName.compareTo(userName) == 0;
        }
        return false;
    }

    private int hashCode;
    @Override
    public int hashCode() {
        int result = hashCode;
        if(result == 0) {
            result = userId.hashCode();
            result = 31 * result + isAdmin.hashCode();
            result = 31 * result + userName.hashCode();
            hashCode = result;
        }
        return result;
    }
}