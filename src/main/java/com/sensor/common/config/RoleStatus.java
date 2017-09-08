package com.sensor.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sensor.common.client.MetaClient;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyi on 26/08/2017.
 */
public class RoleStatus {
    private RoleStatus.AliveStatus status;
    private boolean master;
    private Map<String, Long> counters = new HashMap<>();

    public RoleStatus() {
        this.status = RoleStatus.AliveStatus.ALIVE;
        this.master = false;
    }

    public synchronized void clearCounter() {
        this.counters.clear();
    }

    public synchronized void addCounter(String key, Long value) {
        if(null == this.counters) {
            this.counters = new HashMap<>();
        }

        this.counters.put(key, value);
    }

    public synchronized void increaseCounter(String var1, long var2) {
        if(null == this.counters) {
            this.counters = new HashMap<>();
        }

        this.counters.put(var1, this.counters.getOrDefault(var1, 0L) + var2);
    }

    public RoleStatus.AliveStatus getStatus() {
        return this.status;
    }

    public void setStatus(RoleStatus.AliveStatus status) {
        this.status = status;
    }

    public boolean isMaster() {
        return this.master;
    }

    public void setIsMaster(boolean isMaster) {
        this.master = isMaster;
    }

    public Map<String, Long> getCounters() {
        return this.counters;
    }

    public void setCounters(Map<String, Long> counters) {
        this.counters = counters;
    }

    public String toString() {
        return "RoleStatus{status=" + this.status + ", master=" + this.master + ", counters=" + this.counters + '}';
    }

    /** @deprecated */
    @Deprecated
    public static RoleStatus createFromMeta(MetaClient var0, String var1) throws IOException, SQLException {
        return new RoleStatus();
    }

    public void storeInMeta(MetaClient var1, String var2) throws UnknownHostException, JsonProcessingException, SQLException {
    }

    private static String makeCounterPersistanceKey(String key) throws UnknownHostException {
        return "/role_status/" + StringUtils.lowerCase(key) + "/" + InetAddress.getLocalHost().getHostName();
    }

    public static enum AliveStatus {
        ALIVE,
        DEAD,
        UNKNOWN;

        private AliveStatus() {
        }
    }
}
