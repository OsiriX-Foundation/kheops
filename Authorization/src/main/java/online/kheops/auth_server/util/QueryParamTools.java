package online.kheops.auth_server.util;

import online.kheops.auth_server.album.BadQueryParametersException;

import javax.ws.rs.core.MultivaluedMap;
import java.util.OptionalInt;

import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;

public interface QueryParamTools {

    public static OptionalInt getLimit(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException {
        final Integer limit;
        try {
            limit = Integer.parseInt(queryParameters.get(Consts.QUERY_PARAMETER_LIMIT).get(0));
        } catch (NumberFormatException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("'" + Consts.QUERY_PARAMETER_LIMIT + "' must be an integer >= 1")
                    .build();
            throw new BadQueryParametersException(errorResponse);
        }
        if (limit < 1) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("'" + Consts.QUERY_PARAMETER_LIMIT + "' must be an integer >= 1")
                    .build();
            throw new BadQueryParametersException(errorResponse);
        }
        return OptionalInt.of(limit);
    }

    public static OptionalInt getOffset(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException{
        final Integer offset;
        try {
            offset = Integer.parseInt(queryParameters.get(Consts.QUERY_PARAMETER_OFFSET).get(0));
        } catch (Exception e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("'" + Consts.QUERY_PARAMETER_OFFSET + "' must be an integer >= 1")
                    .build();
            throw new BadQueryParametersException(errorResponse);
        }

        if (offset < 0) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("'" + Consts.QUERY_PARAMETER_OFFSET + "' must be an integer >= 0")
                    .build();
            throw new BadQueryParametersException(errorResponse);
        }
        return OptionalInt.of(offset);
    }

    public static void checkDate(String date) throws BadQueryParametersException {
        if (!date.matches("^([0-9]{4})(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$")) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Bad date format : yyyyMMdd")
                    .build();
            throw new BadQueryParametersException(errorResponse);
        }
    }
}
