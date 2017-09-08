package com.sensor.queryengine.response;

import java.util.List;

/**
 * Created by tianyi on 17/08/2017.
 */
public class UserResponse extends QueryResponse {
    private List<String> columnName;
    private List<UserProfile> users;
    private int pageNum;
    private int size;

    public UserResponse() {
    }

    public List<String> getColumnName() {
        return this.columnName;
    }

    public void setColumnName(List<String> columns) {
        this.columnName = columns;
    }

    public List<UserProfile> getUsers() {
        return this.users;
    }

    public void setUsers(List<UserProfile> users) {
        this.users = users;
    }

    public int getPageNum() {
        return this.pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
