package com.sensor.common.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyi on 31/08/2017.
 */
public class ImpalaConfigInfo {
    private List<String> jdbcUrlList = new ArrayList<>();
    private String user;
    private String password;
    private int maxPoolSize = 10;

    public ImpalaConfigInfo() {
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public List<String> getJdbcUrlList() {
        return this.jdbcUrlList;
    }

    public boolean addJdbcUrl(String jdbcUrl) {
        return this.jdbcUrlList.add(jdbcUrl);
    }

    public void setJdbcUrlList(List<String> jdbcUrlList) {
        this.jdbcUrlList = jdbcUrlList;
    }
}
