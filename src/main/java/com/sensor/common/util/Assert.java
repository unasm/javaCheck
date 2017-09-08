package com.sensor.common.util;

import java.util.List;

/**
 * Created by woodle on 15/10/13.
 */

public abstract  class Assert {

    protected Assert() {
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(Boolean obj, String message) {
        if (!obj) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notBlank(String obj, String message) {
        if (obj == null || "".equals(obj.trim())) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void  notEmpty(List<?> objList, String message) {
        if (objList == null || objList.size() == 0) {
            throw new IllegalArgumentException(message);
        }
    }

}
