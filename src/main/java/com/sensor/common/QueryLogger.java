package com.sensor.common;

import com.sensor.queryengine.Constants;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.*;

/**
 * Created by tianyi on 21/08/2017.
 */
public class QueryLogger {
    private static ThreadLocal<QueryLogger.QueryLog> logMaps = new ThreadLocal<>();

    public QueryLogger() {
    }

    public static void init() {
        QueryLogger.QueryLog logger = logMaps.get();
        if(logger == null) {
            logger = new QueryLogger.QueryLog();
            logMaps.set(logger);
        }

    }

    public static void startQuery() {
        QueryLogger.QueryLog logger = logMaps.get();
        if(logger != null) {
            if(logger.isRoot) {
                logger.isRoot = false;
                return;
            }

            if(logger.parent == null) {
                QueryLogger.QueryLog var1 = new QueryLogger.QueryLog();
                var1.parent = logger;
                logger.subQueryLogs.add(var1);
                logMaps.set(var1);
            }
        }

    }

    public static void endQuery() {
        QueryLogger.QueryLog logger = logMaps.get();
        if(logger != null && logger.parent != null) {
            logMaps.set(logger.parent);
        }

    }

    public static void clear() {
        QueryLogger.QueryLog logger = logMaps.get();
        if(logger != null) {
            logger.content.clear();
            logger.sqlLogs.clear();
            logger.subQueryLogs.clear();
            logger.isRoot = true;
        }

    }

    public static void updateLog(String var0, Object var1) {
        QueryLogger.QueryLog logger = logMaps.get();
        if (logger != null) {
            logger.content.put(var0, var1);
        }

    }

    /**
     * 开启一个sql 的查询
     */
    public static void beginSql() {
        QueryLogger.QueryLog logger = logMaps.get();
        if (logger != null) {
            QueryLogger.QueryLog queryLog = new QueryLogger.QueryLog();
            queryLog.parent = logger;
            logger.sqlLogs.add(queryLog);
            logMaps.set(queryLog);
        }

    }

    /**
     * 结束一个 请求的初心
     *
     * @param startTime     开启的时间
     */

    public static void endSql(long startTime) {
        QueryLogger.QueryLog logger = logMaps.get();
        if (logger != null) {
            logger.content.put("elapse", System.currentTimeMillis() - startTime);
            logMaps.set(logger.parent);
        }

    }

    private static String toLogString(Map<String, Object> var0) {
        StringBuilder var1 = new StringBuilder();

        for(Iterator var2 = var0.entrySet().iterator(); var2.hasNext(); var1.append("\' ")) {
            Map.Entry var3 = (Map.Entry)var2.next();
            var1.append((String)var3.getKey());
            var1.append("=\'");
            Object var4 = var3.getValue();
            if(var4 == null) {
                var1.append("null");
            } else if(var4 instanceof String) {
                var1.append(var4);
            } else {
                var1.append(Constants.GSON.toJson(var4));
            }
        }

        return var1.toString();
    }

    public static String toLogString() {
        QueryLogger.QueryLog var0 = logMaps.get();
        StringBuilder var1 = new StringBuilder();
        if(var0 == null) {
            return null;
        } else if(!var0.content.isEmpty() && var0.content.containsKey("request")) {
            var1.append(toLogString(var0.content)).append("\n");
            Iterator var2 = var0.sqlLogs.iterator();

            QueryLogger.QueryLog var3;
            while(var2.hasNext()) {
                var3 = (QueryLogger.QueryLog)var2.next();
                var1.append("\t----|").append(toLogString(var3.content)).append("\n");
            }

            var2 = var0.subQueryLogs.iterator();

            while(var2.hasNext()) {
                var3 = (QueryLogger.QueryLog)var2.next();
                var1.append("\t---->").append(toLogString(var3.content)).append("\n");
                Iterator var4 = var3.sqlLogs.iterator();

                while(var4.hasNext()) {
                    QueryLogger.QueryLog var5 = (QueryLogger.QueryLog)var4.next();
                    var1.append("\t\t----|").append(toLogString(var5.content)).append("\n");
                }
            }

            var1.append("\n");
            return var1.toString();
        } else {
            return null;
        }
    }

    private static class QueryLog {
        private Map<String, Object> content;
        private List<QueryLog> sqlLogs;
        private List<QueryLogger.QueryLog> subQueryLogs;
        private QueryLogger.QueryLog parent;
        private boolean isRoot;

        private QueryLog() {
            this.content = new HashMap<>();
            this.sqlLogs = new ArrayList<>();
            this.subQueryLogs = new ArrayList<>();
            this.parent = null;
            this.isRoot = true;
        }

        public String toString() {
            return (new ToStringBuilder(this)).append("content", this.content).append("sqlLogs", this.sqlLogs).
                            append("subQueryLogs", this.subQueryLogs).append("parent", this.parent).toString();
        }
    }
}
