package com.sensor.common.config;

/**
 * Created by tianyi on 31/08/2017.
 */
public class VerticaConfigInfo {
    private String jdbcUrl;
    private String jdbcBackupUrl;
    private String user;
    private String password;
    private int maxPoolSize = 10;

    public VerticaConfigInfo() {
    }

    public String getJdbcUrl() {
        return this.jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcBackupUrl() {
        return this.jdbcBackupUrl;
    }

    public void setJdbcBackupUrl(String jdbcBackupUrl) {
        this.jdbcBackupUrl = jdbcBackupUrl;
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
}
