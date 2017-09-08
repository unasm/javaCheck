package com.sensor.common.util;

import com.sensor.common.BonusContext;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by woodle on 15/11/7.
 * 公共工具类
 */
public class CommonUtil {

    public static final long BASE_START_DAY       = 1420041600000l; //基准时间为2015-01-01日 00:00:00

    public static final long ONE_DAY_MILLIS       = 86400000;

    public static void main(String... strings) {
        String str = "a,b,c,,";
        int len = StringUtils.split(str, ",").length;
        System.out.print(len);
    }


    public static boolean isLocalEnv() {
        return "local".equals(BonusContext.ENV);
    }

    public static boolean isDevEnv() {
       return "dev".equals(BonusContext.ENV) || "dev2".equals(BonusContext.ENV);
    }

    public static boolean isNotLocalEnv() {
        return ! isLocalEnv();
    }

    public static boolean isNotDevEnv() {
        return ! isDevEnv();
    }

    public static boolean isNotDevEnvAndNotLocalEnv() {
        return isNotLocalEnv() && isNotDevEnv();
    }

    public static long getTimeStampEndOfDay() {
        return BASE_START_DAY + ((System.currentTimeMillis() - BASE_START_DAY) / ONE_DAY_MILLIS + 1) * ONE_DAY_MILLIS - 1;
    }

    public static long getTimeStampAfter(int days) {
        return getTimeStampEndOfDay() + days * ONE_DAY_MILLIS;
    }

    public static Map<Object, Object> buildMap(Object... e) {
        //Preconditions.checkArgument(e.length % 2 == 0 && e.length >= 2, "个数错误");
        Map<Object, Object> map = new HashMap();

        /*
        for(int i = 0; i < e.length - 1; i += 2) {
            map.put(e[i], e[i + 1]);
        }
        */

        return map;
    }

    public static Map<Object, Object> success(Object data) {
        Map<Object, Object> res = new HashMap();
        res.put("data", data);
        res.put("returnCode", 200);
        return res;
    }
}
