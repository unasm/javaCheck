package com.sensor.common;

/**
 * Created by tianyi on 18/08/2017.
 */
public class RedisConstants {
    public static final int DATABASE_ID_MAPPING = 0;
    public static final int DATABASE_WEB_SESSION = 1;
    public static final int DATABASE_QUERY_ENGINE_CACHE = 2;
    public static final int DATABASE_DIMENSION_DICT = 3;
    public static final String WEB_USER_GUIDE = "guide";
    public static final String WEB_USER_TOKEN = "token";
    public static final String WEB_USER_TOKEN_REV = "token-rev";
    public static final String WEB_DIMENSION = "DICT";
    public static final String WEB_JS_EDIT_MODE = "js-edit-mode";

    public RedisConstants() {
    }

    public static String generateKey(String var0, String var1, int var2) {
        return generateKey(var0 + "_" + var1, var2);
    }

    public static String generateKey(String var0, int var1) {
        return var1 == 1 ? var0:generateProjectKeyPrefix(var1) + var0;
    }

    public static String generateProjectKeyPrefix(int index) {
        return "p_" + index + "#";
    }

    public static Integer getExtConfigIndex(int index) {
        Integer var1 = null;
        switch(index) {
            case 0:
            case 3:
                var1 = null;
                break;
            case 1:
            case 2:
                var1 = 0;
        }

        return var1;
    }
}
