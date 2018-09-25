package online.kheops.auth_server.util;

import online.kheops.auth_server.album.BadQueryParametersException;
import org.jooq.Condition;
import org.jooq.Field;

import javax.ws.rs.core.MultivaluedMap;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JOOQTools {

    private JOOQTools() { throw new IllegalStateException("Utility class"); }

    public static int getLimit(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException {
        Integer limit;
        try {
            limit = Integer.parseInt(queryParameters.get(Consts.QUERY_PARAMETER_LIMIT).get(0));
        } catch (Exception e) {
            throw new BadQueryParametersException(Consts.QUERY_PARAMETER_LIMIT + ": " + queryParameters.get(Consts.QUERY_PARAMETER_LIMIT).get(0));
        }
        if (limit < 1) {
            throw new BadQueryParametersException(Consts.QUERY_PARAMETER_LIMIT + ": " + queryParameters.get(Consts.QUERY_PARAMETER_LIMIT).get(0));
        }
        return limit;
    }

    public static int getOffset(MultivaluedMap<String, String> queryParameters) throws BadQueryParametersException{
        Integer offset;
        try {
            offset = Integer.parseInt(queryParameters.get(Consts.QUERY_PARAMETER_OFFSET).get(0));
        } catch (Exception e) {
            throw new BadQueryParametersException(Consts.QUERY_PARAMETER_OFFSET + ": " + queryParameters.get(Consts.QUERY_PARAMETER_OFFSET).get(0));
        }

        if (offset < 0) {
            throw new BadQueryParametersException(Consts.QUERY_PARAMETER_OFFSET + ": " + queryParameters.get(Consts.QUERY_PARAMETER_OFFSET).get(0));
        }
        return offset;
    }

    public static Boolean isFuzzyMatching(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {
        if (queryParameters.containsKey(Consts.QUERY_PARAMETER_FUZZY_MATCHING)) {
            if (queryParameters.get(Consts.QUERY_PARAMETER_FUZZY_MATCHING).get(0).compareTo("true") == 0) {
                return true;
            } else if (queryParameters.get(Consts.QUERY_PARAMETER_FUZZY_MATCHING).get(0).compareTo("false") == 0) {
                return false;
            } else {
                throw new BadQueryParametersException(Consts.QUERY_PARAMETER_FUZZY_MATCHING + ": " + queryParameters.get(Consts.QUERY_PARAMETER_FUZZY_MATCHING).get(0));
            }
        } else {
            return false;
        }
    }

    public static void checkDate (String date) throws BadQueryParametersException {
        if (!date.matches("^([0-9]{4})(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$")) {
            throw new BadQueryParametersException("Bad date format : yyyyMMdd");
        }
    }

    public static Condition createDateCondition(MultivaluedMap<String, String> queryParameters, String key, Field<Timestamp> column)
            throws BadQueryParametersException {
        if (queryParameters.containsKey(key)) {
            String parameter = queryParameters.get(key).get(0);


            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
            if (parameter.contains("-")) {
                String[] parameters = parameter.split("-");
                if (parameters.length == 2) {
                    try {
                        if(parameters[0].length() == 0) {
                            parameters[0] = "00010101";
                        }
                        checkDate(parameters[0]);
                        checkDate(parameters[1]);

                        Date parsedDateBegin = dateFormat.parse(parameters[0]+"000000");
                        Timestamp begin = new java.sql.Timestamp(parsedDateBegin.getTime());
                        Date parsedDateEnd = dateFormat.parse(parameters[1] + "235959");
                        Timestamp end = new java.sql.Timestamp(parsedDateEnd.getTime());
                        return column.between(begin, end);
                    } catch (ParseException | BadQueryParametersException e) {
                        throw new BadQueryParametersException(key + ": " + parameter);
                    }
                } else if(parameters.length == 1) {
                    try {
                    checkDate(parameters[0]);
                    Date parsedDateBegin = dateFormat.parse(parameters[0]+"000000");
                    Timestamp begin = new java.sql.Timestamp(parsedDateBegin.getTime());
                    Date parsedDateEnd = dateFormat.parse("99991231235959");
                    Timestamp end = new java.sql.Timestamp(parsedDateEnd.getTime());
                    return column.between(begin, end);
                    } catch (ParseException | BadQueryParametersException e) {
                        throw new BadQueryParametersException(key + ": " + parameter);
                    }
                } else {
                    throw new BadQueryParametersException(key + ": " + parameter);
                }
            } else {
                try {
                    checkDate(parameter);
                    Date parsedDateBegin = dateFormat.parse(parameter+"000000");
                    Timestamp begin = new java.sql.Timestamp(parsedDateBegin.getTime());
                    Date parsedDateEnd = dateFormat.parse(parameter+"235959");
                    Timestamp end = new java.sql.Timestamp(parsedDateEnd.getTime());
                    return column.between(begin, end);
                } catch (ParseException | BadQueryParametersException e) {
                    throw new BadQueryParametersException(key + ": " + parameter);
                }
            }
        }
        return null;
    }
}
