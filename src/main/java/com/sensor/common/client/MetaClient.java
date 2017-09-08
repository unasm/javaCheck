package com.sensor.common.client;

import com.sensor.common.metadata.TableInfo;
import com.sensor.db.MetaConnectionPool;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by tianyi on 23/08/2017.
 */
public class MetaClient {

    private static final Logger logger = LoggerFactory.getLogger(MetaClient.class);

    public MetaClient(boolean var1) throws Exception {
        if(!var1) {
            MetaConnectionPool.init();
        }

    }

    public MetaClient() throws Exception {
        MetaConnectionPool.init();
    }
    protected MetaClient(String var1) {
    }

    public static MetaClient createNoThrow() {
        return new MetaClient("");
    }


    public List<TableInfo> getAllTableInfoByUpdateTime(int type, Date lastUpdateTime, Set<Integer> projectIds) throws IOException, SQLException {
        List var4 = this.query("SELECT id, db, table_name, hdfs_path, project_id FROM table_define WHERE type = ? AND update_time > ? " +
                        "AND project_id IN (" + StringUtils.join(projectIds, ',') + ")", type, lastUpdateTime);
        if(var4 == null) {
            logger.error("Get id from table_define for type = {} fail", type);
            return null;
        } else {
            ArrayList<TableInfo> res = new ArrayList<>();
            Iterator<TableInfo> iterator = var4.iterator();
            for (;iterator.hasNext();) {
                Map row = (Map)iterator.next();
                int id = ((Integer)row.get("id"));
                String table_name = (String)row.get("table_name");
                String db = (String)row.get("db");
                String hdfs_path = (String)row.get("hdfs_path");
                Integer project_id= (Integer)row.get("project_id");
                logger.debug("getAllTableInfoByUpdateTime_info {}, {}, {}, {}, {}", id, table_name, db, hdfs_path,  project_id);
                res.add(new TableInfo(id, table_name, db, hdfs_path,  project_id));
            }
            return res;
        }
    }

    protected List<Map<String, Object>> query(String var1) throws SQLException {
        return (List<Map<String, Object>>)this.query(var1, (ResultSetHandler)(new MapListHandler()));
    }

    protected List<Map<String, Object>> query(String var1, Object... var2) throws SQLException {
        return this.query(var1, new MapListHandler(), var2);
    }

    protected Map<String, Object> queryFirstRecord(String var1, Object... var2) throws SQLException {
        return this.query(var1, new MapHandler(), var2);
    }

    protected <T> T query(String var1, ResultSetHandler<T> var2) throws SQLException {
        return this.query(var1, var2, (Object[])null);
    }

    protected Long insert(String tableName, Object... fields) throws SQLException {
        return (Long)this.insert(tableName, new ScalarHandler(), fields);
    }

    private  <T> T insert(String tableName, ResultSetHandler<T> fields, Object... value) throws SQLException {
        int retry = 0;

        while(true) {
            try {
                QueryRunner runner = new QueryRunner(MetaConnectionPool.getDataSource());
                return runner.insert(tableName, fields, value);
            } catch (SQLException ex) {
                if(retry >= 3) {
                    logger.error("retry touch max, fail", ex);
                    throw ex;
                }

                logger.warn("run sql:" + tableName + " fail,retry " + retry + " time", ex);
                ++retry;
            }
        }
    }


    protected <T> T query(String sql, ResultSetHandler<T> fields, Object... values) throws SQLException {
        int times = 0;
        while(true) {
            try {
                QueryRunner runner = new QueryRunner(MetaConnectionPool.getDataSource());
                logger.debug("meta_client_query {} , {}, {}", sql, fields, values);
                return runner.query(sql, fields, values);
            } catch (SQLException ex) {
                if(times >= 3) {
                    logger.error("retry touch max, fail", ex);
                    throw ex;
                }

                logger.warn("run sql:" + sql + " fail,retry " + times + " time", ex);
                ++times;
            }
        }
    }

    protected int update(String sql) throws SQLException {
        return this.update(sql, (Object[])null);
    }

    protected int update(String var1, Object... var2) throws SQLException {
        int var3 = 0;

        while(true) {
            try {
                QueryRunner var4 = new QueryRunner(MetaConnectionPool.getDataSource());
                return var4.update(var1, var2);
            } catch (SQLException var5) {
                if(var3 >= 3) {
                    logger.error("retry touch max, fail", var5);
                    throw var5;
                }

                logger.warn("run sql:" + var1 + " fail,retry " + var3 + " time", var5);
                ++var3;
            }
        }
    }
}
