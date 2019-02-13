package online.kheops.auth_server.album;

import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.util.JOOQTools;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

import static online.kheops.auth_server.util.Consts.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")

public final class AlbumQueryParams {

    private static final String[] ACCEPTED_VALUES_FOR_SORTING_ARRAY = {"created_time", "last_event_time", "name", "number_of_users", "number_of_studies", "number_of_comments"};
    private static final Set<String> ACCEPTED_VALUES_FOR_SORTING = new HashSet<>(Arrays.asList(ACCEPTED_VALUES_FOR_SORTING_ARRAY));

    private final boolean descending;
    private final String orderBy;

    private final boolean canAddSeries; //add permission or admin
    private final boolean canCreateCapabilityToken; //create a new capability token

    private boolean fuzzyMatching;

    private final OptionalInt limit;
    private final OptionalInt offset;

    private final Optional<String> name;
    private final Optional<String> createdTime;
    private final Optional<String> lastEventTime;

    private boolean favorite = false;

    private long DBID;

    public AlbumQueryParams(KheopsPrincipalInterface kheopsPrincipal, MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {

        name = extractName(queryParameters);
        createdTime = extractCreatedTime(queryParameters);
        lastEventTime = extractLastEventTime(queryParameters);

        canAddSeries = extractCanAddSeries(queryParameters);
        canCreateCapabilityToken = extractCanCreateCapabilityToken(queryParameters);

        if (queryParameters.containsKey(QUERY_PARAMETER_SORT)) {
            descending = queryParameters.get(QUERY_PARAMETER_SORT).get(0).startsWith("-");
            orderBy = queryParameters.get(QUERY_PARAMETER_SORT).get(0).replace("-", "");
            if (!ACCEPTED_VALUES_FOR_SORTING.contains(orderBy)) {
                throw new BadQueryParametersException("sort: " + orderBy);
            }
        } else {
            descending = true;
            orderBy = CREATED_TIME;
        }

        favorite = extractFavorite(queryParameters);

        fuzzyMatching = extractFuzzyMatching(queryParameters);

        limit = extractLimit(queryParameters);
        offset = extractOffset(queryParameters);

        DBID = kheopsPrincipal.getDBID();
    }

    private Optional<String> extractName(MultivaluedMap<String, String> queryParameters) {

        if (queryParameters.containsKey(NAME)) {
            return Optional.ofNullable(queryParameters.get(NAME).get(0));
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> extractCreatedTime(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(CREATED_TIME)) {
            return Optional.ofNullable(queryParameters.get(CREATED_TIME).get(0));
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> extractLastEventTime(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(LAST_EVENT_TIME)) {
            return Optional.ofNullable(queryParameters.get(LAST_EVENT_TIME).get(0));
        } else {
            return Optional.empty();
        }
    }

    private OptionalInt extractLimit(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {
        if (queryParameters.containsKey(QUERY_PARAMETER_LIMIT)) {
            return JOOQTools.getLimit(queryParameters);
        } else {
            return OptionalInt.empty();
        }
    }

    private OptionalInt extractOffset(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {
        if (queryParameters.containsKey(QUERY_PARAMETER_OFFSET)) {
            return JOOQTools.getOffset(queryParameters);
        } else {
            return OptionalInt.empty();
        }
    }

    private boolean extractFavorite(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(FAVORITE)) {
            return Boolean.valueOf(queryParameters.get(FAVORITE).get(0));
        }
        return false;
    }

    private boolean extractCanAddSeries(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey("canAddSeries")) {
            return Boolean.valueOf(queryParameters.get("canAddSeries").get(0));
        }
        return false;
    }

    private boolean extractCanCreateCapabilityToken(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey("canCreateCapabilityToken")) {
            return Boolean.valueOf(queryParameters.get("canCreateCapabilityToken").get(0));
        }
        return false;
    }

    private boolean extractFuzzyMatching(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(QUERY_PARAMETER_FUZZY_MATCHING)) {
            return Boolean.parseBoolean(queryParameters.get(QUERY_PARAMETER_FUZZY_MATCHING).get(0));
        }
        return false;
    }


    public boolean isDescending() { return descending; }

    public String getOrderBy() { return orderBy; }

    public boolean isFuzzyMatching() { return fuzzyMatching; }

    public OptionalInt getLimit() { return limit; }

    public OptionalInt getOffset() { return offset; }

    public Optional<String> getName() { return name; }

    public Optional<String> getCreatedTime() { return createdTime; }

    public Optional<String> getLastEventTime() { return lastEventTime; }

    public boolean isFavorite() { return favorite; }

    public boolean canAddSeries() { return canAddSeries; }

    public boolean canCreateCapabilityToken() { return canCreateCapabilityToken; }

    public long getDBID() { return DBID; }
}