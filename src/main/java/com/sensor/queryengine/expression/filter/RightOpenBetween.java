package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;

import java.util.Date;

/**
 * Created by tianyi on 02/09/2017.
 */
public class RightOpenBetween  extends AbstractFilter{
    private Object lowerBound;
    private Object upperBound;

    public RightOpenBetween(AbstractColumn var1, Object var2, Object var3) {
        super(var1);
        this.lowerBound = var2;
        this.upperBound = var3;
    }

    public String constructSql() throws Exception {
        String var1 = this.column.getId();
        if(!(this.lowerBound instanceof Number) && !(this.upperBound instanceof Number)) {
            Date var2 = (Date)this.lowerBound;
            Date var3 = (Date)this.upperBound;
            return String.format("%s >= %d AND %s < %d", this.column.getId(), var2.getTime(), this.column.getId(), var3.getTime());
        } else {
            return this.lowerBound == null?String.format("%s < %s", var1, this.upperBound):(this.upperBound == null ?
                            String.format("%s >= %s", var1, this.lowerBound) :
                    String.format("%s >= %s AND %s < %s", var1, this.lowerBound, var1, this.upperBound));
        }
    }
}
