package com.sensor.queryengine.rewriter.store;

import com.sensor.common.RedisClient;
import com.sensor.common.RedisConstants;
import com.sensor.db.bean.PropertyBean;
import com.sensor.queryengine.Constants;
import com.sensor.service.MetaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ScanResult;

import java.util.Map;

/**
 * Created by tianyi on 05/09/2017.
 */
public class RedisStore {
    private static final Logger logger = LoggerFactory.getLogger(RedisStore.class);
    private RedisClient redisClient = new RedisClient(3);

    public RedisStore() {
    }

    private int getProjectId() {
        return MetaDataService.getInstance().getCurrentProjectId();
    }

    public String getDimensionDictValue(PropertyBean var1, boolean var2, String var3) {
        String var5 = "%s_%s_ID_MAPPING_VALUE";
        if(var2) {
            var5 = "%s_%s_VALUE_MAPPING_ID";
        }

        String var4;
        if(var1.isEventProperty()) {
            var4 = RedisConstants.generateKey("DICT", String.format(var5, Constants.TYPE_PROPERTY_EVENT, var1.getName().replace("$session", "")), this.getProjectId());
        } else {
            var4 = RedisConstants.generateKey("DICT", String.format(var5, Constants.TYPE_PROPERTY_USER, var1.getName().replace("$session", "")), this.getProjectId());
        }

        String var6 = null;

        try {
            var6 = this.redisClient.hget(var4, var3);
        } catch (Exception var8) {
            logger.warn("fail to get value by key", var8);
        }

        return var6;
    }

    public ScanResult<Map.Entry<String, String>> getDimensionDictMap(PropertyBean var1, int var2) {
        String var3;
        if(var1.isEventProperty()) {
            var3 = RedisConstants.generateKey("DICT", String.format("%s_%s_ID_MAPPING_VALUE", Constants.TYPE_PROPERTY_EVENT, var1.getName().replace("$session", "")), this.getProjectId());
        } else {
            var3 = RedisConstants.generateKey("DICT", String.format("%s_%s_ID_MAPPING_VALUE", Constants.TYPE_PROPERTY_USER, var1.getName().replace("$session", "")), this.getProjectId());
        }

        ScanResult var4 = null;

        try {
            var4 = this.redisClient.hscan(var3, var2);
        } catch (Exception var6) {
            logger.warn("fail to get value by key", var6);
        }

        return var4;
    }

    public void clearDict(String var1, String var2) {
        String[] var3 = new String[]{RedisConstants.generateKey("DICT", String.format("%s_%s_ID_MAPPING_VALUE", var2, var1),
                this.getProjectId()), RedisConstants.generateKey("DICT", String.format("%s_%s_VALUE_MAPPING_ID", var2, var1), this.getProjectId())};

        try {
            this.redisClient.del(var3);
        } catch (Exception var5) {
            logger.error("access redis failed", var5);
        }

    }
}
