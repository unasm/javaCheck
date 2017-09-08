package com.sensor.common.config;

import java.util.ArrayList;

/**
 * Created by tianyi on 17/08/2017.
 */
public class HiveConfigInfo {
    private ArrayList<String> hiveUrlList = null;
    private String hiveUser = null;
    private String hivePassword;
    private int maxPoolSize = 10;

    public HiveConfigInfo() {
    }

    public void setHiveUrlList(ArrayList<String> var1) {
        this.hiveUrlList = var1;
    }

    public void setHiveUser(String var1) {
        this.hiveUser = var1;
    }

    public String getHiveUser() {
        return this.hiveUser;
    }

    public ArrayList<String> getHiveUrlList() {
        return this.hiveUrlList;
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public void setMaxPoolSize(int var1) {
        this.maxPoolSize = var1;
    }

    public String getHivePassword() {
        return this.hivePassword;
    }

    public void setHivePassword(String var1) {
        this.hivePassword = var1;
    }
}
