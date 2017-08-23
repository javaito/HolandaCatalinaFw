package org.hcjf.layers.query.functions;

import org.hcjf.properties.SystemProperties;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public class DateQueryFunctionLayer extends BaseQueryFunctionLayer implements QueryFunctionLayerInterface {

    private static final String NOW = "now";
    private static final String GET_YEAR = "getYear";
    private static final String GET_MONTH = "getMonth";
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

    private final Set<String> aliases;

    public DateQueryFunctionLayer() {
        super(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) +
                SystemProperties.get(SystemProperties.Query.Function.DATE_LAYER_NAME));

        aliases = new HashSet<>();
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + NOW);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + GET_YEAR);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + GET_MONTH);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + GET_DAY_OF_MONTH);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + GET_DAY_OF_WEEK);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + GET_DAY_OF_YEAR);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + GET_HOUR);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + GET_MINUTE);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + GET_SECOND);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + GET_MILLISECOND_UNIX_EPOCH);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + GET_NANO);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PLUS_YEARS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PLUS_MONTHS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PLUS_DAYS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PLUS_HOURS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PLUS_MINUTES);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PLUS_SECONDS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + MINUS_YEARS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + MINUS_MONTHS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + MINUS_DAYS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + MINUS_HOURS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + MINUS_MINUTES);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + MINUS_SECONDS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PERIOD_IN_NANOS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PERIOD_IN_MILLISECONDS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PERIOD_IN_SECONDS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PERIOD_IN_MINUTES);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PERIOD_IN_HOURS);
        aliases.add(SystemProperties.get(SystemProperties.Query.Function.NAME_PREFIX) + PERIOD_IN_DAYS);
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
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
                    throw new IllegalArgumentException();
                }
                break;
            }
            case GET_YEAR: result = getZonedDateTimeFromDate(parameters).getYear(); break;
            case GET_MONTH: result = getZonedDateTimeFromDate(parameters).getMonth(); break;
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
            default: throw new IllegalArgumentException("Date function not found: " + functionName);
        }
        return result;
    }

    private ZonedDateTime getZonedDateTimeFromDate(int skipping, Object... parameters) {
        Object[] subSet = new Object[parameters.length - skipping];
        System.arraycopy(parameters, 0, subSet, 0, subSet.length);
        return getZonedDateTimeFromDate(subSet);
    }

    private ZonedDateTime getZonedDateTimeFromDate(Object... parameters) {
        ZonedDateTime result;
        if(parameters.length == 1) {
            result = ZonedDateTime.ofInstant(((Date)parameters[0]).toInstant(), ZoneId.systemDefault());
        } else if(parameters.length == 2) {
            ZoneId zoneId = ZoneId.of((String)parameters[0]);
            result = ZonedDateTime.ofInstant(((Date)parameters[0]).toInstant(), zoneId);
        } else {
            throw new IllegalArgumentException();
        }
        return result;
    }

    private Duration getDuration(Object... parameters) {
        Duration result;
        if(parameters.length == 1) {
            result = Duration.between(((Date)parameters[0]).toInstant(), Instant.now());
        } else if(parameters.length == 2) {
            result = Duration.between(((Date)parameters[0]).toInstant(), ((Date)parameters[0]).toInstant());
        } else {
            throw new IllegalArgumentException();
        }
        return result;
    }
}
