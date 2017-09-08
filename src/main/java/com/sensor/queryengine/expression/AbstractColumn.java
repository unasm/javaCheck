package com.sensor.queryengine.expression;

import com.sensor.common.DataType;

/**
 * Created by tianyi on 14/08/2017.
 */
public abstract class AbstractColumn implements  SqlGenerator, Cloneable{
    protected String alias;
    protected AbstractTable table;
    protected DataType dataType;
    protected String defaultValue;
    protected boolean valueMapping;

    public boolean isValueMapping() {
        return this.valueMapping;
    }

    public AbstractColumn(String alias, AbstractTable table) {
        this.dataType = DataType.UNKNOWN;
        this.defaultValue = null;
        this.valueMapping = false;
        this.alias = alias;
        this.table = table;
    }

    public abstract String getId() throws Exception;

    public void setDataType(DataType var1) {
        this.dataType = var1;
    }

    public DataType getDataType() {
        return this.dataType;
    }

    public String getAlias() {
        return this.alias.replace("$", "");
    }

    public String getRawAlias() {
        return this.alias;
    }

    public void setAlias(String var1) {
        this.alias = var1;
    }

    public AbstractTable getTable() {
        return this.table;
    }

    public void setTable(AbstractTable var1) {
        this.table = var1;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String var1) {
        this.defaultValue = var1;
    }

    public AbstractColumn clone() {
        try {
            return (AbstractColumn)super.clone();
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }
}
