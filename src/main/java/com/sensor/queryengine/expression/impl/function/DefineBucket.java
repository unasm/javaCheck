package com.sensor.queryengine.expression.impl.function;

import com.sensor.queryengine.expression.AbstractColumn;
import com.sensor.queryengine.expression.ExecutableExpression;
import com.sensor.queryengine.expression.filter.Greater;
import com.sensor.queryengine.expression.filter.Less;
import com.sensor.queryengine.expression.filter.RightOpenBetween;
import com.sensor.queryengine.util.NumericUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by tianyi on 05/09/2017.
 */
public class DefineBucket implements ExecutableExpression{

    private List<Number> byDefine;
    private boolean needDivThousand = true;

    public void setNeedDivThousand(boolean var1) {
        this.needDivThousand = var1;
    }

    public DefineBucket(List<Number> var1) {
        this.byDefine = var1;
    }

    public List<Number> getByDefine() {
        return this.byDefine;
    }

    private long getLongValue(Number var1) {
        return this.needDivThousand? NumericUtil.toLong(var1):var1.longValue();
    }

    public String eval(List<AbstractColumn> var1) throws Exception {
        if(var1 != null && var1.size() != 0) {
            StringBuilder var2 = new StringBuilder();
            var2.append("CASE ");
            long var3 = this.getLongValue(this.byDefine.get(0));
            Less var5 = new Less(var1.get(0), var3);
            var2.append(String.format("WHEN %s THEN %s ", new Object[]{var5.constructSql(), Integer.valueOf(-1)}));
            int var6 = this.byDefine.size() - 1;

            for(int var7 = 0; var7 < var6; ++var7) {
                long var8 = this.getLongValue(this.byDefine.get(var7));
                long var10 = this.getLongValue(this.byDefine.get(var7 + 1));
                RightOpenBetween var12 = new RightOpenBetween(var1.get(0), var8, var10);
                var2.append(String.format("WHEN %s THEN %s ", new Object[]{var12.constructSql(), Integer.valueOf(var7)}));
            }

            long var13 = this.getLongValue((Number)this.byDefine.get(var6));
            Greater var9 = new Greater(var1.get(0), false, var13);
            var2.append(String.format("WHEN %s THEN %s ", new Object[]{var9.constructSql(), Integer.valueOf(var6)}));
            var2.append("END");
            return var2.toString();
        } else {
            throw new SQLException("invalid column size");
        }
    }
}
