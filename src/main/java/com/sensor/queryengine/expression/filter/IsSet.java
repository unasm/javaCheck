package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;

/**
 * Created by tianyi on 02/09/2017.
 */
public class IsSet extends AbstractFilter{
    public IsSet(AbstractColumn var1) {
        super(var1);
    }

    public String constructSql() throws Exception {
        return String.format("%s is not null", new Object[]{this.column.getId()});
    }
}
