package com.sensor.queryengine;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by tianyi on 01/08/2017.
 */
public class Constants {
    public static final String COMMA = ",";
    public static final String SPACE = " ";
    public static final String LEFT_PARENTHESIS = "(";
    public static final String RIGHT_PARENTHESIS = ")";
    public static final String SQL_$_PLACE_HOLDER = "_O_O_";
    public static final int USER_PROFILE_EVENT_ID = 0;
    public static final String EVENT_DATE = "$date";
    public static final String EVENT_DAY_ID = "$day";
    public static final String EVENT_WEEK_ID = "$week_id";
    public static final String EVENT_MONTH_ID = "$month_id";
    public static final String EVENT_TIME = "$time";
    public static final String EVENT_USER_ID = "$user_id";
    public static final String EVENT_DISTINCT_ID = "$distinct_id";
    public static final String EVENT_ID = "$event_id";
    public static final String USER_ID = "$id";
    public static final String FIRST_ID = "$first_id";
    public static final String SECOND_ID = "$second_id";
    public static final String SEGMENTER_VALUE = "value";
    public static final String EVENT_BUCKET_ID = "$event_bucket";
    public static final String USER_NODE = "user";
    public static final String EVENT_NODE = "event";
    public static final String SESSION_NODE = "session";
    public static final String SESSION_ID = "$session_id";
    public static final String SESSION_DEPTH = "$session_depth";
    public static final String SESSION_DURATION = "$session_duration";
    public static final String SESSION_POSITION = "$session_position";
    public static final String SESSION_EVENT_DURATION = "$session_event_duration";
    public static final String SESSION_FIRST_EVENT = "$event_id$session";
    public static final int COMMON_EVENT_ID = -1;
    public static final String COMMON_EVENT_NAME = "$Anything";
    public static final long MAX_GROUP_NUM = 1000L;
    public static final int SAMPLING_BASE = 64;
    public static final String TOTAL_BY_NAME = "$ALL";
    public static final String WEB_DIMENSION_DICT_REV = "%s_%s_VALUE_MAPPING_ID";
    public static final String WEB_DIMENSION_DICT = "%s_%s_ID_MAPPING_VALUE";
    public static String TYPE_PROPERTY_EVENT = "property";
    public static String TYPE_PROPERTY_USER = "profile";
    public static final String NEGATIVE_INFINITY = "-INF";
    public static final Gson GSON;
    public static final int GRACEFULLY_EXIT_TIMEOUT = 10;

    public Constants() {
    }

    static {
        GSON = (new GsonBuilder()).enableComplexMapKeySerialization().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    public static enum TABLE_TYPE {
        EVENT_TABLE,
        USER_TABLE;

        private TABLE_TYPE() {
        }
    }
}
