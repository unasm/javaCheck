package com.sensor.common;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tianyi on 01/08/2017.
 */
public class DateFormat {
    public static final FastDateFormat FULL_DATETIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");
    public static final FastDateFormat DEFAULT_DAY_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
    public static final FastDateFormat DEFAULT_DATETIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    public static final FastDateFormat SHORT_DATETIME_FORMAT = FastDateFormat.getInstance("yyyyMMdd_HHmmss");
    public static final FastDateFormat SHORT_DAY_FORMAT = FastDateFormat.getInstance("yyyyMMdd");
    public static final FastDateFormat SHORT_TIME_FORMAT = FastDateFormat.getInstance("HHmmss");


    /*
    public static final SimpleDateFormat FULL_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final SimpleDateFormat DEFAULT_DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DEFAULT_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat SHORT_DATETIME_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    public static final SimpleDateFormat SHORT_DAY_FORMAT = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat SHORT_TIME_FORMAT = new SimpleDateFormat("HHmmss");
    */
    //public static final SimpleDateFormat[] ALL_DATETIME_FORMATS;
    public static final FastDateFormat[] ALL_DATETIME_FORMATS;
    private static final Pattern DATE_FIELD_FORMAT_PATTERN;
    private static final Pattern TIME_FIELD_FORMAT_PATTERN;
    private static final Pattern DATE_FIELD_CHECK_PATTERN;
    private static final Pattern DATE_TIME_FIELD_CHECK_PATTERN;

    public DateFormat() {
    }

    public static DataType getDateFieldType(String var0) {
        Matcher var1 = DATE_FIELD_FORMAT_PATTERN.matcher(var0);
        if(var1.find()) {
            if(var0.length() == 10) {
                return com.sensor.common.DataType.DATE;
            }

            var1 = TIME_FIELD_FORMAT_PATTERN.matcher(var0);
            if(var1.matches()) {
                return com.sensor.common.DataType.DATETIME;
            }
        }

        return null;
    }

    public static boolean checkDateField(String var0) {
        return DATE_FIELD_CHECK_PATTERN.matcher(var0).matches();
    }

    public static boolean checkDateTimeField(String var0) {
        return DATE_TIME_FIELD_CHECK_PATTERN.matcher(var0).matches();
    }

    static {
        ALL_DATETIME_FORMATS = new FastDateFormat[]{FULL_DATETIME_FORMAT, DEFAULT_DATETIME_FORMAT, DEFAULT_DAY_FORMAT, SHORT_DATETIME_FORMAT, SHORT_DAY_FORMAT};
        DATE_FIELD_FORMAT_PATTERN = Pattern.compile("^(19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])");
        TIME_FIELD_FORMAT_PATTERN = Pattern.compile("^.{10} (0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](|\\.\\d{1,3})$");
        DATE_FIELD_CHECK_PATTERN = Pattern.compile("^(19|20)\\d\\d-([1-9]|0[1-9]|1[012])-([1-9]|0[1-9]|[12][0-9]|3[01])$");
        DATE_TIME_FIELD_CHECK_PATTERN = Pattern.compile("^(19|20)\\d\\d-([1-9]|0[1-9]|1[012])-([1-9]|0[1-9]|[12][0-9]|3[01]) ([0-9]|0[0-9]|1[0-9]|2[0-3]):(|[0-5])[0-9]:(|[0-5])[0-9](|\\.\\d{1,3})$");
    }
}
