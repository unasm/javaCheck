package com.sensor.common.utils;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created by tianyi on 05/09/2017.
 */
public class BytesUtil {

    public BytesUtil() {
    }

    private static void intToQuadBytes(int var0, byte[] var1, int var2) {
        var1[var2 + 0] = (byte)(var0 >> 24 & 255);
        var1[var2 + 1] = (byte)(var0 >> 16 & 255);
        var1[var2 + 2] = (byte)(var0 >> 8 & 255);
        var1[var2 + 3] = (byte)(var0 >> 0 & 255);
    }

    private static int quadBytesToInt(byte[] var0, int var1) {
        boolean var2 = false;
        int var3 = (var0[var1 + 0] & 255) << 24 | (var0[var1 + 1] & 255) << 16 | (var0[var1 + 2] & 255) << 8 | (var0[var1 + 3] & 255) << 0;
        return var3;
    }

    public static byte[] intTreeSetToQuadBytesArray(TreeSet<Integer> var0) {
        byte[] var1 = new byte[var0.size() * 4];
        Iterator var2 = var0.iterator();

        for(int var3 = 0; var3 < var0.size(); ++var3) {
            Integer var4 = (Integer)var2.next();
            intToQuadBytes(var4, var1, var3 * 4);
        }

        return var1;
    }

    public static int[] quadBytesArrayToIntArray(byte[] var0) {
        if(var0 == null) {
            return new int[0];
        } else {
            int[] var1 = new int[var0.length / 4];

            for(int var2 = 0; var2 < var1.length; ++var2) {
                var1[var2] = quadBytesToInt(var0, var2 * 4);
            }

            return var1;
        }
    }
}
