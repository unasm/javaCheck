package com.sensor.queryengine.expression;

/**
 * Created by tianyi on 17/08/2017.
 */
public class Limit implements  SqlGenerator{
    private long value;

    public Limit(Long var1) {
        this.value = var1.longValue();
    }

    public String constructSql() throws Exception {
        return String.format("LIMIT %d", new Object[]{Long.valueOf(this.value)});
    }
}
