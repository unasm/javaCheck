package com.sensor.common;

import com.sensor.common.util.Assert;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

/**
 * Created by woodle on 15/10/22.
 * 红包上下文环境
 */
public class BonusContext {
    private static final Logger log = LoggerFactory.getLogger(BonusContext.class);

    private static final String SEPARATOR       = ",";

    public static String ENV;                        // 系统环境

    public static String PRIVATE_KEY;

    public static String PEOPLE_KEY;

    public static String JDB_TRADE_APP_KEY;
    public static String JDB_TRADE_HOST;

    public static String PAY_HOST;
    public static String NOTICE_HOST;
    public static String VOUCHER_HOST;
    public static String PEOPLE_HOST;
    public static String VOIP_HOST;
    public static String VOIP_FROM;
    public static String VOIP_SKEY;

    public static boolean REDIS_TEST_ON_BORROW;
    public static int REDIS_MAX_IDLE;
    public static int REDIS_MIN_EVICTABLE_IDLE_TIME_MILLS;
    public static int REDIS_MAX_TOTAL;
    public static int REDIS_MAX_WAIT_MILLIS;
    public static String[] REDIS_INFO;

    public static String PAY_MERCHANT_ID;

    public static String MAIL_SEND_MAIL;
    public static String MAIL_SEND_PASSWORD;
    public static String MAIL_SEND_SMTP;
    public static String MAIL_SEND_PORT;
    public static String MAIL_RECEIVE_MAIL;
    public static String X_REDIS_KEY;
    public static String DETAIL_H5;

    public static String PASSPORT_APP_ID;
    public static String PASSPORT_APP_KEY;

    public static String MQ_NAME_SERVER;
    public static String POSSPORT_HOST;
    public static String LOCAL_HOST;

    public static void init() {
    }

    private static String get(String key,  ResourceBundle... resourceBundles) {
        String value = null;
        for (ResourceBundle resourceBundle: resourceBundles) {
            /*
            if (resourceBundle.containsKey(key)) {
                value = resourceBundle.getString(key);
                break;
            }
            */
        }

        Assert.notNull(value, "必要参数［" + key + "]没有配置");
        return value;
    }

    private static String[] getArray(String key, ResourceBundle... resourceBundles) {
        return get(key, resourceBundles).split(SEPARATOR);
    }

    private static boolean getBoolean(String key, ResourceBundle... resourceBundles) {
        return BooleanUtils.toBoolean(get(key, resourceBundles));
    }

    private static int getInt(String key, ResourceBundle...resourceBundles){
        return NumberUtils.toInt(get(key, resourceBundles));
    }

    public static boolean isXEnv() {
        return "xwuli".equals(ENV);
    }

    public static boolean isNotXEnv() {
        return !isXEnv();
    }
}
