package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;
import com.sensor.queryengine.expression.SqlGenerator;

import java.io.Serializable;

/**
 *
 * filter 对象 基类, 过滤器，where条件
 *
 * Created by tianyi on 01/09/2017.
 */
public abstract class AbstractFilter  implements SqlGenerator, Serializable{
    protected AbstractColumn column;

    public AbstractFilter(AbstractColumn column) {
        this.column = column;
    }

    public AbstractColumn getColumn() {
        return this.column;
    }

    public void setColumn(AbstractColumn var1) {
        this.column = var1;
    }

    public String toString() {
        try {
            return this.constructSql();
        } catch (Exception ex) {
            return "constructSql failed";
        }
    }
}
