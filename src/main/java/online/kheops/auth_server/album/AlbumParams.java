package online.kheops.auth_server.album;

import online.kheops.auth_server.KheopsPrincipalInterface;

import javax.ws.rs.core.MultivaluedMap;

public class AlbumParams {

    private boolean descending = true;
    private int orderByTag;

    private boolean fuzzyMatching = false;

    private Integer limit = null;
    private Integer offset = null;

    private String name;
    private String createdTime;
    private String lastEventTime;

    public AlbumParams(KheopsPrincipalInterface kheopsPrincipal, MultivaluedMap<String, String> queryParameters) {


    }
}
