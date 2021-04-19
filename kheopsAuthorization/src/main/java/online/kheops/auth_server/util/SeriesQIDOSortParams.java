package online.kheops.auth_server.util;

import online.kheops.auth_server.album.BadQueryParametersException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static online.kheops.auth_server.util.Consts.QUERY_PARAMETER_SORT;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;

public final class SeriesQIDOSortParams {
    private static final Set<Integer> ACCEPTED_TAGS_FOR_SORTING = new HashSet<>(Arrays.asList(Tag.Modality, Tag.SeriesInstanceUID, Tag.SeriesNumber, Tag.NumberOfSeriesRelatedInstances));

    private SeriesQIDOSortParams() {}

    public static Comparator<Attributes> sortComparator(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException {

        boolean descending = false;
        int orderByTag = Tag.SeriesNumber;

        if (queryParameters.containsKey(QUERY_PARAMETER_SORT)) {
            descending = queryParameters.get(QUERY_PARAMETER_SORT).get(0).startsWith("-");
            final String orderByParameter = queryParameters.get(QUERY_PARAMETER_SORT).get(0).replace("-", "");
            orderByTag = org.dcm4che3.util.TagUtils.forName(orderByParameter);
            if (orderByTag == -1 || !ACCEPTED_TAGS_FOR_SORTING.contains(orderByTag)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("'sort' query parameter is bad")
                        .build();
                throw new BadQueryParametersException(errorResponse);
            }
        }

        Comparator<Attributes> comparator;
        switch (orderByTag) {
            case Tag.Modality:
            case Tag.SeriesInstanceUID:
                comparator = getStringAttributesComparator(orderByTag);
                break;
            case Tag.SeriesNumber:
            case Tag.NumberOfSeriesRelatedInstances:
                comparator = getIntAttributesComparator(orderByTag);
                break;
            default:
                comparator = getIntAttributesComparator(Tag.SeriesNumber);
        }

        if (descending) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    private static Comparator<Attributes> getIntAttributesComparator(int tag) {
        return Comparator.comparingInt(value -> value.getInt(tag, 0));
    }

    private static Comparator<Attributes> getStringAttributesComparator(int tag) {
        return Comparator.comparing(value -> value.getString(tag, ""));
    }
}


