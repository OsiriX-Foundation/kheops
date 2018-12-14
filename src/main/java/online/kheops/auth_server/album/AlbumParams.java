package online.kheops.auth_server.album;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.util.JOOQTools;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

import static online.kheops.auth_server.util.Consts.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")

public final class AlbumParams {

    private static final String[] ACCEPTED_VALUES_FOR_SORTING_ARRAY = {"created_time", "last_event_time", "name", "number_of_users", "number_of_studies", "number_of_comments"};
    private static final Set<String> ACCEPTED_VALUES_FOR_SORTING = new HashSet<>(Arrays.asList(ACCEPTED_VALUES_FOR_SORTING_ARRAY));

    private final boolean descending;
    private final String orderBy;

    private boolean fuzzyMatching = false;

    private final OptionalInt limit;
    private final OptionalInt offset;

    private final Optional<String> name;
    private final Optional<String> createdTime;
    private final Optional<String> lastEventTime;

    private boolean favorite = false;

    private long DBID;

    public AlbumParams(KheopsPrincipalInterface kheopsPrincipal, MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {

        name = exctractName(queryParameters);
        createdTime = exctractCreatedTime(queryParameters);
        lastEventTime = exctractLastEventTime(queryParameters);

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

        favorite = exctractFavorite(queryParameters);

        fuzzyMatching = exctractFuzzyMatching(queryParameters);

        limit = exctractLimit(queryParameters);
        offset = exctractOffset(queryParameters);

        DBID = kheopsPrincipal.getDBID();
    }

    private Optional<String> exctractName(MultivaluedMap<String, String> queryParameters) {

        if (queryParameters.containsKey(NAME)) {
            return Optional.ofNullable(queryParameters.get(NAME).get(0));
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> exctractCreatedTime(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(CREATED_TIME)) {
            return Optional.ofNullable(queryParameters.get(CREATED_TIME).get(0));
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> exctractLastEventTime(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(LAST_EVENT_TIME)) {
            return Optional.ofNullable(queryParameters.get(LAST_EVENT_TIME).get(0));
        } else {
            return Optional.empty();
        }
    }

    private OptionalInt exctractLimit(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {
        if (queryParameters.containsKey(QUERY_PARAMETER_LIMIT)) {
            return JOOQTools.getLimit(queryParameters);
        } else {
            return OptionalInt.empty();
        }
    }

    private OptionalInt exctractOffset(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {
        if (queryParameters.containsKey(QUERY_PARAMETER_OFFSET)) {
            return JOOQTools.getOffset(queryParameters);
        } else {
            return OptionalInt.empty();
        }
    }

    private boolean exctractFavorite(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters.containsKey(FAVORITE)) {
            if (queryParameters.get(FAVORITE).get(0).compareTo("true") == 0) {
                return true;
            }
        }
        return false;
    }

    private boolean exctractFuzzyMatching(MultivaluedMap<String, String> queryParameters) {
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

    public long getDBID() { return DBID; }
}