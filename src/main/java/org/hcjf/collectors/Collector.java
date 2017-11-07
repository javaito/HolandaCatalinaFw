package org.hcjf.collectors;

import org.hcjf.service.ServiceConsumer;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * This class represents an implementation of a collector.
 * @author javaito
 */
public abstract class Collector<C extends Object> implements ServiceConsumer {

    private final String name;
    private final Long startTime;

    public Collector(String name, Long startTime) {
        if(name == null) {
            throw new NullPointerException("");
        }

        this.name = name;
        this.startTime = startTime;
    }

    /**
     * Returns the name of the collector instance.
     * This name is used to index the collector information.
     * @return Name of the collector instance.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the start date of the collector.
     * @return Start date of the collector.
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * This method is the public interface to collect information into the collector.
     * This method calculates the time id to index de information using the periodicity
     * of the collector then call the onCollector method to execute the internal collecting algorithm.
     * @param collectibleObject Collectible object.
     */
    public final void collect(C collectibleObject) {
        LocalDateTime localDateTime = LocalDateTime.now();
        ChronoUnit chronoUnit = null;
        switch (getPeriodicity()) {
            case MINUTE: chronoUnit = ChronoUnit.MINUTES; break;
            case HOUR: chronoUnit = ChronoUnit.HOURS; break;
            case DAY: chronoUnit = ChronoUnit.DAYS; break;
            case WEEK: chronoUnit = ChronoUnit.WEEKS; break;
            case MONTH: chronoUnit = ChronoUnit.MONTHS; break;
            case YEAR: chronoUnit = ChronoUnit.YEARS; break;
        }
        Long timeId = 0L;

        if(chronoUnit != null) {
            timeId = Date.from(localDateTime.truncatedTo(chronoUnit).atZone(ZoneId.systemDefault()).toInstant()).getTime();
        }

        onCollect(collectibleObject, timeId);
    }

    /**
     * This method must implements the internal collecting algorithm.
     * @param collectibleObject Collectible object.
     * @param timeId Time windows id.
     */
    protected abstract void onCollect(C collectibleObject, Long timeId);

    /**
     * Returns the periodicity of the collector instance.
     * @return Periodicity of the collector instance.
     */
    public abstract Periodicity getPeriodicity();

    /**
     * This method will be called periodically in order to persist the information
     * into a safety storage.
     */
    public abstract void flush();

    /**
     * This enum contains all the periodicity values for the collectors.
     */
    public enum Periodicity {

        MINUTE,

        HOUR,

        DAY,

        WEEK,

        MONTH,

        YEAR,

        ETERNITY

    }

}
