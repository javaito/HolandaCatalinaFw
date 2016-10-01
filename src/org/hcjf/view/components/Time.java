package org.hcjf.view.components;

import org.hcjf.view.ViewComponent;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class Time extends ViewComponent {

    private int hour;
    private int minutes;
    private int seconds;
    private char format;
    private HourMarker marker;
    public char TWENTY_FOUR_HOUR = 'H';
    public char TWELVE_HOUR_FORMAT = 'h';

    public Time(String text) {
        super(text);
        format = TWENTY_FOUR_HOUR;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public char getFormat() {
        return format;
    }

    public void setFormat(char format) {
        this.format = format;
    }

    public HourMarker getMarker() {
        return marker;
    }

    public void setMarker(HourMarker marker) {
        this.marker = marker;
    }

    public enum HourMarker {
        AM,
        PM;
    }
}
