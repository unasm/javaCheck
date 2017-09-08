package com.sensor.queryengine.executor.impl;

import com.sensor.db.OLAPEngineConnectionPool;
import com.sensor.db.OLAPEngineType;
import com.sensor.queryengine.expression.*;
import com.sensor.queryengine.query.SQLQueryService;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tianyi on 21/08/2017.
 */
public class JoinedTable  extends AbstractTable{
    private static final Logger logger = LoggerFactory.getLogger(JoinedTable.class);
    protected AbstractTable table;
    private List<Pair<AbstractTable, JoinType>> joinTables = new ArrayList<>();
    private List<List<JoinCondition>> joinConditions = new ArrayList<>();

    public List<Pair<AbstractTable, JoinedTable.JoinType>> getJoinTables() {
        return this.joinTables;
    }

    public JoinedTable(String alias, AbstractTable table) {
        super(alias);
        this.table = table;
        this.addColumns(table);
        this.setEventBucketColumn(table.getEventBucketColumn());
    }

    public void addJoinTable(AbstractTable table, List<JoinCondition> conditions) {
        this.addJoinTable(table, conditions, JoinedTable.JoinType.INNER);
    }

    public void addJoinTable(AbstractTable joinTable, List<JoinCondition> conditions, JoinedTable.JoinType joinType) {
        this.joinTables.add(MutablePair.of(joinTable, joinType));
        this.joinConditions.add(conditions);
        this.addColumns(joinTable);
    }

    private void addColumns(AbstractTable joinTable) {
        if (joinTable instanceof Table && ((Table)joinTable).isAtomTable()) {
            this.columnMap.putAll(joinTable.getColumnMap());
        } else if(joinTable instanceof MultiEventTable) {
            ((MultiEventTable)joinTable).getEventTables().values().forEach((key) -> {
                this.columnMap.putAll(key.getColumnMap());
            });
            this.columnMap.putAll(joinTable.getColumnMap());
        } else {
            int cnt = 1;

            for(Iterator iterator = joinTable.getColumns().iterator(); iterator.hasNext(); ++cnt) {
                AbstractColumn column = (AbstractColumn)iterator.next();
                AtomColumn atomColumn = new AtomColumn(String.format("%s_%d", joinTable.getAlias(), cnt), this, column);
                this.columnMap.put(atomColumn.getName(), atomColumn);
            }
        }

    }

    private String getTableExpression(AbstractTable table) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("\n");
        String subSql = table.constructSql();
        if (subSql.contains(" ")) {
            sql.append("(");
            sql.append(subSql);
            sql.append(")");
        } else {
            sql.append(subSql);
        }

        sql.append(" ");
        sql.append(table.getAlias());
        return sql.toString();
    }

    private String getJoinConditionExpression(List<JoinCondition> conditions) throws Exception {
        StringBuilder sql = new StringBuilder();
        int notFirst = 0;

        for(Iterator iterator = conditions.iterator(); iterator.hasNext(); ++notFirst) {
            JoinCondition condition = (JoinCondition)iterator.next();
            sql.append(' ');
            if (notFirst > 0) {
                sql.append(" AND ");
            }

            sql.append(condition.constructSql());
        }

        return sql.toString();
    }

    public void addSelectTable(String tableName) {
        this.tableAlias = String.format("%s.*", tableName);
    }

    public void addSelect(AbstractColumn selectColumn, String alias) {
        if (null != selectColumn) {
            boolean isFound = false;
            if (selectColumn.getTable() != this.table && selectColumn.getTable() != this) {
                if (this.table instanceof MultiEventTable) {
                    isFound = true;
                } else {
                    //Iterator var4 = this.joinTables.iterator();
                    //while(var4.hasNext()) {
                    for (Pair pair : this.joinTables) {
                        //Pair var5 = (Pair)var4.next();
                        if (pair.getLeft() == selectColumn.getTable()) {
                            isFound = true;
                            break;
                        }
                    }
                }
            } else {
                isFound = true;
            }

            if (!isFound) {
                throw new RuntimeException(String.format("add select column. [column=%s]", selectColumn.toString()));
            }

            this.addSelect(selectColumn, alias, false);
        }

    }

    public String constructSql() throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(this.constructSelectColumn());
        sql.append("\n\tFROM ");
        sql.append(this.getTableExpression(this.table));

        for(int i = 0; i < this.joinTables.size(); ++i) {
            AbstractTable var3 = (AbstractTable)((Pair)this.joinTables.get(i)).getLeft();
            JoinedTable.JoinType var4 = (JoinedTable.JoinType)((Pair)this.joinTables.get(i)).getRight();
            List var5 = (List)this.joinConditions.get(i);
            if(this.joinConditions.size() == 0) {
                throw new SQLException(String.format("invalid join table, no join condition for table %s", new Object[]{var3.getAlias()}));
            }

            sql.append("\n\t");
            sql.append(var4.name());
            sql.append(" JOIN ");
            if(SQLQueryService.isForceUseShuffleJoin() && OLAPEngineConnectionPool.getEngineType() == OLAPEngineType.IMPALA) {
                sql.append(" /* +SHUFFLE */ ");
            }

            sql.append(this.getTableExpression(var3));
            sql.append("\n\tON ");
            sql.append(this.getJoinConditionExpression(var5));
        }

        String var6 = this.getWhereExpression();
        if(var6.length() > 0) {
            sql.append("\n\tWHERE ");
            sql.append(var6);
        }

        int var7;
        if (null != this.groupByNum) {
            sql.append("\n\t");
            sql.append("GROUP BY ");

            for(var7 = 1; var7 <= this.groupByNum; ++var7) {
                sql.append(var7);
                if(var7 != this.groupByNum) {
                    sql.append(",");
                }
            }
        }

        if (this.orderBys.size() > 0 || null != this.orderByNum) {
            sql.append("\n\t");
            sql.append("ORDER BY ");
            var7 = 0;

            for(Iterator var8 = this.orderBys.iterator(); var8.hasNext(); ++var7) {
                OrderBy var10 = (OrderBy)var8.next();
                if(var7 > 0) {
                    sql.append(",");
                }

                sql.append(var10.constructSql());
            }

            if(this.orderBys.size() > 0 && null != this.orderByNum) {
                sql.append(", ");
            }

            for(int idx = this.startOrderByCol; idx <= this.orderByNum; ++idx) {
                sql.append(idx);
                if (idx != this.orderByNum) {
                    sql.append(",");
                }
            }
        }

        if(null != this.window) {
            sql.append("\n\t");
            sql.append(this.window.constructSql());
        }

        if(null != this.limit) {
            sql.append("\n\t");
            sql.append(this.limit.constructSql());
        }

        return sql.toString();
    }

    private String getWhereExpression() throws Exception {
        StringBuilder var1 = new StringBuilder();
        String var2 = this.constructWhere();
        if(var2.length() > 0) {
            var1.append("(");
            var1.append(var2);
            var1.append(")");
        }

        return var1.toString();
    }

    public AbstractTable getTable() {
        return this.table;
    }

    public String toString() {
        return "JoinedTable{table=" + this.table + ", joinTables=" + this.joinTables + ", joinConditions=" + this.joinConditions + '}';
    }

    public static enum JoinType {
        INNER,
        LEFT,
        RIGHT,
        FULL;

        private JoinType() {
        }
    }
}
