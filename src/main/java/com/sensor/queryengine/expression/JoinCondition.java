package com.sensor.queryengine.expression;

/**
 *
 *  JoinCondition 是用于 Table  需要连接 查询的时候的对象
 *
 * Created by tianyi on 21/08/2017.
 */
public class JoinCondition {
    protected AbstractColumn left;
    protected String operator;
    protected AbstractColumn right;

    public JoinCondition(AbstractColumn left, String operator, AbstractColumn right) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public String constructSql() throws Exception {
        String left = this.getExpression(this.left);
        String right = this.getExpression(this.right);
        return String.format("%s %s %s", left, this.operator, right);
    }

    private String getExpression(AbstractColumn column) throws Exception {
        String res = "";
        if (column instanceof ExpressionColumn) {
            ExpressionColumn expressionColumn= (ExpressionColumn)column;
            res = expressionColumn.getExpression().eval(expressionColumn.getColumns());
        } else {
            res = column.getId();
        }
        return res;
    }
}
