package com.sensor.queryengine.executor.impl;

import com.sensor.common.DataType;
import com.sensor.queryengine.expression.AbstractTable;
import com.sensor.queryengine.expression.Table;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyi on 17/08/2017.
 */
public class MultiEventTable extends AbstractTable {
    private Map<String, Table> eventTables = new HashMap<>();
    private String name = null;

    public MultiEventTable(String alias, String name, Integer samplingFactor) throws SQLException {
        super(alias);
        this.name = name;
        this.samplingFactor = samplingFactor;
        this.createColumn("$date", "$date", 0, DataType.UNKNOWN.getIndex());
        this.createColumn("$day", "$day", 0, DataType.DATE.getIndex());
        this.createColumn("$time", "$time", 0, DataType.UNKNOWN.getIndex());
        this.createColumn("$user_id", "$user_id", 0, DataType.UNKNOWN.getIndex());
        this.createColumn("$distinct_id", "$distinct_id", 0, DataType.STRING.getIndex());
        this.createColumn("$event_id", "$event_id", 0, DataType.UNKNOWN.getIndex());
        this.createColumn("$month_id", "$month_id", 0, DataType.NUMBER.getIndex());
        this.createColumn("$week_id", "$week_id", 0, DataType.NUMBER.getIndex());
        this.createColumn("$event_bucket", "$event_bucket", 0, DataType.NUMBER.getIndex());
        this.addSamplingFilter(this.getColumn("$user_id"));
        this.eventBucketColumn = this.getColumn("$event_bucket");
    }

    public Table getEventTable(String var1) {
        return this.eventTables.get(var1);
    }

    public void addEventTable(String var1, Table var2) {
        this.eventTables.put(var1, var2);
    }

    public Map<String, Table> getEventTables() {
        return this.eventTables;
    }

    public String getName() {
        return this.name;
    }

    /**
     * 用于创建sql 语句
     *
     * @return
     * @throws Exception
     */

    public String constructSql() throws Exception {
        String selectSql = this.constructSelectColumn();
        String whereSql = this.constructWhere();
        //if(var1.equals(this.tableAlias) && this.groupByNum == null && this.window == null && this.limit == null && StringUtils.isEmpty(var2)) {
        if (selectSql.equals(this.tableAlias) && this.groupByNum == null && this.limit == null && StringUtils.isEmpty(whereSql)) {
            return this.name;
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append(selectSql);
            sql.append("\n\tFROM ");
            sql.append(this.name);
            sql.append(" ");
            sql.append(this.alias);
            if (whereSql.length() > 0) {
                sql.append("\n\tWHERE ");
                sql.append(whereSql);
            }

            if (null != this.window) {
                sql.append("\n\t");
                sql.append(this.window.constructSql());
            }
            return sql.toString();
        }
    }

    public String constructWhere() throws Exception {
        StringBuilder sql = new StringBuilder();
        String where = super.constructWhere();
        if (where.length() > 0) {
            sql.append("(");
            sql.append(where);
            sql.append(")");
        }

        return sql.toString();
    }
}
