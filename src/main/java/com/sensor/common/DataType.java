package com.sensor.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by tianyi on 01/08/2017.
 */
public enum DataType {
    NUMBER(1),
    STRING(2),
    LIST(3),
    DATE(4),
    DATETIME(5),
    BOOL(6),
    UNKNOWN(-1);

    public static final int NUMBER_STORE_IN_VERTICA_FACTOR = 1000;
    public static final int STRING_IN_LIST_MAX_BYTES_SIZE = 255;
    public static final int LIST_MAX_ELEMENT_SIZE = 100;
    public static final BigDecimal NUMBER_FACTOR_BIG_DECIMAL = new BigDecimal(1000);
    private int index;
    private static DataType[] indexArray = new DataType[8];
    private static final Logger logger;

    private DataType(int index) {
        this.index = index;
    }

    public static DataType fromString(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (Exception ex) {
            logger.warn("Unknown DataType \'{}\', errorMsg: \'{}\', errorTrace: \'{}\'", type, ex.getMessage(),
                            Arrays.deepToString(ex.getStackTrace()));
            return UNKNOWN;
        }
    }

    public static DataType fromInt(int index) {
        return index >= 1 && index <= 6 ? indexArray[index] : UNKNOWN;
    }

    public int getIndex() {
        return this.index;
    }

    public static long multiplyNumberFactor(double var0) {
        return BigDecimal.valueOf(var0).multiply(NUMBER_FACTOR_BIG_DECIMAL).longValue();
    }

    public static long multiplyNumberFactor(String var0) {
        BigDecimal value = (new BigDecimal(var0)).multiply(NUMBER_FACTOR_BIG_DECIMAL);
        return value.longValue();
    }

    static {
        DataType[] value = values();
        int length = value.length;

        for (int index = 0; index < length; ++index) {
            DataType type = value[index];
            if (type.index == -1) {
                indexArray[7] = type;
            } else {
                indexArray[type.index] = type;
            }
        }

        logger = LoggerFactory.getLogger(DataType.class);
    }
}
