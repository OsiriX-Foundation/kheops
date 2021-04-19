package online.kheops.auth_server.util;

import online.kheops.auth_server.album.BadQueryParametersException;
import org.jooq.Condition;
import org.jooq.Field;

import javax.ws.rs.core.MultivaluedMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.OptionalInt;

import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;

public abstract class JOOQTools {

    private JOOQTools() { throw new IllegalStateException("Utility class"); }

    public static OptionalInt getLimit(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException {
        final Integer limit;
        try {
            limit = Integer.parseInt(queryParameters.get(Consts.QUERY_PARAMETER_LIMIT).get(0));
        } catch (Exception e) {
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

    public static Condition createDateCondition(String parameter, Field<LocalDateTime> column)
            throws BadQueryParametersException {

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .message(BAD_QUERY_PARAMETER)
                .detail("Bad date format : yyyyMMdd")
                .build();

        if (parameter.contains("-")) {
            String[] parameters = parameter.split("-");
            if (parameters.length == 2) { //case yyyyMMdd-yyyyMMdd
                if(parameters[0].length() == 0) {//case -yyyyMMdd = all before yyyyMMdd
                    parameters[0] = "00010101";
                }
                checkDate(parameters[0]);
                checkDate(parameters[1]);

                final String strDateBegin = parameters[0]+"000000";
                final LocalDateTime begin = LocalDateTime.parse(strDateBegin, formatter);
                final String strDateEnd = parameters[1] + "235959";
                final LocalDateTime end = LocalDateTime.parse(strDateEnd, formatter);
                return column.between(begin, end);
            } else if(parameters.length == 1) { //case yyyyMMdd- = all after yyyyMMdd
                checkDate(parameters[0]);

                final String strDateBegin = parameters[0]+"000000";
                final LocalDateTime begin = LocalDateTime.parse(strDateBegin, formatter);
                final String strDateEnd = "99991231235959";
                final LocalDateTime end = LocalDateTime.parse(strDateEnd, formatter);
                return column.between(begin, end);
            } else {
                throw new BadQueryParametersException(errorResponse);
            }
        } else {  //case only at this date yyyyMMdd
            checkDate(parameter);
            final String strDateBegin = parameter+"000000";
            final LocalDateTime begin = LocalDateTime.parse(strDateBegin, formatter);
            final String strDateEnd = parameter+"235959";
            final LocalDateTime end = LocalDateTime.parse(strDateEnd, formatter);
            return column.between(begin, end);
        }
    }
}
