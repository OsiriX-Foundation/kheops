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

        if (queryParameters.containsKey(NAME)) {
            name = Optional.ofNullable(queryParameters.get(NAME).get(0));
        } else {
            name = Optional.empty();
        }

        if (queryParameters.containsKey(CREATED_TIME)) {
            createdTime = Optional.ofNullable(queryParameters.get(CREATED_TIME).get(0));
        } else {
            createdTime = Optional.empty();
        }

        if (queryParameters.containsKey(LAST_EVENT_TIME)) {
            lastEventTime = Optional.ofNullable(queryParameters.get(LAST_EVENT_TIME).get(0));
        } else {
            lastEventTime = Optional.empty();
        }

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

        if (queryParameters.containsKey(FAVORITE)) {
            if (queryParameters.get(FAVORITE).get(0).compareTo("true") == 0) {
                favorite = true;
            }
        }

        if (queryParameters.containsKey(QUERY_PARAMETER_FUZZY_MATCHING)) {
            fuzzyMatching = Boolean.parseBoolean(queryParameters.get(QUERY_PARAMETER_FUZZY_MATCHING).get(0));
        }

        if (queryParameters.containsKey(QUERY_PARAMETER_LIMIT)) {
            limit = JOOQTools.getLimit(queryParameters);
        } else {
            limit = OptionalInt.empty();
        }
        if (queryParameters.containsKey(QUERY_PARAMETER_OFFSET)) {
            offset = JOOQTools.getOffset(queryParameters);
        } else {
            offset = OptionalInt.empty();
        }

        DBID = kheopsPrincipal.getDBID();
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