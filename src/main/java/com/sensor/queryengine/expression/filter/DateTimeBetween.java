package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;

/**
 * Created by tianyi on 02/09/2017.
 */
public class DateTimeBetween extends AbstractFilter {
    private Object lowerBound;
    private Object upperBound;

    public DateTimeBetween(AbstractColumn var1, Object var2, Object var3) {
        super(var1);
        this.lowerBound = var2;
        this.upperBound = var3;
    }

    public String constructSql() throws Exception {
        /*
        DataType var1 = this.column.getDataType();
        if(var1 != DataType.DATE && var1 != DataType.DATETIME) {
            throw new Exception("data type must be date / datetime: ");
        } else {
            Date var2 = DateUtil.tryParse(this.lowerBound.toString());
            Date var3 = DateUtil.tryParse(this.upperBound.toString());
            if(var2 != null && var3 != null) {
                int var4 = DateUtil.getPartOfDate(var3, DateUnit.HOUR);
                int var5 = DateUtil.getPartOfDate(var3, DateUnit.MINUTE);
                if(var4 == 0 && var5 == 0) {
                    var3 = DateUtil.nextDateUnit(var3, DateUnit.DAY);
                } else if(var5 == 0) {
                    var3 = DateUtil.nextDateUnit(var3, DateUnit.HOUR);
                } else {
                    var3 = DateUtil.nextDateUnit(var3, DateUnit.MINUTE);
                }

                return String.format("%s >= %d AND %s < %d", new Object[]{this.column.getId(), Long.valueOf(var2.getTime()), this.column.getId(), Long.valueOf(var3.getTime())});
            } else {
                throw new Exception("fail to parse params");
            }
        }
        */
        return null;
    }
}
