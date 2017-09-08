package com.sensor.db.bean;

import java.util.Date;

/**
 * Created by tianyi on 26/08/2017.
 */
public class SessionBean {
    private int id;
    private String name;
    private String cname;
    private Date createTime;
    private String sessionRule;
    private String eventList;
    private int projectId;
    private Date updateTime;

    public SessionBean() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCname() {
        return this.cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getSessionRule() {
        return this.sessionRule;
    }

    public void setSessionRule(String rule) {
        this.sessionRule = rule;
    }

    public String getEventList() {
        return this.eventList;
    }

    public void setEventList(String eventList) {
        this.eventList = eventList;
    }

    public int getProjectId() {
        return this.projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
