package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;

/**
 * Created by tianyi on 02/09/2017.
 */
public class SimpleFilter extends  AbstractFilter{
    protected String operator;
    protected Object value;

    public SimpleFilter(AbstractColumn var1, String var2, Object var3) {
        super(var1);
        this.operator = var2;
        this.value = var3;
    }

    public String constructSql() throws Exception {
        String var2 = this.column.getId();
        String var1;
        if(this.value instanceof Number) {
            var1 = this.value.toString();
        } else {
            var1 = String.format("\'%s\'", this.value);
        }

        return String.format("%s %s %s", var2, this.operator, var1);
    }
}
