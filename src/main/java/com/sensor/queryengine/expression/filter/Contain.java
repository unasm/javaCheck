package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;
import com.sensor.queryengine.query.SQLQueryService;

/**
 * Created by tianyi on 02/09/2017.
 */
public class Contain extends AbstractFilter{
    private String text;
    private boolean reverse = false;

    public Contain(AbstractColumn var1, Object var2, boolean var3) {
        super(var1);
        this.text = (String)var2;
        this.reverse = var3;
    }

    public String constructSql() throws Exception {
        return String.format("%s %s LIKE \'%%%s%%\'", new Object[]{this.column.getId(), this.reverse?"NOT":"", SQLQueryService.escapeString(this.text).replace("%", "\\%")});
    }
}
