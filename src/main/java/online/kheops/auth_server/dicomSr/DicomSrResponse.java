package online.kheops.auth_server.dicomSr;

import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import online.kheops.auth_server.album.AlbumResponseBuilder;
import online.kheops.auth_server.album.UserAlbumResponse;
import online.kheops.auth_server.entity.DicomSR;
import online.kheops.auth_server.user.UserResponse;
import online.kheops.auth_server.user.UserResponseBuilder;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.List;

public class DicomSrResponse {

    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "url")
    private String url;
    @XmlElement(name = "client_id")
    private String clientId;
    @XmlElement(name = "client_secret")
    private String clientSecret;
    @XmlElement(name = "isPrivate")
    private boolean isPrivate;
    @XmlElement(name = "user")
    private UserResponse user;
    @XmlElement(name = "created_time")
    private LocalDateTime createdTime;

    private DicomSrResponse() { /*empty*/ }

    protected DicomSrResponse(DicomSR dicomSR) {
        name = dicomSR.getName();
        url = dicomSR.getUrl();
        clientId = dicomSR.getClientId();
        isPrivate = dicomSR.isPrivate();
        if(dicomSR.isPrivate()) {
            clientSecret = dicomSR.getClientSecret();
        }
        final UserResponseBuilder userResponseBuilder = new UserResponseBuilder();
        user = userResponseBuilder.setSub(dicomSR.getUser().getKeycloakId()).setEmail(dicomSR.getUser().getEmail()).build();

        createdTime = dicomSR.getCreationTime();
    }
}
