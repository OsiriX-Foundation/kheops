package online.kheops.auth_server.album;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.JOOQTools;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Optional;

public class AlbumParams {

    private boolean descending = true;
    private String orderBy = "created_time";

    private boolean fuzzyMatching = false;

    private Integer limit = null;
    private Integer offset = null;

    private String name = null;
    private String createdTime = null;
    private String lastEventTime = null;

    private boolean favorite = false;

    private long DBID;

    public AlbumParams(KheopsPrincipalInterface kheopsPrincipal, MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {

        if (queryParameters.containsKey("name")) {
            name = queryParameters.get("name").get(0);
        }

        if (queryParameters.containsKey("created_time")) {
            createdTime = queryParameters.get("created_time").get(0);
        }

        if (queryParameters.containsKey("last_event_time")) {
            lastEventTime = queryParameters.get("last_event_time").get(0);
        }

        if (queryParameters.containsKey("sort")) {
            descending = queryParameters.get("sort").get(0).startsWith("-");
            lastEventTime = queryParameters.get("sort").get(0).replace("-", "");
        }

        if (queryParameters.containsKey("favorite")) {
            if (queryParameters.get("favorite").get(0).compareTo("true") == 0) {
                favorite = true;
            }
        }

        if (queryParameters.containsKey("fuzzyMatching")) {
            fuzzyMatching = Boolean.parseBoolean(queryParameters.get("fuzzyMatching").get(0));
        }

        if (queryParameters.containsKey(Consts.QUERY_PARAMETER_LIMIT)) {
            limit = JOOQTools.getLimit(queryParameters);
        }
        if (queryParameters.containsKey(Consts.QUERY_PARAMETER_OFFSET)) {
            offset = JOOQTools.getOffset(queryParameters);
        }

        DBID = kheopsPrincipal.getDBID();
    }

    public boolean isDescending() {
        return descending;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public boolean isFuzzyMatching() {
        return fuzzyMatching;
    }

    public Optional<Integer> getLimit() {
        return Optional.ofNullable(limit);
    }

    public Optional<Integer> getOffset() {
        return Optional.ofNullable(offset);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getCreatedTime() {
        return Optional.ofNullable(createdTime);
    }

    public Optional<String> getLastEventTime() {
        return Optional.ofNullable(lastEventTime);
    }

    public boolean isFavorite() {
        return favorite;
    }

    public long getDBID() {
        return DBID;
    }
}
