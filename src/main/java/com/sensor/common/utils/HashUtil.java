package com.sensor.common.utils;

import com.sensor.common.client.ZookeeperClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tianyi on 21/08/2017.
 */
public class HashUtil {
    private static final Logger logger = LoggerFactory.getLogger(HashUtil.class);
    public static long bkdrHashFactor = 31L;

    public HashUtil() {
    }

    public static long bkdrhash(String var0) {
        long var1 = 0L;
        if(var0.length() > 0) {
            char[] var3 = var0.toCharArray();

            for(int var4 = 0; var4 < var3.length; ++var4) {
                var1 = 31L * var1 + (long)var3[var4];
            }
        }

        return var1;
    }

    public static long userIdHash(String var0) {
        if(bkdrHashFactor == -1L) {
            throw new RuntimeException("Init bkdr hash factor failed");
        } else {
            long var1 = 0L;
            if(var0.length() > 0) {
                char[] var3 = var0.toCharArray();

                for(int var4 = 0; var4 < var3.length; ++var4) {
                    var1 = bkdrHashFactor * var1 + (long)var3[var4];
                }
            }

            return var1;
        }
    }

    public static long userIdHash(byte[] var0) {
        if(bkdrHashFactor == -1L) {
            throw new RuntimeException("Init bkdr hash factor failed");
        } else {
            long var1 = 0L;
            byte[] var3 = var0;
            int var4 = var0.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                byte var6 = var3[var5];
                var1 = bkdrHashFactor * var1 + (long)var6;
            }

            return var1;
        }
    }

    static {
        ZookeeperClient var0 = null;

        try {
            var0 = new ZookeeperClient();
            bkdrHashFactor = var0.getGlobalConfigInfo().getBkdrHashFactor();
        } catch (Exception var10) {
            logger.error("Can\'t init bkdr hash factor", var10);
            bkdrHashFactor = -1L;
        } finally {
            if(var0 != null) {
                try {
                    var0.close();
                } catch (Exception var9) {
                    logger.error("Close zookeeper client failed", var9);
                }
            }

        }

    }
}
