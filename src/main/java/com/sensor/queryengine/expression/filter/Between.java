package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;

/**
 * Created by tianyi on 02/09/2017.
 */
public class Between extends AbstractFilter{
    private Object lowerBound;
    private Object upperBound;

    public Between(AbstractColumn var1, Object var2, Object var3) {
        super(var1);
        this.lowerBound = var2;
        this.upperBound = var3;
    }

    public String constructSql() throws Exception {
        String var1 = this.column.getId();
        return this.lowerBound instanceof Number?String.format("%s BETWEEN %s AND %s", new Object[]{var1, this.lowerBound, this.upperBound}):String.format("%s BETWEEN \'%s\' AND \'%s\'", new Object[]{var1, this.lowerBound, this.upperBound});
    }
}
