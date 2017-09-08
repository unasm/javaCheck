package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;
import com.sensor.queryengine.query.SQLQueryService;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tianyi on 02/09/2017.
 */
public class Include extends AbstractFilter{
    private List<Object> values;
    private boolean not = false;

    public Include(AbstractColumn var1, List<Object> var2) {
        super(var1);
        this.values = var2;
    }

    public Include(AbstractColumn var1, boolean var2, List<Object> var3) {
        super(var1);
        this.values = var3;
        this.not = var2;
    }

    public String constructSql() throws Exception {
        if(this.values == null) {
            throw new SQLException("invalid in filter expression, no value or too many values");
        } else {
            StringBuilder var1 = new StringBuilder();
            if(this.not) {
                var1.append(" not ");
            }

            var1.append("(");
            boolean var2 = true;
            Iterator var3 = this.values.iterator();

            while(var3.hasNext()) {
                Object var4 = var3.next();
                if(!var2) {
                    var1.append(" OR ");
                }

                var2 = false;
                if(var4 instanceof Number) {
                    var1.append("contains32(");
                    var1.append(this.column.getId());
                    var1.append(", ");
                    var1.append(var4);
                    var1.append(")");
                } else {
                    var1.append("find_in_list(\'");
                    var1.append(SQLQueryService.escapeString(var4.toString()));
                    var1.append("\', ");
                    var1.append(this.column.getId());
                    var1.append(")");
                }
            }

            var1.append(")");
            return var1.toString();
        }
    }
}
