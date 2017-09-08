package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;

/**
 * Created by tianyi on 02/09/2017.
 */
public class IsTrue  extends AbstractFilter {
    public IsTrue(AbstractColumn column) {
        super(column);
    }

    public String constructSql() throws Exception {
        return String.format("%s = 1", this.column.getId());
    }
}
