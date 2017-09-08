package com.sensor.queryengine.executor.impl;

import com.sensor.queryengine.expression.SqlGenerator;

/**
 * Created by tianyi on 21/08/2017.
 */
public class OrderBy implements SqlGenerator {
    private boolean isAsc = true;
    private int orderByIndex = 0;

    public OrderBy(int index, boolean isAsc) {
        this.orderByIndex = index;
        this.isAsc = isAsc;
    }

    public String constructSql() throws Exception {
        return String.format("%d %s ", this.orderByIndex, this.isAsc ? "ASC":"DESC");
    }
}
