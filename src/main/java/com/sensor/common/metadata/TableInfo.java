package com.sensor.common.metadata;

/**
 *
 * table_define 表的信息
 *
 * Created by tianyi on 14/08/2017.
 */
public class TableInfo {
    private int tableId;
    private  String tableName;
    private String db;
    private  String hdfsPath;
    private  int projectId;

    public TableInfo(int tableId, String tableName, String db, String hdfsPath, int projectId) {
        this.tableId = tableId;
        this.tableName = tableName;
        this.db = db;
        this.hdfsPath = hdfsPath;
        this.projectId = projectId;
    }

    public int getProjectId() {
        return this.projectId;
    }

    public int getTableId() {
        return this.tableId;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getDb() {
        return this.db;
    }

    public String getHdfsPath() {
        return this.hdfsPath;
    }
}
