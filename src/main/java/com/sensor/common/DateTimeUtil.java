package com.sensor.common;

import com.sensor.common.client.ZookeeperClient;
import com.sensor.common.config.GlobalConfigInfo;
import com.sensor.common.util.DateUnit;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianyi on 01/08/2017.
 */
public class DateTimeUtil {
    private static final Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);
    public static final LocalDateTime WEEK_START_TIME;
    public static LocalDateTime THE_EPOCH;

    public DateTimeUtil() {
    }

    public static String msTimestampToDatetimeString(long var0) {
        return DateFormat.FULL_DATETIME_FORMAT.format(var0);
    }

    public static String msTimestampToDateString(long var0) {
        return DateFormat.DEFAULT_DAY_FORMAT.format(var0);
    }

    public static String getCurrentDateTimeString() {
        return DateFormat.FULL_DATETIME_FORMAT.format(System.currentTimeMillis());
    }

    public static long dateStringToMsTimestamp(String var0) throws ParseException {
        return DateFormat.DEFAULT_DAY_FORMAT.parse(var0).getTime();
    }

    public static long shortDatetimeStringToMsTimestamp(String var0) throws ParseException {
        return DateFormat.SHORT_DATETIME_FORMAT.parse(var0).getTime();
    }

    public static Date datetimeStringToDate(String var0) throws ParseException {
        FastDateFormat var1 = DateFormat.FULL_DATETIME_FORMAT;

        try {
            return var1.parse(var0);
        } catch (ParseException var3) {
            var1 = DateFormat.DEFAULT_DATETIME_FORMAT;
            return var1.parse(var0);
        }
    }

    public static long datetimeStringToMsTimestamp(String var0) throws ParseException {
        return datetimeStringToDate(var0).getTime();
    }

    public static long getDateDiff(Date var0, Date var1, TimeUnit var2) {
        long var3 = var1.getTime() - var0.getTime();
        return var2.convert(var3, TimeUnit.MILLISECONDS);
    }

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

    public static int calcWeekId(long var0) {
        return calcWeekId((new DateTime(var0)).toLocalDateTime());
    }

    public static int calcMonthId(long var0) {
        return calcMonthId((new DateTime(var0)).toLocalDateTime());
    }

    public static int calcDayId(LocalDateTime var0) {
        int var1 = Days.daysBetween(THE_EPOCH, var0).getDays();
        return THE_EPOCH.isAfter(var0)?var1 - 1:var1;
    }

    public static int calcWeekId(LocalDateTime var0) {
        int var1 = Weeks.weeksBetween(WEEK_START_TIME, var0).getWeeks();
        return WEEK_START_TIME.isAfter(var0)?var1 - 1:var1;
    }

    public static int calcMonthId(LocalDateTime var0) {
        int var1 = Months.monthsBetween(THE_EPOCH, var0).getMonths();
        return THE_EPOCH.isAfter(var0)?var1 - 1:var1;
    }

    public static TimeZone setTimeZone(String var0) {
        TimeZone var1 = TimeZone.getTimeZone(var0);
        TimeZone.setDefault(var1);
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(var1));
        return var1;
    }

    public static TimeZone setDefaultTimeZone(GlobalConfigInfo var0) {
        if(null == var0) {
            logger.warn("null global config got. here doesn\'t set timezone initiative.");
        } else {
            String var1 = var0.getDefaultTimeZoneId();
            if(null == var1) {
                logger.warn("global config is null. here doesn\'t set timezone initiative.");
            } else {
                logger.info("set global time zone as zoneId={}", var1);
                setTimeZone(var1);
            }
        }
        logger.info("now default zone: java:{}, jodaTime:{}", TimeZone.getDefault().getID(), DateTimeZone.getDefault().toString());
        return TimeZone.getDefault();
    }

    public static TimeZone initDefautlTimeZone(ZookeeperClient zkConfig) throws Exception {
        return setDefaultTimeZone(zkConfig.getGlobalConfigInfo());
    }

    public static TimeZone setDefaultTimeZoneInMapReduce(String var0) throws Exception {
        if(null == var0) {
            throw new Exception("null zoneId, here can not set default timeZone");
        } else {
            setTimeZone(var0);
            return TimeZone.getDefault();
        }
    }

    public static Date nextDateUnit(Date var0, int var1, DateUnit var2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(var0);
        //switch(null.$SwitchMap$com$sensorsdata$analytics$common$DateUnit[var2.ordinal()]) {
        // TODO: 06/09/2017 测试正确的结果
        Integer index  = var2.ordinal();
        /*
        switch(DateUnit.fromIndex[index]) {
            case 1:
                calendar.add(10, var1);
                break;
            case 2:
                calendar.add(5, var1);
                break;
            case 3:
                calendar.add(4, var1);
                break;
            case 4:
                calendar.add(2, var1);
                break;
            case 5:
                calendar.add(12, var1);
        }
        calendar.add(12, var1);
        */
        return calendar.getTime();
    }

    public static Date truncate(Date var0, DateUnit var1) {
        return DateUtils.truncate(var0, 5);
        /*
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
        */
    }

    static {
        THE_EPOCH = (new DateTime(0L, DateTimeZone.UTC)).toLocalDateTime();
        WEEK_START_TIME = (new DateTime(-259200000L, DateTimeZone.UTC)).toLocalDateTime();
    }
}
