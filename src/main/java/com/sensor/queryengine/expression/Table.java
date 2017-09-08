package com.sensor.queryengine.expression;

import com.sensor.common.DataType;
import com.sensor.db.bean.PropertyBean;
import com.sensor.queryengine.executor.impl.OrderBy;
import com.sensor.service.MetaDataService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by tianyi on 11/08/2017.
 */
public class Table extends  AbstractTable {
    private static final Logger logger = LoggerFactory.getLogger(Table.class);

    // 子表， 用于 子查询的时候 使用 ，select * from
    protected AbstractTable preTable = null;
    protected String name = null;
    protected Integer eventId = null;


    public Table(String alias, AbstractTable table) {
        super(alias);
        this.preTable = table;
        if(table instanceof Table && ((Table)table).isAtomTable()) {
            this.eventId = ((Table)table).getEventId();
            this.name = ((Table)table).getName();
        }

        int cnt = 1;

        for (Iterator iterator = table.getColumns().iterator(); iterator.hasNext(); ++cnt) {
            AbstractColumn column = (AbstractColumn)iterator.next();
            AtomColumn atomColumn = new AtomColumn(String.format("c%d", cnt), this, column);
            this.columnMap.put(atomColumn.getName(), atomColumn);
        }
    }


    /**
     *
     * 汇总各个 constructSql 拼接成完整的sql
     *
     * @return string  sql
     * @throws Exception
     */
    public String constructSql() throws Exception {
        logger.info("before_constructSql_select_column name ");
        String selectColumnStr = this.constructSelectColumn();
        logger.info("constructSql_select_column name : {}", this.name);
        String whereStr = this.constructWhere();
        logger.info("constructSql_where_column name : {}", this.name);
        if (selectColumnStr.equals(this.tableAlias) && this.groupByNum == null && this.window == null &&
                        this.limit == null && StringUtils.isEmpty(whereStr))
        {
            logger.info("constructTableName : {}", this.constructTableName());
            return this.constructTableName();
        } else {
            logger.info("construct_ready_for_create_sql");
            StringBuilder resSql = new StringBuilder();
            resSql.append("SELECT ");
            resSql.append(selectColumnStr);
            resSql.append("\n\tFROM ");
            if (this.preTable == null) {
                resSql.append(this.constructTableName());
            } else {
                // 如果是需要子查询的话，则select * from （son） 的方式来 确定子表
                resSql.append("(");
                resSql.append("\n\t");
                logger.info("ready_for_next_sql {}" , this.preTable.getClass());
                String preTable = this.preTable.constructSql();
                resSql.append(preTable.replaceAll("\n", "\n\t"));
                resSql.append(")");
            }

            resSql.append(" ");
            //表的 代称 ，用于屏蔽 子查询和从数据表中的差异
            resSql.append(this.alias);
            if (whereStr.length() > 0) {
                resSql.append("\n\tWHERE ");
                resSql.append(this.constructWhere());
            }

            int idx;
            if (null != this.groupByNum) {
                // 如果groupBy 不为空的话
                resSql.append("\n\t");
                resSql.append("GROUP BY ");

                for (idx = 1; idx <= this.groupByNum; ++idx) {
                    resSql.append(idx);
                    if (idx != this.groupByNum) {
                        resSql.append(",");
                    }
                }
            }

            if (this.orderBys.size() > 0 || null != this.orderByNum) {
                //order by
                resSql.append("\n\t");
                resSql.append("ORDER BY ");
                idx = 0;

                for(Iterator iterator = this.orderBys.iterator(); iterator.hasNext(); ++idx) {
                    OrderBy order = (OrderBy)iterator.next();
                    if (idx > 0) {
                        resSql.append(",");
                    }

                    resSql.append(order.constructSql());
                }

                if (this.orderBys.size() > 0 && null != this.orderByNum) {
                    resSql.append(", ");
                }

                if (this.orderByNum != null) {
                    for(int i = this.startOrderByCol; i <= this.orderByNum; ++i) {
                        resSql.append(i);
                        if (i != this.orderByNum) {
                            resSql.append(",");
                        }
                    }
                }

                if (!this.orderByAsc) {
                    resSql.append(" DESC");
                }
            }

            if (null != this.window) {
                resSql.append("\n\t");
                logger.info("window_check_sql : {}", this.window.constructSql());
                resSql.append(this.window.constructSql());
            }

            if (null != this.limit) {
                resSql.append("\n\t");
                resSql.append(this.limit.constructSql());
            }

            logger.info("step_into_constructSql selectColumnStr ：{},  whereStr : {}, tablealias : {}, groupByNum : {}, columnSize : {}, res : {}",
                    selectColumnStr, whereStr, this.tableAlias, this.groupByNum, this.columns.size(), resSql.toString());
            return resSql.toString();
        }
    }


    public Table(String alias, String tableName, Table.TableType tableType, Integer samplingFactor) throws SQLException {
        super(alias);
        this.name = tableName;
        this.eventId = 0;
        this.samplingFactor = samplingFactor;
        AtomColumn idColumn;
        if(tableType == Table.TableType.PROFILE) {
            //用户表
            this.createColumn("$id", "$id", 0, DataType.NUMBER.getIndex());
            this.createColumn("$first_id", "$first_id", 0, DataType.NUMBER.getIndex());
            this.createColumn("$second_id", "$second_id", 0, DataType.NUMBER.getIndex());
            this.createColumns(MetaDataService.currentProject().getAllUserProfiles());
            logger.info("create_people_table, tableName : {}, profile : {}",
                    this.toString(), MetaDataService.currentProject().getAllUserProfiles().size());
            idColumn = this.getColumn("$id");
        } else {
            //用户分群表，或者事件表
            this.createColumn("$id", "$id", 0, DataType.NUMBER.getIndex());
            this.createColumn("$distinct_id", "$distinct_id", 0, DataType.NUMBER.getIndex());
            this.createColumn("value", "value", 0, DataType.BOOL.getIndex());
            idColumn = this.getColumn("$id");
        }
        this.addSamplingFilter(idColumn);
    }


    public void createColumns(Collection<PropertyBean> properties) {
        if(null != properties) {
            for (PropertyBean bean : properties) {
                logger.info("create_column_properties {}", bean.toString());
                this.createColumn(bean.getDbColumnName(), bean.getName(), bean.getId(), bean.getDataType(),
                        bean.isValueMapping(), bean.getDefaultValue());
            }
        }

    }


    public String toString() {
        return "Table{preTable=" + this.preTable + ", name=\'" + this.name + '\'' + ", eventId=" + this.eventId + "}, " + super.toString();
    }

    public String getName() {
        return this.name;
    }

    public Integer getEventId() {
        return this.eventId;
    }

    public boolean isAtomTable() {
        return this.preTable == null;
    }

    public String constructTableName() throws Exception {
        return this.name;
    }

    public static enum TableType {
        EVENT,
        PROFILE,
        SEGMENTER;

        private TableType() {
        }
    }
}
