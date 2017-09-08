package com.sensor.queryengine.expression.filter;

import com.sensor.common.util.DateUnit;
import com.sensor.common.util.DateUtil;
import com.sensor.queryengine.expression.AbstractColumn;

import java.util.Date;

/**
 * Created by tianyi on 02/09/2017.
 */
public class DateTimeBefore extends AbstractFilter{
    protected Object value;

    public DateTimeBefore(AbstractColumn var1, Object var2) {
        super(var1);
        this.value = var2;
    }

    public String constructSql() throws Exception {
        Date var1 = DateUtil.parseInputDate(this.value.toString(), DateUnit.DAY);
        if (var1 == null) {
            throw new Exception("fail to parse date");
        } else {
            return String.format("%s < %s", this.column.getId(), var1.getTime());
        }
    }
}
