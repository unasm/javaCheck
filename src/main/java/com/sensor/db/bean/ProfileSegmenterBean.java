package com.sensor.db.bean;

import java.util.Date;

/**
 * Created by tianyi on 27/08/2017.
 */
public class ProfileSegmenterBean {
    private int id;
    private String name;
    private String cname;
    private int type;
    private String status;
    private Date successTime;
    private String content;
    private int projectId;
    private String lastPartition;
    private String defaultValue;
    private Date updateTime;

    public ProfileSegmenterBean() {
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

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getSuccessTime() {
        return this.successTime;
    }

    public void setSuccessTime(Date successTime) {
        this.successTime = successTime;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getProjectId() {
        return this.projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getLastPartition() {
        return this.lastPartition;
    }

    public void setLastPartition(String lastPartition) {
        this.lastPartition = lastPartition;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
