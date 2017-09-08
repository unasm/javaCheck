package com.sensor.common.util;

/**
 * Created by tianyi on 01/08/2017.
 */
public enum  DateUnit {
    HOUR(0),
    DAY(1),
    WEEK(2),
    MONTH(3),
    MINUTE(4);

    private int index;

    private DateUnit(int index) {
        this.index = index;
    }

    public static DateUnit fromIndex(int index) {
        switch(index) {
            case 0:
                return HOUR;
            case 1:
                return DAY;
            case 2:
                return WEEK;
            case 3:
                return MONTH;
            case 4:
                return MINUTE;
            default:
                return DAY;
        }
    }

    public boolean isLessThenHourLevel() {
        return this.equals(HOUR) || this.equals(MINUTE);
    }

    public int getIndex() {
        return this.index;
    }
}
