package com.sensor.db.bean;

/**
 * Created by tianyi on 05/09/2017.
 */
public class FunnelBean {
    private int id;
    private int userId;
    private String name;
    private int maxConvertTime;
    private String steps;
    private String comment;
    private int projectId;

    public FunnelBean() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxConvertTime() {
        return this.maxConvertTime;
    }

    public void setMaxConvertTime(int maxConvertTime) {
        this.maxConvertTime = maxConvertTime;
    }

    public String getSteps() {
        return this.steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String toString() {
        return "FunnelBean{id=" + this.id + ", userId=" + this.userId + ", name=\'" + this.name + '\'' + ", maxConvertTime=" + this.maxConvertTime + ", steps=\'" + this.steps + '\'' + ", comment=\'" + this.comment + '\'' + '}';
    }

    public int getProjectId() {
        return this.projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }
}
