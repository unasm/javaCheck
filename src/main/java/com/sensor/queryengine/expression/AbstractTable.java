package com.sensor.queryengine.expression;

import com.sensor.db.OLAPEngineConnectionPool;
import com.sensor.db.OLAPEngineType;
import com.sensor.queryengine.executor.impl.OrderBy;
import com.sensor.queryengine.expression.filter.AbstractFilter;
import com.sensor.queryengine.expression.filter.SamplingFilter;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;

/**
 * table 的 基类
 * Created by tianyi on 14/08/2017.
 */
public abstract  class AbstractTable implements SqlGenerator, Serializable, Cloneable{
    //a, b, c,d 用于在sql 中 替代表名称
    protected String alias;
    protected Window window;
    protected List<OrderBy> orderBys = new ArrayList<>();
    // 这个表里面，所有的 列 的集合，未必是筛选的，而是全部的, 包括用户分群的字段, key 是property 的name
    protected Map<String, AtomColumn> columnMap = new HashMap<>();
    // 用于筛选的column, select 的字段
    protected List<AbstractColumn> columns = new ArrayList<>();
    // 用于构建where 条件
    protected List<AbstractFilter> andFilters = new ArrayList<>();
    protected List<AbstractFilter> orFilters = new ArrayList<>();

    protected Limit limit;
    protected Integer groupByNum;
    protected Integer orderByNum;
    protected Integer startOrderByCol = 1;
    protected String tableAlias = " * ";
    protected Integer samplingFactor = null;
    protected Boolean orderByAsc = true;
    protected Set<Integer> eventBuckets = new HashSet<>();
    protected boolean useEventBucketsFilter = true;
    protected AbstractColumn eventBucketColumn = null;
    public AbstractTable(String alias) {
        this.alias = alias;
    }
    public List<AbstractColumn> getColumns() {
        return this.columns;
    }
    public String getAlias() {
        return this.alias;
    }

    public void addOrFilter(AbstractFilter filter) {
        if(null != filter) {
            this.orFilters.add(filter);
        }

    }

    public void addOrFilters(List<AbstractFilter> filter) {
        if (null != filter) {
            this.orFilters.addAll(filter);
        }

    }

    public AtomColumn getColumn(String filedName) throws SQLException {
        AtomColumn column = this.columnMap.get(filedName);
        if (null != column) {
            return column;
        } else if(filedName.contains("$")) {
            return this.getColumn(filedName.replace("$", ""));
        } else {
            //StringBuilder var3 = new StringBuilder();
            //Iterator var4 = this.columnMap.keySet().iterator();

            //while(var4.hasNext()) {
            //    String var5 = (String)var4.next();
            //    var3.append(var5);
            //    var3.append(",");
            //}
            String keyStr = StringUtils.join(this.columnMap.keySet(), ",");
            throw new SQLException(String.format("column %s not exist in table %s. [available columns={%s}]",
                                filedName, this.alias, keyStr));
        }
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    //public AtomColumn(String alias, AbstractTable table, String columnName, String propertyName, Integer propertyId,
                      //Integer dataType, Boolean valueMapping, String defaultValue) {
    protected void createColumn(String columnName, String propertyName, Integer propertyId, Integer dataType,
                                Boolean valueMapping, String defaultValue)
    {
        AtomColumn atomColumn = new AtomColumn(this.alias + "_" + propertyId + "_" + columnName, this, columnName,
                        propertyName, propertyId, dataType, valueMapping, defaultValue);
        this.columnMap.put(propertyName, atomColumn);
    }

    /**
     *  用于创建 列对象
     * @param columnName
     * @param propertyName
     * @param propertyId
     * @param dataType
     */
    protected void createColumn(String columnName, String propertyName, Integer propertyId, Integer dataType) {
        this.createColumn(columnName, propertyName, propertyId, dataType, false, null);
    }

    public void addSamplingFilter(AbstractColumn idColumn) {
        if (this.samplingFactor != null && this.samplingFactor >= 1
                    && this.samplingFactor < 64) {
            this.addAndFilter(new SamplingFilter(idColumn, this.samplingFactor));
        }
    }

    /**
     * 将select 的字段 拼接成sql 的string
     *
     * @return string
     * @throws Exception
     */
    public String constructSelectColumn() throws Exception {
        if (null != this.columns && this.columns.size() != 0) {
            StringBuilder sql = new StringBuilder();
            int notFrist = 0;

            for(Iterator iterator = this.columns.iterator(); iterator.hasNext(); ++notFrist) {
                AbstractColumn column = (AbstractColumn)iterator.next();
                if(notFrist > 0) {
                    sql.append(",");
                }

                sql.append(column.constructSql());
            }
            return sql.toString();
        } else {
            return this.tableAlias;
        }
    }

    public Map<String, AtomColumn> getColumnMap() {
        return this.columnMap;
    }

    public void setOrderByNum(Integer orderByNum) {
        this.orderByNum = orderByNum;
    }


    public void setStartOrderByCol(Integer startOrder) {
        this.startOrderByCol = startOrder;
    }

    public String constructWhere() throws Exception {
        StringBuilder where = new StringBuilder();
        boolean notFirst = false;
        Iterator iterator = this.andFilters.iterator();

        AbstractFilter filter;
        String sonSql;
        while (iterator.hasNext()) {
            filter = (AbstractFilter)iterator.next();
            sonSql = filter.constructSql();
            if (!StringUtils.isBlank(sonSql)) {
                if (notFirst) {
                    where.append(" AND ");
                }

                where.append("(");
                where.append(sonSql);
                where.append(")");
                notFirst = true;
            }
        }

        if (this.orFilters.size() > 0) {
            if (this.andFilters.size() > 0) {
                where.append(" AND ");
                where.append("(");
            }

            notFirst = false;
            iterator = this.orFilters.iterator();

            while (iterator.hasNext()) {
                filter = (AbstractFilter)iterator.next();
                sonSql = filter.constructSql();
                if(!StringUtils.isBlank(sonSql)) {
                    if (notFirst) {
                        where.append(" OR ");
                    }

                    where.append("(");
                    where.append(sonSql);
                    where.append(")");
                    notFirst = true;
                }
            }

            if(this.andFilters.size() > 0) {
                where.append(")");
            }
        }

        if (OLAPEngineConnectionPool.getEngineType() == OLAPEngineType.IMPALA &&
                this.eventBuckets.size() > 0 && this.useEventBucketsFilter) {
            if (notFirst) {
                where.append(" AND ");
            }

            where.append(this.eventBucketColumn.getId());
            where.append(" IN (");
            where.append(StringUtils.join(this.eventBuckets, ','));
            where.append(")");
        }

        return where.toString();
    }

    public AbstractColumn getEventBucketColumn() {
        return this.eventBucketColumn;
    }

    public void setEventBucketColumn(AbstractColumn eventBucketColumn) {
        this.eventBucketColumn = eventBucketColumn;
    }

    public void addSelect(AbstractColumn select) {
        this.addSelect(select, null);
    }

    public void addSelect(AbstractColumn selectColumn, String alias) {
        this.addSelect(selectColumn, alias, true);
    }

    public void addSelect(String alias) throws SQLException {
        this.addSelect(this.getColumn(alias), alias);
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    public Window getWindow() {
        return this.window;
    }


    /**
     * 添加想要select 字段
     * @param selectColumn
     * @param alias
     * @param var3
     */
    public void addSelect(AbstractColumn selectColumn, String alias, boolean var3) {
        if (null != selectColumn) {
            if (var3 && selectColumn.getTable() != this) {
                throw new RuntimeException(String.format("add select column. [column=%s, table=%s]",
                                selectColumn.getTable().getAlias(), this.getAlias()));
            }

            AbstractColumn column = selectColumn;
            if (selectColumn.getTable() != this) {
                column = SerializationUtils.clone(selectColumn);
            }

            if (alias != null) {
                column.setAlias(alias);
            }

            this.columns.add(column);
        }
    }


    public void addAndFilter(AbstractFilter filter) {
        if (null != filter) {
            this.andFilters.add(filter);
        }
    }
}
