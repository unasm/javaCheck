package com.sensor.queryengine.expression;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyi on 03/09/2017.
 */
public class ExpressionColumn extends  AbstractColumn{
    private ExecutableExpression expression;
    private List<AbstractColumn> columns = new ArrayList<>();
    private Window window;

    public ExpressionColumn(String alias, AbstractTable table, ExecutableExpression expression) {
        super(alias, table);
        this.expression = expression;
    }

    public ExpressionColumn(String alias, AbstractTable table, ExecutableExpression expression, String... fields) throws SQLException {
        super(alias, table);
        this.expression = expression;
        //String[] var5 = var4;
        int len = fields.length;

        for(int i = 0; i < len; ++i) {
            String fieldName = fields[i];
            this.addColumn(table.getColumn(fieldName));
        }

    }

    public ExpressionColumn(String alias, AbstractTable table, ExecutableExpression expression, AbstractColumn... columns) throws SQLException {
        super(alias, table);
        this.expression = expression;
        //AbstractColumn[] var5 = var4;
        int var6 = columns.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            AbstractColumn column = columns[var7];
            this.addColumn(column);
        }

    }

    public ExpressionColumn(String alias, AbstractTable table, ExecutableExpression expression, Window window, AbstractColumn... columns) {
        super(alias, table);
        this.expression = expression;
        this.window = window;
        //AbstractColumn[] var6 = var5;
        int length = columns.length;

        for(int i = 0; i < length; ++i) {
            AbstractColumn column = columns[i];
            this.addColumn(column);
        }

    }

    public String constructSql() throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append(this.expression.eval(this.columns));
        if(null != this.window) {
            sql.append(" OVER ");
            sql.append(this.window.constructSql());
        }

        sql.append(" AS ");
        sql.append(this.alias.replace("$", ""));
        return sql.toString();
    }

    public String getId() throws Exception {
        return this.expression.eval(this.columns);
    }

    public void addColumn(AbstractColumn column) {
        this.columns.add(column);
        if(this.columns.size() == 1) {
            this.valueMapping = column.isValueMapping();
        } else {
            this.valueMapping = this.valueMapping || column.isValueMapping();
        }

    }

    public ExecutableExpression getExpression() {
        return this.expression;
    }

    public void setExpression(ExecutableExpression expression) {
        this.expression = expression;
    }

    public List<AbstractColumn> getColumns() {
        return this.columns;
    }

    public void setColumns(List<AbstractColumn> columns) {
        this.columns = columns;
    }

    public String toString() {
        return (new ToStringBuilder(this)).append("expression", this.expression).append("columns", this.columns).append("window", this.window).toString();
    }
}
