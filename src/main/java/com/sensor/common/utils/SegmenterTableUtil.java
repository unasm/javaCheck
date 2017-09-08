package com.sensor.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sensor.common.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户处理用户分群的数据
 *
 * Created by tianyi on 02/09/2017.
 */
public class SegmenterTableUtil {
    public SegmenterTableUtil() {
    }

    public static String constructTempTableName(String var0, Integer var1) {
        return "segmenter_" + var1 + "___temp___" + var0;
    }

    /**
     * 创建 用户分群表
     *
     * 最后创建成功的表名称： segmenter_1_test
     *
     * @param segmenterName     指定的分群名称
     * @param idx               下标
     * @return  string , segmenter_1_test
     */
    public static String constructFinalTableName(String segmenterName, Integer idx) {
        return "segmenter_" + idx + "_" + segmenterName;
    }

    public static Map<String, Object> parseLastPartition(String partitions) {
        if(partitions != null && partitions.contains("{")) {
            try {
                return Constants.DEFAULT_OBJECT_MAPPER.readValue(partitions, new TypeReference() {});
            } catch (IOException var3) {
                HashMap<String, Object> resArr = new HashMap<>();
                resArr.put("last_partition", partitions);
                resArr.put("num_values", 0L);
                return resArr;
            }
        } else {
            HashMap<String, Object> resArr = new HashMap<>();
            resArr.put("last_partition", partitions);
            resArr.put("num_values", 0L);
            return resArr;
        }
    }
}
