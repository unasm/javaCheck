package com.sensor.queryengine.expression.filter;

import com.sensor.common.DataType;
import com.sensor.queryengine.expression.AbstractColumn;

/**
 * Created by tianyi on 02/09/2017.
 */
public class IsEmpty extends AbstractFilter{
    private boolean reverse;

    public IsEmpty(AbstractColumn var1) {
        super(var1);
    }

    public IsEmpty(AbstractColumn var1, boolean var2) {
        super(var1);
        this.reverse = var2;
    }

    public String constructSql() throws Exception {
        if(this.column.getDataType() != DataType.LIST) {
            throw new Exception("data type must be list.");
        } else {
            return this.reverse?String.format("(%s is not null and %s != \'\')", this.column.getId(), this.column.getId())
                    :String.format("(%s = \'\')", this.column.getId(), this.column.getId());
        }
    }
}
