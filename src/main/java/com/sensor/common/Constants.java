package com.sensor.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * Created by woodle on 15/9/29.
 * 常数
 */
public class Constants {
    public static final String SENSORS_ANALYTICS_PROPERTY = "sensors_analytics.property";
    public static final String INSTALL_TYPE_PROPERTY_NAME = "sensors_analytics.install_type";
    public static final String CUSTOMER_ID_PROPERTY_NAME = "sensors_analytics.customer_id";
    public static final String DEAD_TIME_PROPERTY_NAME = "sensors_analytics.dead_time";
    public static final String EXPIRE_TIME_PROPERTY_NAME = "sensors_analytics.expire_time";
    public static final String INSTALL_TIME_PROPERTY_NAME = "sensors_analytics.install_time";
    public static final String MAX_MESSAGE_COUNT_PROPERTY_NAME = "sensors_analytics.max_message_count";
    public static final String REMIND_TIME_PROPERTY_NAME = "sensors_analytics.remind_time";
    public static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();
    public static final String COMMON_EVENT_NAME = "$Anything";
    public static final String HDFS_DATA_RUNTIME_BASE = "/sa/runtime";
    public static final int EVENT_ID_OF_PROFILE = 0;
    public static final int EVENT_ID_OF_PUBLIC_PROPERTY = -1;
    public static final int EVENT_TABLE_TYPE = 0;
    public static final int PROFILE_TABLE_TYPE = 1;
    public static final int SEGMENTATION_TABLE_TYPE = 2;
    public static final int REGISTER_ROLE_RETRY_TIMES = 3;
    public static final int REGISTER_ROLE_RETRY_INTERVAL = 1;
    public static final int REGISTER_ROLE_TIMEOUT_SECONDS = 3;
    public static int SSH_CONNECT_RETRY_TIMES;
    public static int SSH_CONNECT_RETRY_INTERVAL;
    public static int EVENT_BUCKET_SIZE;
    public static final int HDFS_STORAGE_TYPE = 0;
    public static final int VERTICA_STORAGE_TYPE = 1;
    public static final int DEFAULT_PROJECT_ID = 1;
    public static final String DEFAULT_PROJECT_NAME = "default";
    public static final String DEFAULT_BUILD_VERSION = "1.6.5";

    public Constants() {
    }

    static {
        DEFAULT_OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DEFAULT_OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        SSH_CONNECT_RETRY_TIMES = 3;
        SSH_CONNECT_RETRY_INTERVAL = 5;
        EVENT_BUCKET_SIZE = 10;
    }

    public static enum LibType {
        Java,
        python,
        php,
        Ruby,
        Node,
        js,
        Android,
        iOS,
        BatchImporter,
        FormatImporter,
        LogAgent,
        HDFSImporter,
        DotNET,
        Other;

        private LibType() {
        }
    }
}
