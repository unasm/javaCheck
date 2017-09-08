package com.sensor.queryengine.expression.filter;

import com.sensor.db.OLAPEngineConnectionPool;
import com.sensor.db.OLAPEngineType;
import com.sensor.queryengine.expression.AbstractColumn;
import com.sensor.queryengine.query.SQLQueryService;

/**
 * Created by tianyi on 02/09/2017.
 */
public class RegexpLike extends AbstractFilter{
    private String text;
    private boolean reverse = false;

    public RegexpLike(AbstractColumn var1, Object var2, boolean var3) {
        super(var1);
        this.text = (String)var2;
        this.reverse = var3;
    }

    public String constructSql() throws Exception {
        return OLAPEngineConnectionPool.getEngineType() == OLAPEngineType.VERTICA?String.format("%s REGEXP_LIKE(%s, \'%s\')", new Object[]{this.reverse?"NOT":"", this.column.getId(), SQLQueryService.escapeString(this.text).replace("%", "\\%")}):String.format("%s %s REGEXP \'%s\'", new Object[]{this.reverse?"NOT":"", this.column.getId(), SQLQueryService.escapeString(this.text).replace("%", "\\%")});
    }
}
