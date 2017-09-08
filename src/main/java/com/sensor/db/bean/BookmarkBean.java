package com.sensor.db.bean;

import java.util.Date;
import java.util.List;

/**
 * Created by tianyi on 06/09/2017.
 */
public class BookmarkBean {
    private Integer id;
    private Integer userId;
    private String type;
    private String name;
    private String data;
    private String time;
    private Date createTime;
    private String relatedVirtualEventNames;
    private List<DashboardItemBean> dashboards;
    private Integer projectId;

    public BookmarkBean() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer var1) {
        this.id = var1;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer var1) {
        this.userId = var1;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String var1) {
        this.type = var1;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String var1) {
        this.name = var1;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String var1) {
        this.data = var1;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String var1) {
        this.time = var1;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date var1) {
        this.createTime = var1;
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder("BookmarkBean{");
        var1.append("id=").append(this.id);
        var1.append(", userId=").append(this.userId);
        var1.append(", type=\'").append(this.type).append('\'');
        var1.append(", name=\'").append(this.name).append('\'');
        var1.append(", data=\'").append(this.data).append('\'');
        var1.append(", time=\'").append(this.time).append('\'');
        var1.append(", createTime=").append(this.createTime);
        var1.append(", relatedVirtualEventNames=\'").append(this.relatedVirtualEventNames).append('\'');
        var1.append(", dashboards=").append(this.dashboards);
        var1.append(", projectId=").append(this.projectId);
        var1.append('}');
        return var1.toString();
    }

    public List<DashboardItemBean> getDashboards() {
        return this.dashboards;
    }

    public void setDashboards(List<DashboardItemBean> var1) {
        this.dashboards = var1;
    }

    public String getRelatedVirtualEventNames() {
        return this.relatedVirtualEventNames;
    }

    public void setRelatedVirtualEventNames(String var1) {
        this.relatedVirtualEventNames = var1;
    }

    public Integer getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Integer var1) {
        this.projectId = var1;
    }
}
