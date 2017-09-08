package com.sensor.queryengine;

import com.sensor.common.config.RoleStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyi on 02/08/2017.
 */
public class QueryEngineCounter {
    private Map<String, Long> counters = new HashMap<String, Long>();

    public QueryEngineCounter() {
    }

    private void inc(String key, long value) {
        this.counters.put(key, this.get(key) + value);
    }

    private void set(String key, long value) {
        this.counters.put(key, value);
    }

    private long get(String var1) {
        return this.counters.getOrDefault(var1, 0L);
        //return ((Long)this.counters.get(var1)).longValue();
        //return ((Long)this.counters.getOrDefault(var1, Long.valueOf(0L))).longValue();
    }

    public synchronized void newRequest(String var1) {
        this.inc(String.format("%s_num", var1.toLowerCase()), 1L);
        this.set("last_query_time", System.currentTimeMillis());
    }

    /**
     * 什么时间，什么请求结束了
     *
     * @param name      请求的类型
     * @param times     请求的时间
     */
    public synchronized void completeRequest(String name, long times) {
        long var4 = System.currentTimeMillis() - times;
        this.inc(String.format("%s_succeed_num", name.toLowerCase()), 1L);
        this.inc(String.format("%s_execute_time_ms", name.toLowerCase()), var4);
    }

    public synchronized RoleStatus calcRoleStatus() {
        RoleStatus status = new RoleStatus();
        status.setCounters(this.counters);
        return status;
    }
}
