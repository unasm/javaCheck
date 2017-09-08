package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;
import com.sensor.queryengine.query.SQLQueryService;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by tianyi on 17/08/2017.
 */
public class Equal extends AbstractFilter {
    private boolean not = false;
    private Collection<Object> values;

    public Equal(AbstractColumn var1, boolean var2, Collection<Object> var3) {
        super(var1);
        this.not = var2;
        this.values = var3;
    }

    public String constructSql() throws Exception {
        StringBuilder var1 = new StringBuilder();
        boolean var2 = false;

        for(Iterator var3 = this.values.iterator(); var3.hasNext(); var2 = true) {
            Object var4 = var3.next();
            if(var2) {
                var1.append(",");
            }

            if(var4 instanceof Number) {
                var1.append(String.format("%s", new Object[]{var4}));
            } else if(var4 instanceof Date) {
                var1.append(((Date)var4).getTime());
            } else if(var4 == null) {
                var1.append("null");
            } else {
                var1.append(String.format("\'%s\'", new Object[]{SQLQueryService.escapeString(var4.toString())}));
            }
        }

        String var5;
        if(this.values.size() > 1) {
            if(this.not) {
                var5 = "not in";
            } else {
                var5 = "in";
            }

            return String.format("%s %s (%s)", new Object[]{this.column.getId(), var5, var1.toString()});
        } else {
            if(this.not) {
                var5 = "!=";
            } else {
                var5 = "=";
            }

            return String.format("%s %s %s", new Object[]{this.column.getId(), var5, var1.toString()});
        }
    }
}
