package com.sensor.db.bean;

import java.util.Comparator;

/**
 * Created by tianyi on 06/09/2017.
 */
public class DashboardItemBean {
    private int id;
    private int bookmarkId;
    private int dashboardId;
    private String config;
    private int showOrder;
    private BookmarkBean bookmark;

    public DashboardItemBean() {
    }

    public int getBookmarkId() {
        return this.bookmarkId;
    }

    public void setBookmarkId(int var1) {
        this.bookmarkId = var1;
    }

    public int getDashboardId() {
        return this.dashboardId;
    }

    public void setDashboardId(int var1) {
        this.dashboardId = var1;
    }

    public String getConfig() {
        return this.config;
    }

    public void setConfig(String var1) {
        this.config = var1;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getShowOrder() {
        return this.showOrder;
    }

    public void setShowOrder(int var1) {
        this.showOrder = var1;
    }

    public BookmarkBean getBookmark() {
        return this.bookmark;
    }

    public void setBookmark(BookmarkBean var1) {
        this.bookmark = var1;
    }

    public static class ComparatorByOrder implements Comparator<DashboardItemBean> {
        public ComparatorByOrder() {
        }

        public int compare(DashboardItemBean var1, DashboardItemBean var2) {
            return var1.getShowOrder() - var2.getShowOrder();
        }
    }
}
