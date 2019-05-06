package online.kheops.auth_server.dicom_sr;


import online.kheops.auth_server.entity.DicomSr;
import online.kheops.auth_server.user.UserResponse;
import online.kheops.auth_server.user.UserResponseBuilder;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

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

    protected DicomSrResponse(DicomSr dicomSr) {
        name = dicomSr.getName();
        url = dicomSr.getUrl();
        clientId = dicomSr.getClientId();
        isPrivate = dicomSr.isPrivate();
        if(dicomSr.isPrivate()) {
            clientSecret = dicomSr.getClientSecret();
        }
        final UserResponseBuilder userResponseBuilder = new UserResponseBuilder();
        user = userResponseBuilder.setSub(dicomSr.getUser().getKeycloakId()).setEmail(dicomSr.getUser().getEmail()).build();

        createdTime = dicomSr.getCreationTime();
    }
}
