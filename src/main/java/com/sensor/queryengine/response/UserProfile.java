package com.sensor.queryengine.response;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户的profile bean
 * Created by tianyi on 17/08/2017.
 */
public class UserProfile {
    private  String id;
    private  String firstId;
    private  String secondId;
    private  String distinctId;
    private  Map<String, Object> profiles = new HashMap<>();

    public UserProfile() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstId() {
        return this.firstId;
    }

    public void setFirstId(String firstId) {
        this.firstId = firstId;
    }

    public String getSecondId() {
        return this.secondId;
    }

    public void setSecondId(String secondId) {
        this.secondId = secondId;
    }

    public String getDistinctId() {
        return this.distinctId;
    }

    public void setDistinctId(String distinctId) {
        this.distinctId = distinctId;
    }

    public Map<String, Object> getProfiles() {
        return this.profiles;
    }

    public void setProfiles(Map<String, Object> profiles) {
        this.profiles = profiles;
    }
}
