package org.hcjf.layers.query.functions;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.properties.SystemProperties;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author javaito
 */
public class DateQueryFunctionLayer extends BaseQueryFunctionLayer implements QueryFunctionLayerInterface {

    private static final String NOW = "now";
    private static final String GET_YEAR = "getYear";
    private static final String GET_MONTH = "getMonth";
    private static final String GET_MONTH_NUMBER = "getMonthNumber";
    private static final String GET_DAY_OF_MONTH = "getDayOfMonth";
    private static final String GET_DAY_OF_WEEK = "getDayOfWeek";
    private static final String GET_DAY_OF_YEAR = "getDayOfYear";
    private static final String GET_HOUR = "getHour";
    private static final String GET_MINUTE = "getMinute";
    private static final String GET_SECOND = "getSecond";
    private static final String GET_MILLISECOND_UNIX_EPOCH = "getMillisecondUnixEpoch";
    private static final String GET_NANO = "getNano";
    private static final String PLUS_YEARS = "plusYears";
    private static final String PLUS_MONTHS = "plusMonths";
    private static final String PLUS_DAYS = "plusDays";
    private static final String PLUS_HOURS = "plusHours";
    private static final String PLUS_MINUTES = "plusMinutes";
    private static final String PLUS_SECONDS = "plusSeconds";
    private static final String MINUS_YEARS = "minusYears";
    private static final String MINUS_MONTHS = "minusMonths";
    private static final String MINUS_DAYS = "minusDays";
    private static final String MINUS_HOURS = "minusHours";
    private static final String MINUS_MINUTES = "minusMinutes";
    private static final String MINUS_SECONDS = "minusSeconds";
    private static final String PERIOD_IN_NANOS = "periodInNanos";
    private static final String PERIOD_IN_MILLISECONDS = "periodInMilliseconds";
    private static final String PERIOD_IN_SECONDS = "periodInSeconds";
    private static final String PERIOD_IN_MINUTES = "periodInMinutes";
    private static final String PERIOD_IN_HOURS = "periodInHours";
    private static final String PERIOD_IN_DAYS = "periodInDays";
    private static final String DATE_FORMAT = "dateFormat";
    private static final String PARSE_DATE = "parseDate";
    private static final String TO_DATE = "toDate";

    private final Map<String,DateTimeFormatter> dateTimeFormatterCache;

    public DateQueryFunctionLayer() {
        super(SystemProperties.get(SystemProperties.Query.Function.DATE_FUNCTION_NAME));

        this.dateTimeFormatterCache = new HashMap<>();

        addFunctionName(NOW);
        addFunctionName(GET_YEAR);
        addFunctionName(GET_MONTH);
        addFunctionName(GET_MONTH_NUMBER);
        addFunctionName(GET_DAY_OF_MONTH);
        addFunctionName(GET_DAY_OF_WEEK);
        addFunctionName(GET_DAY_OF_YEAR);
        addFunctionName(GET_HOUR);
        addFunctionName(GET_MINUTE);
        addFunctionName(GET_SECOND);
        addFunctionName(GET_MILLISECOND_UNIX_EPOCH);
        addFunctionName(GET_NANO);
        addFunctionName(PLUS_YEARS);
        addFunctionName(PLUS_MONTHS);
        addFunctionName(PLUS_DAYS);
        addFunctionName(PLUS_HOURS);
        addFunctionName(PLUS_MINUTES);
        addFunctionName(PLUS_SECONDS);
        addFunctionName(MINUS_YEARS);
        addFunctionName(MINUS_MONTHS);
        addFunctionName(MINUS_DAYS);
        addFunctionName(MINUS_HOURS);
        addFunctionName(MINUS_MINUTES);
        addFunctionName(MINUS_SECONDS);
        addFunctionName(PERIOD_IN_NANOS);
        addFunctionName(PERIOD_IN_MILLISECONDS);
        addFunctionName(PERIOD_IN_SECONDS);
        addFunctionName(PERIOD_IN_MINUTES);
        addFunctionName(PERIOD_IN_HOURS);
        addFunctionName(PERIOD_IN_DAYS);
        addFunctionName(DATE_FORMAT);
        addFunctionName(PARSE_DATE);
        addFunctionName(TO_DATE);
    }

    @Override
    public Object evaluate(String functionName, Object... parameters) {
        Object result;
        switch (functionName) {
            case NOW: {
                if(parameters.length == 0) {
                    result = Date.from(ZonedDateTime.now().toInstant());
                } else if(parameters.length == 1) {
                    ZoneId zoneId = ZoneId.of((String)parameters[0]);
                    result = Date.from(ZonedDateTime.now(zoneId).toInstant());
                } else {
                    throw new HCJFRuntimeException("Illegal parameters length, now() or now((String)zoneId)");
                }
                break;
            }
            case GET_YEAR: result = getZonedDateTimeFromDate(parameters).getYear(); break;
            case GET_MONTH: result = getZonedDateTimeFromDate(parameters).getMonth(); break;
            case GET_MONTH_NUMBER: result = getZonedDateTimeFromDate(parameters).getMonthValue(); break;
            case GET_DAY_OF_MONTH: result = getZonedDateTimeFromDate(parameters).getDayOfMonth(); break;
            case GET_DAY_OF_WEEK: result = getZonedDateTimeFromDate(parameters).getDayOfWeek(); break;
            case GET_DAY_OF_YEAR: result = getZonedDateTimeFromDate(parameters).getDayOfYear(); break;
            case GET_HOUR: result = getZonedDateTimeFromDate(parameters).getHour(); break;
            case GET_MINUTE: result = getZonedDateTimeFromDate(parameters).getMinute(); break;
            case GET_SECOND: result = getZonedDateTimeFromDate(parameters).getSecond(); break;
            case GET_NANO: result = getZonedDateTimeFromDate(parameters).getNano(); break;
            case GET_MILLISECOND_UNIX_EPOCH: result = Date.from(getZonedDateTimeFromDate(parameters).toInstant()).getTime(); break;
            case PLUS_YEARS: result = getZonedDateTimeFromDate(1, parameters).plusYears(((Long)parameters[parameters.length - 1])); break;
            case PLUS_MONTHS: result = getZonedDateTimeFromDate(1, parameters).plusMonths(((Long)parameters[parameters.length - 1])); break;
            case PLUS_DAYS: result = getZonedDateTimeFromDate(1, parameters).plusDays(((Long)parameters[parameters.length - 1])); break;
            case PLUS_HOURS: result = getZonedDateTimeFromDate(1, parameters).plusHours(((Long)parameters[parameters.length - 1])); break;
            case PLUS_MINUTES: result = getZonedDateTimeFromDate(1, parameters).plusMinutes(((Long)parameters[parameters.length - 1])); break;
            case PLUS_SECONDS: result = getZonedDateTimeFromDate(1, parameters).plusSeconds(((Long)parameters[parameters.length - 1])); break;
            case MINUS_YEARS: result = getZonedDateTimeFromDate(1, parameters).minusYears(((Long)parameters[parameters.length - 1])); break;
            case MINUS_MONTHS: result = getZonedDateTimeFromDate(1, parameters).minusMonths(((Long)parameters[parameters.length - 1])); break;
            case MINUS_DAYS: result = getZonedDateTimeFromDate(1, parameters).minusDays(((Long)parameters[parameters.length - 1])); break;
            case MINUS_HOURS: result = getZonedDateTimeFromDate(1, parameters).minusHours(((Long)parameters[parameters.length - 1])); break;
            case MINUS_MINUTES: result = getZonedDateTimeFromDate(1, parameters).minusMinutes(((Long)parameters[parameters.length - 1])); break;
            case MINUS_SECONDS: result = getZonedDateTimeFromDate(1, parameters).minusSeconds(((Long)parameters[parameters.length - 1])); break;
            case PERIOD_IN_NANOS: result = getDuration(parameters).toNanos(); break;
            case PERIOD_IN_MILLISECONDS: result = getDuration(parameters).toMillis(); break;
            case PERIOD_IN_SECONDS: result = getDuration(parameters).toMillis() / 1000; break;
            case PERIOD_IN_MINUTES: result = getDuration(parameters).toMinutes(); break;
            case PERIOD_IN_HOURS: result = getDuration(parameters).toHours(); break;
            case PERIOD_IN_DAYS: result = getDuration(parameters).toDays(); break;
            case TO_DATE: result = new Date(((Number)parameters[0]).longValue()); break;
            case PARSE_DATE: {
                if(parameters.length >= 2) {
                    try {
                        result = new SimpleDateFormat((String) parameters[0]).parse((String) parameters[1]);
                    } catch (Exception ex){
                        throw new HCJFRuntimeException("Date parse fail", ex);
                    }
                } else {
                    throw new HCJFRuntimeException("Illegal parameters length, parseDate((String)pattern, (String)vale)");
                }
                break;
            }
            case DATE_FORMAT: {
                if(parameters.length >= 2) {
                    String pattern = (String) parameters[parameters.length-1];
                    ZonedDateTime zonedDateTime = getZonedDateTimeFromDate(1, parameters);
                    result = getDateFormatter(pattern).format(zonedDateTime);
                } else {
                    throw new HCJFRuntimeException("Illegal parameters length");
                }
                break;
            }
            default: throw new HCJFRuntimeException("Date function not found: %s", functionName);
        }
        return result;
    }

    private synchronized DateTimeFormatter getDateFormatter(String pattern) {
        DateTimeFormatter formatter = this.dateTimeFormatterCache.get(pattern);
        if(formatter == null) {
            formatter = DateTimeFormatter.ofPattern(pattern);
            this.dateTimeFormatterCache.put(pattern, formatter);
        }
        return formatter;
    }

    private ZonedDateTime getZonedDateTimeFromDate(int skipping, Object... parameters) {
        Object[] subSet = new Object[parameters.length - skipping];
        System.arraycopy(parameters, 0, subSet, 0, subSet.length);
        return getZonedDateTimeFromDate(subSet);
    }

    private ZonedDateTime getZonedDateTimeFromDate(Object... parameters) {
        ZonedDateTime result;
        if(parameters.length == 1) {
            result = ZonedDateTime.ofInstant(((Date)getParameter(0, parameters)).toInstant(), ZoneId.systemDefault());
        } else if(parameters.length == 2) {
            ZoneId zoneId = ZoneId.of(getParameter(0, parameters));
            result = ZonedDateTime.ofInstant(((Date)getParameter(1, parameters)).toInstant(), zoneId);
        } else {
            throw new HCJFRuntimeException("Illegal parameters length");
        }
        return result;
    }

    private Duration getDuration(Object... parameters) {
        Duration result;
        if(parameters.length == 1) {
            result = Duration.between(((Date)getParameter(0, parameters)).toInstant(), Instant.now());
        } else if(parameters.length == 2) {
            result = Duration.between(((Date)getParameter(0, parameters)).toInstant(), ((Date)getParameter(1, parameters)).toInstant());
        } else {
            throw new HCJFRuntimeException("Illegal parameters length");
        }
        return result;
    }
}
