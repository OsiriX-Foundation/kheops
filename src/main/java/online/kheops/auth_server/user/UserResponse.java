package online.kheops.auth_server.user;

import online.kheops.auth_server.capability.CapabilitiesResponse;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.report_provider.ReportProviderResponse;

import javax.xml.bind.annotation.XmlElement;

public class UserResponse  implements Comparable<UserResponse> {

    //Mandatory
    @XmlElement(name = "email")
    private String email;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "sub")
    private String sub;

    //For event
    @XmlElement(name = "can_access")
    private Boolean canAccess;

    //For users in album
    @XmlElement(name = "is_admin")
    private Boolean isAdmin;

    //For users
    @XmlElement(name = "study_access")
    private Boolean studyAccess;
    @XmlElement(name = "album_access")
    private Boolean albumAccess;

    //For webhook new series
    @XmlElement(name = "report_provider")
    private ReportProviderResponse reportProvider;
    @XmlElement(name = "capability_token")
    private CapabilitiesResponse capability;

    private UserResponse() { /*empty*/ }

    public UserResponse(AlbumUser albumUser) {
        email = albumUser.getUser().getEmail();
        isAdmin = albumUser.isAdmin();
        sub = albumUser.getUser().getSub();
        name = albumUser.getUser().getName();
    }

    public UserResponse(User user) {
        email = user.getEmail();
        sub = user.getSub();
        name = user.getName();
    }

    protected UserResponse(UserResponseBuilder userResponseBuilder) {
        email = userResponseBuilder.getEmail();
        sub = userResponseBuilder.getSub();
        name = userResponseBuilder.getName();

        albumAccess = userResponseBuilder.getAlbumAccess();
        studyAccess = userResponseBuilder.getStudyAccess();
        userResponseBuilder.getCanAccess().ifPresent(value -> canAccess = value);
    }

    public void setReportProvider(ReportProvider reportProvider, ReportProviderResponse.Type type) {
        this.reportProvider = new ReportProviderResponse(reportProvider, type);
    }

    public void setCapabilityToken(Capability capability) {
        this.capability = new CapabilitiesResponse(capability);
    }

    public String getSub() { return sub; }

    @Override
    public int compareTo(UserResponse userResponse) {
       int res = email.compareTo(userResponse.email);
       if (res == 0) {
           res = sub.compareTo(userResponse.sub);
           if (res == 0) {
               res = name.compareTo(userResponse.name);
           }
       }
       return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UserResponse) {
            final UserResponse userAlbumResponse = (UserResponse) obj;
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
            hashCode = result;
        }
        return result;
    }
}