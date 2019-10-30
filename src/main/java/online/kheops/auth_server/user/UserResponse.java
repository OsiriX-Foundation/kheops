package online.kheops.auth_server.user;

import javax.xml.bind.annotation.XmlElement;


public class UserResponse {

    @XmlElement(name = "email")
    private String email;
    @XmlElement(name = "sub")
    private String sub;
    @XmlElement(name = "last_name")
    private String lastName;
    @XmlElement(name = "first_name")
    private String firstName;


    @XmlElement(name = "study_access")
    private Boolean studyAccess;
    @XmlElement(name = "album_access")
    private Boolean albumAccess;

    private UserResponse() { /*empty*/ }

    protected UserResponse(UserResponseBuilder userResponseBuilder) {
        email = userResponseBuilder.getEmail();
        sub = userResponseBuilder.getSub();
        firstName = userResponseBuilder.getFirstName();
        lastName = userResponseBuilder.getLastName();

        albumAccess = userResponseBuilder.getAlbumAccess();
        studyAccess = userResponseBuilder.getStudyAccess();
    }
}


