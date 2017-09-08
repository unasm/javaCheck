package com.sensor.common.utils;

import com.sensor.db.CustomBeanProcessor;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by tianyi on 23/08/2017.
 */
public class SqlUtil {
    private static final Logger logger = LoggerFactory.getLogger(SqlUtil.class);

    private static final BasicRowProcessor rowProcessor = new BasicRowProcessor(new CustomBeanProcessor());

    public SqlUtil() {
    }

    public static <T> ResultSetHandler<T> createResultHandler(Class<T> template) {
        return new BeanHandler<>(template, rowProcessor);
    }

    /**
     * 拼接成delete 语句
     *
     * @param tableName     要delete的表名称
     * @param args          delete的参数
     * @return
     */
    public static String getDeleteByColumnQuery(String tableName, String... args) {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(tableName).append(" WHERE ");

        for(int i = 0; i < args.length; ++i) {
            if(i > 0) {
                sql.append(" AND ");
            }

            sql.append(args[i]).append(" = ?");
        }

        return sql.toString();
    }

    /**
     * 拼接成insert 的语句
     *
     * @param tableName         表名称
     * @param fields            要插入的表的字段
     * @return
     */

    public static String getInsertQuery(String tableName, String... fields) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO %s (");

        int i;
        for(i = 0; i < fields.length; ++i) {
            if(i > 0) {
                sql.append(", ");
            }

            sql.append(fields[i]);
        }

        sql.append(") VALUES (");

        for(i = 0; i < fields.length; ++i) {
            if (i > 0) {
                sql.append(", ?");
            } else {
                sql.append("?");
            }
        }

        sql.append(")");
        return String.format(sql.toString(), tableName);
    }


    /**
     * 创建表的语句
     *
     * @param tableName     表名称
     * @param keys          数据库的字段
     * @param  values       值
     * @return
     */
    public static String createTableQuery(String tableName, String[] keys, String[] values) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append(tableName);
        sql.append(" (");

        for(int i = 0; i < keys.length; ++i) {
            if (i != 0) {
                sql.append(',');
            }

            sql.append(keys[i]);
            sql.append(' ');
            sql.append(values[i]);
            sql.append(' ');
        }

        sql.append(")  ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;");
        return sql.toString();
    }


    public static String truncateTableQuery(String tableName) {
        return String.format("truncate table %s", tableName);
    }

    public static String dropTableQuery(String tableName) {
        return String.format("drop table %s", tableName);
    }


    public static String getSelectAllQuery(String tableName) {
        return String.format("SELECT * FROM %s", tableName);
    }

    public static String getSelectAllQuery(String tableName, int projectId) {
        return String.format("SELECT * FROM %s WHERE project_id = %d", tableName, projectId);
    }

    public static <T> ResultSetHandler<List<T>> createListResultHandler(Class<T> template) {
        return new BeanListHandler<>(template, rowProcessor);
    }


    /**
     * 拼接成update语句
     *
     * @param tableName     表名称
     * @param values        值
     * @param fields        update 的字段
     * @return  update的语句
     */
    public static String getUpdateByIdQuery(String tableName, String values, String... fields) {
        StringBuilder updateSql = new StringBuilder();
        updateSql.append("UPDATE %s SET ");

        for(int item = 0; item < fields.length; ++item) {
            if(item > 0) {
                updateSql.append(", ");
            }
            updateSql.append(fields[item]).append(" = ?");
        }

        updateSql.append(" WHERE %s = ?");
        return String.format(updateSql.toString(), tableName, values);
        //return String.format(updateSql.toString(), new Object[]{tableName, field});
    }

    /**
     *
     * 用于拼接成 sql，参数使用 ？ 替代，后续使用正确参数预编译
     *
     * @param tableName     数据库表名称
     * @param condition     条件之间的关联关系, OR, AND
     * @param fields        WHERE的限制条件
     * @return
     */
    public static String getSelectQuery(String tableName, String condition, String... fields) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");

        for(int i = 0; i < fields.length; ++i) {
            if(i > 0) {
                sql.append(" ");
                sql.append(condition);
                sql.append(" ");
            }

            sql.append(fields[i]).append(" = ?");
        }

        return sql.toString();
    }
}
