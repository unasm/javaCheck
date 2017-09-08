package com.sensor.db.bean;

import java.util.Date;

/**
 * Created by tianyi on 25/08/2017.
 */
public class ProjectBean {
    private Integer id;
    private String name;
    private String cname;
    private Date deleteTime;
    private Date createTime;
    private Integer status;
    private String superToken;
    private String normalToken;
    private Boolean isAutoCreate = true;

    public ProjectBean() {
    }

    public String toString() {
        StringBuilder string = new StringBuilder("ProjectBean{");
        string.append("id=").append(this.id);
        string.append(", name=\'").append(this.name).append('\'');
        string.append(", cname=\'").append(this.cname).append('\'');
        string.append(", deleteTime=").append(this.deleteTime);
        string.append(", createTime=").append(this.createTime);
        string.append(", status=").append(this.status);
        string.append(", superToken=\'").append(this.superToken).append('\'');
        string.append(", normalToken=\'").append(this.normalToken).append('\'');
        string.append(", isAutoCreate=").append(this.isAutoCreate);
        string.append('}');
        return string.toString();
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
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

    public void setCname(String name) {
        this.cname = name;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getDeleteTime() {
        return this.deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSuperToken() {
        return this.superToken;
    }

    public void setSuperToken(String token) {
        this.superToken = token;
    }

    public String getNormalToken() {
        return this.normalToken;
    }

    public void setNormalToken(String token) {
        this.normalToken = token;
    }

    public Boolean getAutoCreate() {
        return this.isAutoCreate;
    }

    public void setAutoCreate(Boolean isAutoCreate) {
        this.isAutoCreate = isAutoCreate;
    }
}
