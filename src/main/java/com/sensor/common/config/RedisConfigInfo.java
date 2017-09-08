package com.sensor.common.config;

import java.util.List;

/**
 * redis 的config 类
 * Created by tianyi on 18/08/2017.
 */
public class RedisConfigInfo {
    private List<String> redisList;
    private int masterIndex;
    private String password;
    private List<Integer> portList;
    private List<RedisConfigInfo> extConfigList;

    public RedisConfigInfo() {
    }

    public List<String> getRedisList() {
        return this.redisList;
    }

    public void setRedisList(List<String> redisList) {
        this.redisList = redisList;
    }

    public int getMasterIndex() {
        return this.masterIndex;
    }

    public void setMasterIndex(int masterIndex) {
        this.masterIndex = masterIndex;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Integer> getPortList() {
        return this.portList;
    }

    public void setPortList(List<Integer> portList) {
        this.portList = portList;
    }

    public List<RedisConfigInfo> getExtConfigList() {
        return this.extConfigList;
    }

    public void setExtConfigList(List<RedisConfigInfo> configList) {
        this.extConfigList = configList;
    }
}
