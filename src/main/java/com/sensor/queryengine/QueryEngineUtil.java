package com.sensor.queryengine;

import com.sensor.common.QueryLogger;
import com.sensor.db.bean.DashboardBean;
import com.sensor.queryengine.error.ParameterException;
import com.sensor.queryengine.error.QueryEngineParameterException;
import com.sensor.queryengine.response.QueryResponse;
import com.sensor.service.ExecuteService;
import com.sensor.web.db.WebMetaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;


/**
 * 处理引擎的入口
 * Created by tianyi on 01/08/2017.
 */
public class QueryEngineUtil {
    private static final Logger logger = LoggerFactory.getLogger(QueryEngineUtil.class);

    public  static QueryEngineCounter queryEngineCounter = new QueryEngineCounter();

    public static void queryUserList(QueryRequest request, Writer writer, boolean isCache) {
        QueryContext queryContext = new QueryContext();
        queryContext.setWriter(writer);
        queryContext.setQueryRequest(request);
        QueryResponse response = executeQuery(queryContext, isCache);

        logger.info("after_queryUserList : {}", response);
        if (response != null) {
            Constants.GSON.toJson(response, writer);
        }
    }

    public static void queryUserList(QueryRequest request, Writer writer) throws IOException {
        QueryContext queryContext = new QueryContext();
        queryContext.setQueryRequest(request);
        queryContext.setWriter(writer);
        QueryResponse response = executeQuery(queryContext);
        if(response != null) {
            Constants.GSON.toJson(response, writer);
        }
    }

    private  static QueryResponse executeQuery(QueryContext queryContext) {
        //private  static QueryResponse executeQuery(QueryContext queryContext) {
        return executeQuery(queryContext, true);
    }

    private static  QueryResponse executeQuery(QueryContext queryContext, boolean isCache) {
        //private static  QueryResponse executeQuery(QueryContext queryContext, boolean value) {
        String requestName = queryContext.getQueryRequest().getRequestType().name();
        long startTime = System.currentTimeMillis();
        QueryResponse response ;
        try {
            queryEngineCounter.newRequest(requestName);
            Integer dashboardId = queryContext.getQueryRequest().getDashboardId();
            if (dashboardId != null) {
                DashboardBean dashBean = WebMetaDataService.getInstance().getDashboard(dashboardId) ;
                if (dashBean != null) {
                    QueryLogger.updateLog("dashboard_name", dashBean.getName());
                    QueryLogger.updateLog("dashboard_config", dashBean.getConfig());
                }
            }
            response = ExecuteService.getInstance().execute(queryContext, isCache);
        } catch (ParameterException ex) {
            logger.warn("query parameter exception", ex);
            throw new QueryEngineParameterException(ex.getMessage());
        } catch (Exception ex) {
            logger.warn("exception {}", ex);
            throw new RuntimeException(ex);
        } finally {
            queryEngineCounter.completeRequest(requestName, startTime);
        }

        return response;
    }
}
