package com.sensor.db.bean;

import java.util.Date;
import java.util.List;

/**
 * Created by tianyi on 06/09/2017.
 */
public class DashboardBean {

    private Integer id;
    private Integer userId;
    private Integer showOrder;
    private Integer isDefault = Integer.valueOf(0);
    private Integer isPublic = Integer.valueOf(0);
    private String name;
    private Date createTime;
    private String username;
    private List<DashboardItemBean> items;
    private Integer projectId;
    private String config;

    public DashboardBean() {
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder("DashboardBean{");
        var1.append("id=").append(this.id);
        var1.append(", userId=").append(this.userId);
        var1.append(", showOrder=").append(this.showOrder);
        var1.append(", isDefault=").append(this.isDefault);
        var1.append(", isPublic=").append(this.isPublic);
        var1.append(", name=\'").append(this.name).append('\'');
        var1.append(", createTime=").append(this.createTime);
        var1.append(", username=\'").append(this.username).append('\'');
        var1.append(", items=").append(this.items);
        var1.append(", projectId=").append(this.projectId);
        var1.append(", config=\'").append(this.config).append('\'');
        var1.append('}');
        return var1.toString();
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

    public Integer getShowOrder() {
        return this.showOrder;
    }

    public void setShowOrder(Integer var1) {
        this.showOrder = var1;
    }

    public Integer getIsDefault() {
        return this.isDefault;
    }

    public void setIsDefault(Integer var1) {
        this.isDefault = var1;
    }

    public Integer getIsPublic() {
        return this.isPublic;
    }

    public void setIsPublic(Integer var1) {
        this.isPublic = var1;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String var1) {
        this.name = var1;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date var1) {
        this.createTime = var1;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String var1) {
        this.username = var1;
    }

    public List<DashboardItemBean> getItems() {
        return this.items;
    }

    public void setItems(List<DashboardItemBean> var1) {
        this.items = var1;
    }

    public Integer getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Integer var1) {
        this.projectId = var1;
    }

    public String getConfig() {
        return this.config;
    }

    public void setConfig(String var1) {
        this.config = var1;
    }
}
