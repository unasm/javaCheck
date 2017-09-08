package com.sensor.queryengine.query;

import com.sensor.common.DbUtils;
import com.sensor.common.QueryLogger;
import com.sensor.common.config.HiveConfigInfo;
import com.sensor.common.config.QueryEngineServerConfig;
import com.sensor.db.OLAPEngineConnectionPool;
import com.sensor.db.OLAPEngineType;
import com.sensor.queryengine.error.RequestCancelException;
import com.sensor.queryengine.expression.AbstractTable;
import com.sensor.queryengine.util.UserUtil;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tianyi on 14/08/2017.
 */
public class SQLQueryService {
    private static final Logger logger = LoggerFactory.getLogger(SQLQueryService.class);

    private static SQLQueryService instance;

    private Queue<String> cancelRequestQueue = new ConcurrentLinkedQueue<>();
    private final LRUMap<String, Boolean> canceledRequestIdSet = new LRUMap<>(1000);
    private final ConcurrentHashMap<String, Queue<SQLQueryService.SQLContext>> sqlContextMap = new ConcurrentHashMap<>();

    private static HiveConfigInfo hiveConfigInfo;
    private static QueryEngineServerConfig queryEngineServerConfig;

    public static void init(QueryEngineServerConfig configQueryEngine, HiveConfigInfo configHive) {
        instance = new SQLQueryService();
        queryEngineServerConfig = configQueryEngine;
        hiveConfigInfo = configHive;

        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException var3) {
            ;
        }

    }

    public List<Map<String, Object>> query(String requestId, AbstractTable table, String queryNames, Integer samplingFactor) throws Exception {
        //requestId, table, "userListIds", samplingFactor);
        return this.query(requestId, table, queryNames, samplingFactor, null);
    }

    public List<Map<String, Object>> query(String requestId, AbstractTable table, String queryNames, Integer samplingFactor, List<String> uidList) throws Exception {
        String sql = table.constructSql();
        return this.query(requestId, sql, queryNames, samplingFactor, uidList);
    }

    public void cancelQuery(String var1) {
        this.cancelRequestQueue.add(var1);
    }

    public List<Map<String, Object>> query(String var1, String var2, String var3) throws Exception {
        return this.query(var1, var2, var3, 0);
    }

    public List<Map<String, Object>> query(String var1, String var2, String var3, Integer var4) throws Exception {
        return this.query(var1, var2, var3, var4, new MapListHandler());
    }

    public List<Map<String, Object>> query(String requestId, String sql, String queryNames, Integer samplingFactor, List<String> uidList) throws Exception {
        //return this.query(requestId, sql, queryNames, samplingFactor, uidList, (ResultSetHandler)(new MapListHandler()));
        return this.query(requestId, sql, queryNames, samplingFactor, uidList, (new MapListHandler()));
        //return (List<Map<String, Object>>)this.query(requestId, sql, queryNames, samplingFactor, uidList, new MapListHandler());
    }

    //public <T> Queue query(String var1, String var2, String var3, Integer var4, ResultSetHandler<T> var5) throws Exception {
    public <T> T query(String var1, String var2, String var3, Integer var4, ResultSetHandler<T> rsl) throws Exception {
        return this.query(var1, var2, var3, var4, null, rsl);
    }

    public static boolean isForceUseShuffleJoin() {
        return queryEngineServerConfig.isForceUseShuffleJoin();
    }

    /**
     *
     *
     *
     *  SELECT  "b"."user_id" as "user_id" , "event_user_list"."first_id" as "event_user_list_0_first_id" , "event_user_list"."second_id" as "event_user_list_0_second_id" , "b"."distinct_id" as "distinct_id" , "event_user_list"."p_entrysex" as "event_user_list_80_p_entrysex" , "event_user_list"."p_isvalidated" as "event_user_list_77_p_isvalidated" , "event_user_list"."p__first_referrer_host" as "event_user_list_60_p__first_referrer_host" , "event_user_list"."p_downloadchannel" as "event_user_list_83_p_downloadchannel" , "event_user_list"."p_face_status" as "event_user_list_62_p_face_status" , "event_user_list"."p_isfaced" as "event_user_list_82_p_isfaced" , "event_user_list"."p_entryregistdate" as "event_user_list_78_p_entryregistdate" , "event_user_list"."p_isidentity" as "event_user_list_85_p_isidentity" , "event_user_list"."p_active_layer" as "event_user_list_88_p_active_layer" , "event_user_list"."p_operator" as "event_user_list_76_p_operator" , "event_user_list"."p_mobilecity" as "event_user_list_89_p_mobilecity" , "event_user_list"."p__first_browser_language" as "event_user_list_59_p__first_browser_language" , "event_user_list"."p__first_visit_time" as "event_user_list_57_p__first_visit_time" , "event_user_list"."p_iscard" as "event_user_list_84_p_iscard" , "event_user_list"."p_entryregisttime" as "event_user_list_90_p_entryregisttime" , "event_user_list"."p__first_referrer" as "event_user_list_58_p__first_referrer" , "event_user_list"."p_mobileprovince" as "event_user_list_81_p_mobileprovince" , "event_user_list"."p_entrylevel" as "event_user_list_86_p_entrylevel" , "event_user_list"."p_birthday_2" as "event_user_list_87_p_birthday_2" , "event_user_list"."p_registerchannel" as "event_user_list_79_p_registerchannel"
     *  FROM
     *  (SELECT  "b"."user_id" as "user_id" , "b"."distinct_id" as "distinct_id"
     *  FROM (
     *  SELECT  "a"."id" as "user_id" , "a"."first_id" as "distinct_id"
     *  FROM profile_p1 a
     *  WHERE (COALESCE("a"."p_seg_fenqun1", 0) = 1)) b) b
     *  LEFT JOIN
     *  profile_p1 event_user_list
     *  ON  "event_user_list"."id" = "b"."user_id"
     *
     * @param requestId     请求的唯一id，幂等
     * @param sql           用于执行的sql
     * @param queryName     请求的名称,
     * @param sampleFactor  采样因子
     * @param userIdList    hue查询中使用
     * @param resultSetHandler   对返回的数据通过反射进行处理，返回list map
     * @return  T 也是 list map
     * @throws Exception
     */

    //public <T> T query(String var1, String var2, String var3, Integer var4, List<String> var5, ResultSetHandler<T> var6) throws Exception {
    public <T> T query(String requestId, String sql, String queryName, Integer sampleFactor, List<String> userIdList, ResultSetHandler<T> resultSetHandler) throws Exception {
        logger.info("execute_sql {}: \n {}, engineType : {}", queryName, sql.replaceAll("\\s+", " "),
                        OLAPEngineConnectionPool.getEngineType());
        boolean var7 = false;
        boolean isUseHive = false;
        if (sampleFactor == null || sampleFactor <= 0) {
            sampleFactor = 0;
        }

        String originSql = sql;
        if (OLAPEngineConnectionPool.getEngineType() == OLAPEngineType.IMPALA) {
            if (sql.contains("USE_SQL_ENGINE_HIVE")) {
                isUseHive = true;
            }

            if(!isUseHive) {
                sql = DistinctUserOptimizer.optimize(sql);
                sql = JoinOptimizer.optimize(sql, originSql, isForceUseShuffleJoin());
                if (originSql.contains("ENABLE_APPROX_DISTINCT")) {
                    var7 = true;
                }

                int threshold = queryEngineServerConfig.getApproxDistinctThreshold();
                if (threshold == 64 || threshold != -1 && sampleFactor > 0 && sampleFactor <= threshold) {
                    var7 = true;
                }

                sql = sql + "/*ENABLE_UNION_OPTIMIZE*/";
                if(originSql.contains("DISTINCT_ID_FILTER")) {
                    Pattern pattern = Pattern.compile("/\\*DISTINCT_ID_FILTER=(.*?)\\*/");
                    Matcher matcher = pattern.matcher(originSql);
                    if(matcher.find()) {
                        String tmpStr = matcher.group(1);
                        String[] distinctIds = tmpStr.split(",");
                        if (userIdList == null) {
                            userIdList = new ArrayList<>();
                        }

                        List<Long> uidList = UserUtil.convertDistinctIdToUserId(Arrays.asList(distinctIds));
                        for (Long userId : uidList) {
                            userIdList.add(userId.toString());
                        }
                    }
                }
            } else {
                sql = sql.replaceAll("/\\*.*?\\*/", "");
            }
        }

        SQLQueryService.SQLContext sqlContext = new SQLQueryService.SQLContext();
        if (StringUtils.isEmpty(requestId)) {
            //必须有一个requestId 表示请求id
            requestId = String.valueOf(RandomUtils.nextLong(0L, 9223372036854775807L));
        }

        if(!isUseHive) {
            //sql  = sql + "id=" + requestId + "";
            sql = sql + "/*id=" + requestId + "*/";
        }

        QueryLogger.beginSql();
        QueryLogger.updateLog("name", queryName);
        QueryLogger.updateLog("sql", sql.replaceAll("\\s+", " "));
        QueryLogger.updateLog("samplingFactor", sampleFactor);
        sqlContext.requestId = requestId;
        sqlContext.sql = sql;
        sqlContext.name = queryName;
        Connection connection;
        if (isUseHive) {
            String hiveUrlList = hiveConfigInfo.getHiveUrlList().get(0);
            if(!hiveUrlList.endsWith("rawdata")) {
                hiveUrlList = hiveUrlList + "/rawdata";
            }

            connection = DriverManager.getConnection(hiveUrlList, hiveConfigInfo.getHiveUser(), "");
        } else {
            connection = OLAPEngineConnectionPool.getConnection();
        }

        if (OLAPEngineConnectionPool.getEngineType() == OLAPEngineType.IMPALA && !isUseHive) {
            try {
                Statement statement = connection.createStatement();
                if (queryName.equals("hue")) {
                    statement.execute("SET REQUEST_POOL=\"hue\"");
                }

                statement.execute("SET USER_ID_LIST=\"" + (userIdList != null?StringUtils.join((Iterable)userIdList, "|") : "") + '\"');
                statement.execute("SET sampling_factor=" + sampleFactor);
                statement.execute("SET APPX_COUNT_DISTINCT=" + (var7 ? 1 : 0));
                statement.close();
            } catch (Exception ex) {
                logger.warn("fail to set sampling factor", ex);
            }
        }

        long executeTime = System.currentTimeMillis();
        logger.info("ready_for_sql {}", sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
        } catch (Exception var26) {
            logger.warn("fail to prepare sql. {} ", var26);
            DbUtils.closeQuietly(connection, null, null);
            QueryLogger.endSql(executeTime);
            throw new SQLException(var26);
        }

        sqlContext.preparedStatement = statement;
        sqlContext.startTime = new Date();
        sqlContext.canceled = false;
        //QueryLogger.updateLog("startTime", DateFormat.DEFAULT_DATETIME_FORMAT.format(var32.startTime));
        sqlContext.maxQueryTimeInSeconds = sqlContext.name.equals("hue") ? queryEngineServerConfig.getMaxHueQueryTimeInSeconds()
                            : queryEngineServerConfig.getMaxQueryTimeInSeconds();
        if (originSql.contains("MAX_QUERY_EXECUTION_TIME=")) {
            Pattern var38 = Pattern.compile("/\\*MAX_QUERY_EXECUTION_TIME=(\\d+)\\*/");
            Matcher var41 = var38.matcher(originSql);
            if(var41.find()) {
                sqlContext.maxQueryTimeInSeconds = Integer.parseInt(var41.group(1));
            }
        }

        Queue<SQLContext> sqlCtx;
        synchronized(this.sqlContextMap) {
            //将请求信息放到一个 hash 列表里面，用于超时检查
            sqlCtx =  this.sqlContextMap.get(requestId);
            if (sqlCtx == null) {
                sqlCtx = new ConcurrentLinkedQueue<>();
                this.sqlContextMap.put(requestId, sqlCtx);
            }

            sqlCtx.add(sqlContext);
        }

        ResultSet resultSet ;
        try {
            resultSet = statement.executeQuery();
        } catch (Exception ex) {
            DbUtils.closeQuietly(connection, statement, null);
            if(!this.isCanceled(sqlContext, ex)) {
                logger.warn("fail to execute sql.", ex);
                //QueryLogger.endSql(var36);
                throw new SQLException(ex);
            }

            QueryLogger.updateLog("canceled", true);
            QueryLogger.endSql(executeTime);
            throw new RequestCancelException("request is canceled:" + requestId);
        }

        T resData;
        try {
            resData = resultSetHandler.handle(resultSet);
        } catch (Exception ex) {
            /*
            if(!this.isCanceled(var32, ex)) {
                logger.warn("fail to convert query result.", var28);
                throw new SQLException(ex);
            }

            */
            QueryLogger.updateLog("canceled", true);
            throw new RequestCancelException("sql is canceled:" + sql);
        } finally {
            DbUtils.closeQuietly(connection, statement, resultSet);
            QueryLogger.endSql(executeTime);
        }

        return resData;
    }


    /**
     * 判断请求是否取消
     *
     * @param context   请求的上下文
     * @param ex        发生异常的 异常信息
     * @return
     */
    private boolean isCanceled(SQLQueryService.SQLContext context, Exception ex) {
        return context.canceled || ex.getMessage() != null && ex.getMessage().contains("Cancelled from Impala\'s");
    }

    public SQLQueryService() {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    SQLQueryService.this.removeTimeoutQuery();
                } catch (Exception var2) {
                    SQLQueryService.logger.error("failed to remove timeout query.", var2);
                }

            }
        }, 0L, 5000L);
    }

    private void cancelQuery(SQLQueryService.SQLContext var1) {
        var1.canceled = true;

        try {
            logger.info("try to cancel query. [sql={}, startTime={}]", var1.sql, var1.startTime);
            var1.preparedStatement.cancel();
        } catch (SQLException var3) {
            logger.warn("fail to cancel query.", var3);
            //DbUtils.closeQuietly(var1.preparedStatement);
        }

    }


    private void removeTimeoutQuery() {
        label91:
        while(true) {
            Iterator var3;
            SQLQueryService.SQLContext var4;
            if(!this.cancelRequestQueue.isEmpty()) {
                String var12 = (String)this.cancelRequestQueue.poll();
                Queue var13 = (Queue)this.sqlContextMap.get(var12);
                if(var13 == null) {
                    continue;
                }

                var3 = var13.iterator();

                while(true) {
                    if(!var3.hasNext()) {
                        continue label91;
                    }

                    var4 = (SQLQueryService.SQLContext)var3.next();
                    this.cancelQuery(var4);
                    this.canceledRequestIdSet.put(var4.requestId, true);
                }
            }

            Iterator var1 = this.sqlContextMap.entrySet().iterator();

            Map.Entry var2;
            while(var1.hasNext()) {
                var2 = (Map.Entry)var1.next();
                var3 = ((Queue)var2.getValue()).iterator();

                while(var3.hasNext()) {
                    var4 = (SQLQueryService.SQLContext)var3.next();
                    PreparedStatement var5 = var4.preparedStatement;

                    try {
                        if(!var5.isClosed()) {
                            Date var6 = var4.startTime;
                            Long var7 = (System.currentTimeMillis() - var6.getTime()) / 1000L;
                            if(var7 >= (long)var4.maxQueryTimeInSeconds) {
                                this.cancelQuery(var4);
                                this.canceledRequestIdSet.put(var4.requestId, true);
                            }
                        }
                    } catch (SQLException var10) {
                        DbUtils.closeQuietly(var5);
                    }
                }
            }

            var1 = this.sqlContextMap.entrySet().iterator();

            while(var1.hasNext()) {
                var2 = (Map.Entry)var1.next();
                boolean var14 = true;
                int var15 = 0;

                for(Iterator var16 = ((Queue)var2.getValue()).iterator(); var16.hasNext(); ++var15) {
                    SQLQueryService.SQLContext var18 = (SQLQueryService.SQLContext)var16.next();

                    try {
                        if(!var18.preparedStatement.isClosed()) {
                            var14 = false;
                            break;
                        }
                    } catch (SQLException var11) {
                        logger.warn("fail to check query status.", var11);
                    }
                }

                if(var14) {
                    ConcurrentHashMap var17 = this.sqlContextMap;
                    synchronized(this.sqlContextMap) {
                        if(var15 == ((Queue)var2.getValue()).size()) {
                            this.sqlContextMap.remove(var2.getKey());
                        }
                    }
                }
            }

            //this.cancelImpalaCanceledQueries();
            if(this.sqlContextMap.size() > 0) {
                logger.info("current running request. [size={}]", this.sqlContextMap.size());
            }

            return;
        }
    }





    public static String escapeColumn(String var0) {
        var0 = var0.replace("\"", "");
        var0 = var0.replace("`", "");
        return OLAPEngineConnectionPool.getEngineType() == OLAPEngineType.VERTICA?'\"' + var0 + '\"':'`' + var0 + '`';
    }

    public static String escapeString(String var0) {
        return OLAPEngineConnectionPool.getEngineType() == OLAPEngineType.VERTICA?var0.replace("\'", "\'\'"):var0.replace("\'", "\\047");
    }

    public static SQLQueryService getInstance() {
        return instance;
    }

    private static class SQLContext {
        public String requestId;
        public String sql;
        public PreparedStatement preparedStatement;
        public Date startTime;
        public int maxQueryTimeInSeconds;
        public String name;
        public volatile Boolean canceled;

        private SQLContext() {
        }
    }
}
