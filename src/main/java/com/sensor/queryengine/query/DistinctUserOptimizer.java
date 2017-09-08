package com.sensor.queryengine.query;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by tianyi on 18/08/2017.
 */
public class DistinctUserOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(DistinctUserOptimizer.class);
    private static final String ALIAS_MIDDLE_CNT = "__middle_cnt__";

    public DistinctUserOptimizer() {
    }

    public static String optimize(String sql) {
        if(sql.toLowerCase().contains("funnel_")) {
            return sql;
        } else {
            Statement statement;
            try {
                statement = CCJSqlParserUtil.parse(sql);
            } catch (JSQLParserException var4) {
                logger.warn("fail to optimize sql, fail to parse", var4);
                return sql;
            }

            if(!(statement instanceof Select)) {
                return sql;
            } else {
                //如果是select的请求
                Select select = (Select)statement;
                DistinctUserOptimizer.OptimizerSelectVisitor visitor = new DistinctUserOptimizer.OptimizerSelectVisitor(null, null);
                select.getSelectBody().accept(visitor);
                return visitor.getParentSelect() != null ? visitor.getParentSelect().toString() : select.toString();
            }
        }
    }

    private static class OptimizerSelectVisitor implements SelectVisitor {
        private PlainSelect parentSelect = null;
        private Alias alias = null;

        public OptimizerSelectVisitor(PlainSelect parentSelect, Alias alias) {
            this.parentSelect = parentSelect;
            this.alias = alias;
        }

        public PlainSelect getParentSelect() {
            return this.parentSelect;
        }

        public void visit(final PlainSelect var1) {
            final boolean[] var2 = new boolean[]{false};
            final SelectExpressionItem[] var3 = new SelectExpressionItem[]{null};
            if(var1.getSelectItems().size() > 0 && var1.getGroupByColumnReferences() != null && var1.getGroupByColumnReferences().size() > 0) {
                Iterator var4 = var1.getSelectItems().iterator();

                while(var4.hasNext()) {
                    SelectItem var5 = (SelectItem)var4.next();
                    var5.accept(new SelectItemVisitor() {
                        public void visit(AllColumns var1) {
                        }

                        public void visit(AllTableColumns var1) {
                        }

                        public void visit(SelectExpressionItem var1) {
                            //Alias var2x = var1.getAlias();
                            //if(var2x != null && (var2x.getName().equals("distUser") || var2x.getName().equals("distUserAvg")) && (var1.getExpression() instanceof Function || var1.getExpression() instanceof Division)) {
                            //    var2[0] = true;
                            //    var3[0] = var1;
                            //}

                        }
                    });
                }
            }

            Function var20 = null;
            Function var21 = null;
            if(var2[0]) {
                PlainSelect var6 = new PlainSelect();
                ArrayList var7 = new ArrayList();
                int var8 = 1;
                Iterator var9 = var1.getSelectItems().iterator();

                SelectExpressionItem var11;
                while(var9.hasNext()) {
                    SelectItem var10 = (SelectItem)var9.next();
                    if(var10 instanceof SelectExpressionItem) {
                        var11 = (SelectExpressionItem)var10;
                        if(var10 != var3[0]) {
                            SelectExpressionItem var12 = new SelectExpressionItem();
                            var12.setAlias(var11.getAlias());
                            var12.setExpression(new Column(var11.getAlias().getName()));
                            var6.addSelectItems(new SelectItem[]{var12});
                            var7.add(new LongValue((long)var8));
                            ++var8;
                        } else {
                            Expression var25 = var11.getExpression();
                            Function var14 = new Function();
                            var14.setName("COUNT");
                            var14.setAllColumns(true);
                            Object var13;
                            if(var25 instanceof Division) {
                                Division var15 = (Division)var25;
                                Function var16 = (Function)var15.getLeftExpression();
                                Function var17 = new Function();
                                var17.setName("SUM");
                                ExpressionList var18 = new ExpressionList();
                                var18.setExpressions(Collections.singletonList(new Column("__middle_cnt__")));
                                var17.setParameters(var18);
                                var20 = new Function();
                                if(var16.getName().equalsIgnoreCase("SUM")) {
                                    var20.setName("SUM");
                                    var20.setParameters(var16.getParameters());
                                } else {
                                    var20.setName("COUNT");
                                    var20.setAllColumns(true);
                                }

                                Division var19 = new Division();
                                var19.setLeftExpression(var17);
                                var19.setRightExpression(var14);
                                var13 = var19;
                                var21 = (Function)var15.getRightExpression();
                            } else {
                                var13 = var14;
                                var21 = (Function)var3[0].getExpression();
                            }

                            SelectExpressionItem var28 = new SelectExpressionItem();
                            var28.setAlias(var11.getAlias());
                            var28.setExpression((Expression)var13);
                            var6.addSelectItems(new SelectItem[]{var28});
                        }
                    }
                }

                if(var21 == null) {
                    return;
                }

                Column var22 = (Column)var21.getParameters().getExpressions().get(0);
                var3[0].setExpression(var22);
                var3[0].setAlias(null);
                ArrayList var23 = new ArrayList();

                for(int var24 = 0; var24 < var1.getSelectItems().size(); ++var24) {
                    var23.add(new LongValue((long)(var24 + 1)));
                }

                var1.setGroupByColumnReferences(var23);
                if(var20 != null) {
                    var11 = new SelectExpressionItem();
                    var11.setExpression(var20);
                    var11.setAlias(new Alias("__middle_cnt__"));
                    var1.addSelectItems(new SelectItem[]{var11});
                }

                SubSelect var26 = new SubSelect();
                var26.setSelectBody(var1);
                var26.setAlias(new Alias("wrapper"));
                var6.setFromItem(var26);
                var6.setGroupByColumnReferences(var7);
                if(var1.getLimit() != null) {
                    var6.setLimit(var1.getLimit());
                    var1.setLimit((Limit)null);
                }

                if(this.parentSelect != null) {
                    SubSelect var27 = new SubSelect();
                    var27.setAlias(this.alias);
                    var27.setSelectBody(var6);
                    this.parentSelect.setFromItem(var27);
                } else {
                    this.parentSelect = var6;
                }
            }

            var1.getFromItem().accept(new FromItemVisitor() {
                public void visit(Table var1x) {
                }

                public void visit(SubSelect var1x) {
                    DistinctUserOptimizer.OptimizerSelectVisitor var2 = new DistinctUserOptimizer.OptimizerSelectVisitor(var1, var1x.getAlias());
                    var1x.getSelectBody().accept(var2);
                }

                public void visit(SubJoin var1x) {
                }

                public void visit(LateralSubSelect var1x) {
                }

                public void visit(ValuesList var1x) {
                }
                public void visit(TableFunction func) {

                }
            });
        }

        public void visit(SetOperationList var1) {
        }

        public void visit(WithItem var1) {
        }
    }
}
