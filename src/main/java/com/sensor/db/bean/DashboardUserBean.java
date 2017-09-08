package com.sensor.db.bean;

import java.util.Comparator;

/**
 * Created by tianyi on 06/09/2017.
 */
public class DashboardUserBean {
    private int id;
    private int userId;
    private int dashboardId;
    private int showOrder;

    public DashboardUserBean() {
    }

    public int getDashboardId() {
        return this.dashboardId;
    }

    public void setDashboardId(int var1) {
        this.dashboardId = var1;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int var1) {
        this.id = var1;
    }

    public int getShowOrder() {
        return this.showOrder;
    }

    public void setShowOrder(int var1) {
        this.showOrder = var1;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int var1) {
        this.userId = var1;
    }

    public static class ComparatorByOrder implements Comparator<DashboardUserBean> {
        public ComparatorByOrder() {
        }

        public int compare(DashboardUserBean var1, DashboardUserBean var2) {
            return var1.getShowOrder() - var2.getShowOrder();
        }
    }
}
