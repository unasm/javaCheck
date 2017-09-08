package com.sensor.common.util;

import com.sensor.common.DateFormat;
import com.sensor.db.OLAPEngineConnectionPool;
import com.sensor.db.OLAPEngineType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static com.sensor.common.DateTimeUtil.truncate;

/**
 * Created by tianyi on 01/08/2017.
 */
public class DateUtil {
    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);
    public static Date START_DATE;

    public DateUtil() {
    }

    public static int getBetweenDays(String var0, String var1) {
        var0 = StringUtils.split(var0, " ")[0];
        var1 = StringUtils.split(var1, " ")[0];
        LocalDate var2 = LocalDate.parse(var0);
        LocalDate var3 = LocalDate.parse(var1);
        return Days.daysBetween(var3, var2).getDays();
    }

    public static Date nextDateUnit(Date var0, int var1, DateUnit var2) {
        Calendar var3 = Calendar.getInstance();
        var3.setTime(var0);
        switch(var2.ordinal()) {
            //switch(null.$SwitchMap$com$sensorsdata$analytics$common$DateUnit[var2.ordinal()]) {
            case 1:
                var3.add(10, var1);
                break;
            case 2:
                var3.add(5, var1);
                break;
            case 3:
                var3.add(4, var1);
                break;
            case 4:
                var3.add(2, var1);
                break;
            case 5:
                var3.add(12, var1);
        }
        var3.add(12, var1);
        return var3.getTime();

    }

    public static Date nextDateUnit(Date var0, DateUnit var1) {
        return nextDateUnit(var0, 1, var1);
    }

    /*
    public static int calcDateInterval(Date var0, Date var1, DateUnit var2) {
        DateTime var3 = new DateTime(var0);
        DateTime var4 = new DateTime(var1);
        switch(null.$SwitchMap$com$sensorsdata$analytics$common$DateUnit[var2.ordinal()]) {
            case 1:
                return Hours.hoursBetween(var3, var4).getHours();
            case 2:
                return Days.daysBetween(var3, var4).getDays();
            case 3:
                return Weeks.weeksBetween(var3, var4).getWeeks();
            case 4:
                return Months.monthsBetween(var3, var4).getMonths();
            case 5:
                return Minutes.minutesBetween(var3, var4).getMinutes();
            default:
                return -1;
        }
    }

    public static Date truncate(Date var0, DateUnit var1) {
        switch(null.$SwitchMap$com$sensorsdata$analytics$common$DateUnit[var1.ordinal()]) {
            case 1:
                return DateUtils.truncate(var0, 10);
            case 2:
                return DateUtils.truncate(var0, 5);
            case 3:
                return (new LocalDate(var0)).withDayOfWeek(1).toDate();
            case 4:
                return DateUtils.truncate(var0, 2);
            case 5:
                return DateUtils.truncate(var0, 12);
            default:
                return DateUtils.truncate(var0, 5);
        }
    }
    */
    public static Date parseInputDate(String var0, DateUnit var1) {
        if(var0 == null) {
            return null;
        } else {
            Date var2;
            try {
                var2 = DateFormat.DEFAULT_DATETIME_FORMAT.parse(var0);
            } catch (ParseException var10) {
                try {
                    var2 = DateFormat.SHORT_DATETIME_FORMAT.parse(var0);
                } catch (ParseException var9) {
                    try {
                        var2 = DateFormat.DEFAULT_DAY_FORMAT.parse(var0);
                    } catch (ParseException var8) {
                        try {
                            var2 = DateFormat.SHORT_DAY_FORMAT.parse(var0);
                        } catch (ParseException var7) {
                            logger.warn("fail to parse request date.", var9);
                            return null;
                        }
                    }
                }
            }

            return truncate(var2, var1);
        }
    }
    public static String getSQLTruncUnit(DateUnit var0) {
        //switch(null.$SwitchMap$com$sensorsdata$analytics$common$DateUnit[var0.ordinal()]) {
        //switch(DateUnit.fromIndex(var0.ordinal())) {
        //switch(DateUnit[var0.ordinal()] .fromIndex(var0.ordinal())) {
        // TODO: 05/09/2017 检查一下，是否正确, 有可能ordinal 的值 在switch里面什么都得不到
        switch(var0.ordinal()) {
            case 1:
                return "HH";
            case 2:
                return "DD";
            case 3:
                if(OLAPEngineConnectionPool.getEngineType() == OLAPEngineType.VERTICA) {
                    return "IW";
                }

                return "DAY";
            case 4:
                return "MON";
            case 5:
                return "MI";
            default:
                return "DD";
        }
    }


    /*
    public static int toDateId(Date var0) {
        return calcDateInterval(START_DATE, var0, DateUnit.DAY);
    }

    public static Date fromDateId(int var0) {
        return new Date(START_DATE.getTime() + (long)var0 * 86400000L);
    }




    public static int getPartOfDate(Date var0, DateUnit var1) {
        Calendar var2 = Calendar.getInstance();
        var2.setTime(var0);
        switch(null.$SwitchMap$com$sensorsdata$analytics$common$DateUnit[var1.ordinal()]) {
            case 1:
                return var2.get(11);
            case 5:
                return var2.get(12);
            default:
                return 0;
        }
    }

    public static Date getFunnelRealToDate(int var0, Date var1) {
        int var2 = (int)((long)(var0 * 60) * 1000L / 86400000L);
        if(var2 <= 0) {
            var2 = 1;
        }

        return nextDateUnit(var1, var2, DateUnit.DAY);
    }


    */

    public static Date tryParse(String var0) {
        FastDateFormat[] var1 = DateFormat.ALL_DATETIME_FORMATS;
        int var2 = var1.length;
        int var3 = 0;

        while(var3 < var2) {
            FastDateFormat var4 = var1[var3];

            try {
                return var4.parse(var0);
            } catch (ParseException var6) {
                ++var3;
            }
        }

        return null;
    }


    static {
        try {
            START_DATE = DateFormat.SHORT_DAY_FORMAT.parse("19700101");
        } catch (ParseException var1) {
            ;
        }

    }

}
