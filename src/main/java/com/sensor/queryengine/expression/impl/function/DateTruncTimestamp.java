package com.sensor.queryengine.expression.impl.function;

import com.sensor.common.util.DateUnit;
import com.sensor.common.util.DateUtil;
import com.sensor.db.OLAPEngineConnectionPool;
import com.sensor.db.OLAPEngineType;
import com.sensor.queryengine.expression.AbstractColumn;
import com.sensor.queryengine.expression.ExecutableExpression;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by tianyi on 05/09/2017.
 */
public class DateTruncTimestamp implements ExecutableExpression{

    private DateUnit unit;

    public DateTruncTimestamp(DateUnit var1) {
        this.unit = DateUnit.DAY;
        this.unit = var1;
    }

    public DateTruncTimestamp() {
        this.unit = DateUnit.DAY;
    }

    public DateUnit getUnit() {
        return this.unit;
    }

    public void setUnit(DateUnit var1) {
        this.unit = var1;
    }

    public String eval(List<AbstractColumn> var1) throws Exception {
        if(var1.size() != 1) {
            throw new SQLException("invalid param number");
        } else {
            return OLAPEngineConnectionPool.getEngineType() == OLAPEngineType.VERTICA?String.format("TRUNC(TO_TIMESTAMP(%s/1000), \'%s\')", new Object[]{((AbstractColumn)var1.get(0)).getId(), DateUtil.getSQLTruncUnit(this.unit)}):String.format("TRUNC(FROM_UNIXTIME(CAST(%s/1000 AS bigint)), \'%s\')", new Object[]{((AbstractColumn)var1.get(0)).getId(), DateUtil.getSQLTruncUnit(this.unit)});
        }
    }
}
