package com.sensor.common.config;

import java.util.List;

/**
 * Created by tianyi on 23/08/2017.
 */
public class MetaConfigInfo {
    private List<String> jdbcUrlList = null;
    private int masterIndex = 0;
    private String user = null;
    private String password = null;

    public MetaConfigInfo() {
    }

    public void setJdbcUrlList(List<String> jdbcUrlList) {
        this.jdbcUrlList = jdbcUrlList;
    }

    public void setMasterIndex(int masterIndex) {
        this.masterIndex = masterIndex;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getJdbcUrlList() {
        return this.jdbcUrlList;
    }

    public int getMasterIndex() {
        return this.masterIndex;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public String toString() {
        return "MetaConfigInfo{jdbcUrlList=" + this.jdbcUrlList + ", masterIndex=" + this.masterIndex + ", user=\'" + this.user + '\'' + ", password=\'" + this.password + '\'' + '}';
    }
}
