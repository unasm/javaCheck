package com.sensor.queryengine.util;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;

/**
 * Created by tianyi on 21/08/2017.
 */
public class NumericUtil {
    public static final double exp = 1.0E-6D;

    public NumericUtil() {
    }

    public static int compare(double var0, double var2) {
        double var4 = var0 - var2;
        return Math.abs(var4) <= 1.0E-6D?0:(var4 > 0.0D?1:-1);
    }

    public static float truncateToFloat(double var0) {
        return truncate(Double.valueOf(var0), 2).floatValue();
    }

    public static Number handleNumber(Number var0) {
        long var1 = var0.longValue();
        Object var3;
        if(var1 % 1000L == 0L) {
            var3 = Long.valueOf(var1 / 1000L);
        } else {
            var3 = Double.valueOf((double)var1 / 1000.0D);
        }

        return (Number)var3;
    }

    public static long toLong(Number var0) {
        return (long)(var0.doubleValue() * 1000.0D);
    }

    public static Number setAccuracy(Number var0, String var1) {
        /*
        if(var0 != null && !var0.toString().equals(String.valueOf(0.0D / 0.0)) && var0.doubleValue() < 1.0D / 0.0 && var0.doubleValue() > -1.0D / 0.0) {
            FormatType var2 = FormatType.fromName(var1);
            switch(null.$SwitchMap$com$sensorsdata$analytics$queryengine$common$FormatType[var2.ordinal()]) {
                case 1:
                    return truncate(Double.valueOf(var0.doubleValue()), FormatType.getDigit(var1));
                case 2:
                    return truncate(Double.valueOf(var0.doubleValue() * 100.0D), FormatType.getDigit(var1));
                case 3:
                    return Long.valueOf((long)Math.rint(var0.doubleValue()));
                default:
                    return null;
            }
        } else {
            return null;
        }
        */
        return null;
    }

    public static Pair<Integer, Integer> calculateStartEndPage(int var0, int var1, int var2) {
        int var3 = var0 * var1;
        int var4 = var3 + var1 - 1;
        if(var4 >= var2) {
            var4 = var2 - 1;
        }

        return Pair.of(Integer.valueOf(var3), Integer.valueOf(var4));
    }

    public static Number truncate(Number var0) {
        return truncate(var0, 2);
    }

    public static Number truncate(Number var0, int var1) {
        if(!(var0 instanceof Long) && !(var0 instanceof Integer)) {
            BigDecimal var2 = new BigDecimal(var0.doubleValue());
            if(var2.scale() == 0) {
                return var0;
            } else {
                BigDecimal var3 = var2.setScale(var1, 4);
                return Double.valueOf(var3.doubleValue());
            }
        } else {
            return var0;
        }
    }

    public static Number getValue(Double var0) {
        int var1 = var0.intValue();
        return (Number)((double)var1 == var0.doubleValue()?Integer.valueOf(var1):var0);
    }
}
