package com.sensor.db.bean;

import java.util.Date;

/**
 * event_define 表，或者 源数据表
 * Created by tianyi on 21/08/2017.
 */
public class EventBean {
    private int id;
    private String name;
    private String cname;
    private String comment;
    private Date createTime;
    private boolean visible;
    private boolean virtual;
    private Integer bucketId;
    private String virtualDefine;
    private int projectId;
    private Date updateTime;

    public EventBean() {
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

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Integer getBucketId() {
        return this.bucketId;
    }

    public void setBucketId(Integer bucketId) {
        this.bucketId = bucketId;
    }

    public boolean isVirtual() {
        return this.virtual;
    }

    public void setVirtual(boolean var1) {
        this.virtual = var1;
    }

    public String getVirtualDefine() {
        return this.virtualDefine;
    }

    public void setVirtualDefine(String var1) {
        this.virtualDefine = var1;
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
