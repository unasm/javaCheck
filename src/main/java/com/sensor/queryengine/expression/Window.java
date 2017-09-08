package com.sensor.queryengine.expression;

import com.sensor.db.OLAPEngineConnectionPool;
import com.sensor.db.OLAPEngineType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用于创建sql
 * Created by tianyi on 02/09/2017.
 */
public class Window implements SqlGenerator {

    //降序
    private boolean desc = false;
    private boolean desc2 = false;
    private String alias;
    private List<AbstractColumn> partition = new ArrayList<>();
    private List<AbstractColumn> order = new ArrayList<>();
    private List<AbstractColumn> order2 = new ArrayList<>();
    private Window.WindowType windowType;

    public Window(String alias, boolean desc) {
        this.windowType = Window.WindowType.DEFAULT;
        this.alias = alias;
        this.desc = desc;
    }

    public Window(String alias, boolean desc, boolean desc2) {
        this.windowType = Window.WindowType.DEFAULT;
        this.alias = alias;
        //降序
        this.desc = desc;
        this.desc2 = desc2;
    }

    public String constructSql() throws Exception {
        if (StringUtils.isEmpty(this.alias)) {
            throw new SQLException("invalid window no alias");
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("(");
            boolean notFirst = false;
            Iterator iterator;
            AbstractColumn column;
            if (CollectionUtils.isNotEmpty(this.partition)) {
                sql.append("PARTITION BY ");
                for(iterator = this.partition.iterator(); iterator.hasNext(); notFirst = true) {
                    column = (AbstractColumn)iterator.next();
                    if (notFirst) {
                        sql.append(',');
                    }
                    sql.append(column.getId());
                }
            }

            if(null != this.order && this.order.size() > 0) {
                sql.append(" ORDER BY ");
                notFirst = false;

                for (iterator = this.order.iterator(); iterator.hasNext(); notFirst = true) {
                    column = (AbstractColumn)iterator.next();
                    if (notFirst) {
                        sql.append(',');
                    }

                    sql.append(column.getId());
                }

                if(this.desc) {
                    sql.append(" DESC");
                } else {
                    sql.append(" ASC");
                }

                if (null != this.order2 && this.order2.size() > 0) {
                    for (iterator = this.order2.iterator(); iterator.hasNext(); notFirst = true) {
                        column = (AbstractColumn)iterator.next();
                        if (notFirst) {
                            sql.append(',');
                        }

                        sql.append(column.getId());
                    }

                    if (this.desc2) {
                        sql.append(" DESC");
                    } else {
                        sql.append(" ASC");
                    }
                }

                if(OLAPEngineConnectionPool.getEngineType() == OLAPEngineType.IMPALA) {
                    if(this.windowType == Window.WindowType.ROWS_ALL) {
                        sql.append(" ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING");
                    } else if(this.windowType == Window.WindowType.ROWS_PRECEDING_AND_CURRENT) {
                        sql.append(" ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW");
                    } else if(this.windowType == Window.WindowType.ROWS_PRECEDING_AND_1_FOLLOWING) {
                        sql.append(" ROWS BETWEEN UNBOUNDED PRECEDING AND 1 following");
                    }
                }
            }

            sql.append(")");
            return sql.toString();
        }
    }

    public void addPartition(AbstractColumn column) {
        this.partition.add(column);
    }

    public void addOrder(AbstractColumn column) {
        this.order.add(column);
    }

    public void addOrder2(AbstractColumn column) {
        this.order2.add(column);
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<AbstractColumn> getPartition() {
        return this.partition;
    }

    public void setPartition(List<AbstractColumn> partition) {
        this.partition = partition;
    }

    public List<AbstractColumn> getOrder() {
        return this.order;
    }

    public void setOrder(List<AbstractColumn> order) {
        this.order = order;
    }

    public boolean isDesc() {
        return this.desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    public Window.WindowType getWindowType() {
        return this.windowType;
    }

    public void setWindowType(Window.WindowType type) {
        this.windowType = type;
    }

    public static enum WindowType {
        DEFAULT,
        ROWS_ALL,
        ROWS_PRECEDING_AND_CURRENT,
        ROWS_PRECEDING_AND_1_FOLLOWING;

        private WindowType() {
        }
    }
}
