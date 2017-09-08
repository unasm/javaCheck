package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;

/**
 * Created by tianyi on 02/09/2017.
 */
public class IsFalse extends  AbstractFilter{
    public IsFalse(AbstractColumn var1) {
        super(var1);
    }

    public String constructSql() throws Exception {
        return String.format("%s = 0", new Object[]{this.column.getId()});
    }
}
