package com.sensor.service;

import com.sensor.common.DateFormat;
import com.sensor.common.QueryLogger;
import com.sensor.common.RequestType;
import com.sensor.queryengine.Constants;
import com.sensor.queryengine.QueryContext;
import com.sensor.queryengine.QueryRequest;
import com.sensor.queryengine.error.ProjectNotReadyException;
import com.sensor.queryengine.executor.QueryExecutor;
import com.sensor.queryengine.executor.impl.UserExecutor;
import com.sensor.queryengine.parser.Parser;
import com.sensor.queryengine.parser.UserParser;
import com.sensor.queryengine.response.QueryResponse;
import com.sensor.queryengine.rewriter.Rewriter;
import com.sensor.queryengine.rewriter.UserRewriter;
import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by tianyi on 10/08/2017.
 */
public class ExecuteService {
    private static final Logger logger = LoggerFactory.getLogger(ExecuteService.class);
    private static final int MAX_CACHE_REFRESH_QUEUE_SIZE = 50;
    private static ExecuteService instance = new ExecuteService();
    private Map<RequestType, Class<? extends QueryExecutor>> queryExecutorMap = new HashMap<RequestType, Class<? extends  QueryExecutor>>();
    private BlockingQueue<QueryRequest> cacheRefreshRequestQueue = new ArrayBlockingQueue<QueryRequest>(50);
    private Map<RequestType, Parser> parserMap = new HashMap<>();
    private Map<RequestType, Rewriter> rewriterMap = new HashMap<>();
    /*
    private Map<TableFunctionType, FunctionConverter> userParserMap = new HashMap();
    private BlockingQueue<QueryRequest> cacheRefreshRequestQueue = new ArrayBlockingQueue(50);
    */
    private LRUMap<String, Long> cacheRefreshHistoryMap = new LRUMap<>(500);

    public static ExecuteService getInstance() {
        return instance;
    }

    private ExecuteService() {
        /*
        this.queryExecutorMap.put(RequestType.SEGMENTATION, SegmentationExecutor.class);
        this.queryExecutorMap.put(RequestType.FUNNEL, FunnelExecutor.class);
        this.queryExecutorMap.put(RequestType.ADDICTION, AddictionExecutor.class);
        this.queryExecutorMap.put(RequestType.RETENTION, RetentionExecutor.class);
        this.queryExecutorMap.put(RequestType.SEGMENTATION_USER, SegmentationUserExecutor.class);
        this.queryExecutorMap.put(RequestType.FUNNEL_USER, FunnelUserExecutor.class);
        this.queryExecutorMap.put(RequestType.RETENTION_USER, RetentionUserExecutor.class);
        this.queryExecutorMap.put(RequestType.ADDICTION_USER, AddictionUserExecutor.class);
        this.queryExecutorMap.put(RequestType.USER_EVENTS, UsersEventsExecutor.class);
        this.queryExecutorMap.put(RequestType.COMMON_SEQUENCE, CommonSequenceExecutor.class);
        this.queryExecutorMap.put(RequestType.SEGMENTATION_SEQUENCE, SegmentationSequenceExecutor.class);
        this.queryExecutorMap.put(RequestType.FUNNEL_SEQUENCE, FunnelSequenceExecutor.class);
        this.queryExecutorMap.put(RequestType.RETENTION_SEQUENCE, RetentionSequenceExecutor.class);
        this.queryExecutorMap.put(RequestType.ADDICTION_SEQUENCE, AddictionSequenceExecutor.class);
        this.queryExecutorMap.put(RequestType.USER_ANALYTICS, UserAnalyticsExecutor.class);
        this.queryExecutorMap.put(RequestType.PATH_ANALYTICS, PathAnalysisExecutor.class);
        this.queryExecutorMap.put(RequestType.PATH_ANALYTICS_USER, PathAnalysisUserExecutor.class);
        */
        this.queryExecutorMap.put(RequestType.USER, UserExecutor.class);
        this.parserMap.put(RequestType.USER, new UserParser());
        this.rewriterMap.put(RequestType.USER, new UserRewriter());

        //this.parserMap.put(RequestType.USER_ANALYTICS, new UserAnalyticsParser());
        //this.rewriterMap.put(RequestType.USER_ANALYTICS, new UserAnalyticsRewriter());

        /*
        this.parserMap.put(RequestType.SEGMENTATION, new SegmentationParser());
        this.parserMap.put(RequestType.FUNNEL, new FunnelParser());
        this.parserMap.put(RequestType.ADDICTION, new AddictionParser());
        this.parserMap.put(RequestType.RETENTION, new RetentionParser());
        this.parserMap.put(RequestType.SEGMENTATION_USER, new SegmentationParser());
        this.parserMap.put(RequestType.FUNNEL_USER, new FunnelParser());
        this.parserMap.put(RequestType.RETENTION_USER, new RetentionParser());
        this.parserMap.put(RequestType.ADDICTION_USER, new AddictionParser());
        this.parserMap.put(RequestType.SEGMENTATION_SEQUENCE, new SegmentationParser());
        this.parserMap.put(RequestType.FUNNEL_SEQUENCE, new FunnelParser());
        this.parserMap.put(RequestType.RETENTION_SEQUENCE, new RetentionParser());
        this.parserMap.put(RequestType.ADDICTION_SEQUENCE, new AddictionParser());
        this.parserMap.put(RequestType.PATH_ANALYTICS, new PathAnalysisParser());
        this.rewriterMap.put(RequestType.SEGMENTATION, new SegmentationRewriter());
        this.rewriterMap.put(RequestType.SEGMENTATION_USER, new SegmentationUserRewriter());
        this.rewriterMap.put(RequestType.FUNNEL, new FunnelRewriter());
        this.rewriterMap.put(RequestType.FUNNEL_USER, new FunnelUserRewriter());
        this.rewriterMap.put(RequestType.RETENTION, new RetentionRewriter());
        this.rewriterMap.put(RequestType.RETENTION_USER, new RetentionUserRewriter());
        this.rewriterMap.put(RequestType.ADDICTION, new AddictionRewriter());
        this.rewriterMap.put(RequestType.ADDICTION_USER, new AddictionUserRewriter());
        this.rewriterMap.put(RequestType.USER_EVENTS, new UsersEventsRewriter());
        this.rewriterMap.put(RequestType.COMMON_SEQUENCE, new SequenceRewriter());
        this.rewriterMap.put(RequestType.SEGMENTATION_SEQUENCE, new SegmentationSequenceRewriter());
        this.rewriterMap.put(RequestType.FUNNEL_SEQUENCE, new FunnelSequenceRewriter());
        this.rewriterMap.put(RequestType.RETENTION_SEQUENCE, new RetentionSequenceRewriter());
        this.rewriterMap.put(RequestType.ADDICTION_SEQUENCE, new AddictionSequenceRewriter());
        this.rewriterMap.put(RequestType.PATH_ANALYTICS, new PathAnalysisRewriter());
        this.userParserMap.put(TableFunctionType.FUNNEL_USER, new FunnelFunctionConverter());
        this.userParserMap.put(TableFunctionType.RETENTION_USER, new RetentionFunctionConverter());
        this.userParserMap.put(TableFunctionType.ADDICTION_USER, new AddictionFunctionConverter());
        */

        (new ExecuteService.AsyncRefreshCacheExecutor()).start();
    }


     private class AsyncRefreshCacheExecutor extends Thread {
        AsyncRefreshCacheExecutor() {
        }

        public void run() {
            while(true) {
                try {
                    QueryRequest request = ExecuteService.this.cacheRefreshRequestQueue.take();
                    QueryContext context = new QueryContext();
                    context.setQueryRequest(request);
                    ExecuteService.this.execute(context, false);
                    ExecuteService.logger.info("start one cache refresh request. [request={}]", Constants.GSON.toJson(request));
                } catch (InterruptedException ex) {
                    ExecuteService.logger.warn("cache refresh thread interrupted.", ex);
                    return;
                } catch (Exception ex) {
                    ExecuteService.logger.warn("fail to execute refresh query.", ex);
                }
            }
        }
    }


    private void addCacheRefreshRequest(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        String jsonRequest = Constants.GSON.toJson(request);
        if(!this.cacheRefreshHistoryMap.containsKey(jsonRequest) ||
                startTime - (this.cacheRefreshHistoryMap.get(jsonRequest)) > 1200000L) {
            boolean var5 = this.cacheRefreshRequestQueue.offer(request);
            if(!var5) {
                logger.warn("fail to add cache refresh request, queue is full.");
            }

            logger.info("add new request to cache refresh queue. [request={}]", jsonRequest);
            this.cacheRefreshHistoryMap.put(jsonRequest, startTime);
        }
    }




    public QueryResponse execute(QueryContext context) throws Exception {
        return this.execute(context, true);
    }

    /**
     *
     * 请求的分发
     *
     * 调用具体的执行函数
     *
     * @param context   执行的上下文
     * @param isCache   是否使用缓存
     * @return
     * @throws Exception
     */
    public QueryResponse execute(QueryContext context, boolean isCache) throws Exception {
        try {
            long startTime = System.currentTimeMillis();
            QueryLogger.startQuery();
            QueryLogger.updateLog("type", context.getQueryRequest().getRequestType());
            QueryLogger.updateLog("request", context.getQueryRequest());
            QueryLogger.updateLog("startTime", DateFormat.DEFAULT_DATETIME_FORMAT.format(new Date()));
            QueryLogger.updateLog("id", context.getQueryRequest().getRequestId());
            QueryLogger.updateLog("project", MetaDataService.getInstance().getCurrentProjectName());

            QueryRequest request = context.getQueryRequest();
            RequestType requestType = request.getRequestType();
            int projectId = MetaDataService.getInstance().getCurrentProjectId();
            if (!MetaDataService.getInstance().isProjectCanQuery(projectId)) {
                throw new ProjectNotReadyException();
            } else {
                if (this.rewriterMap.containsKey(requestType) && !request.isInternal()) {
                    //如果是 rewrite 请求，并且不是内部请求的话，执行rewriter
                    (this.rewriterMap.get(requestType)).rewriteRequest(context);
                    request = context.getQueryRequest();
                }

                if (this.parserMap.containsKey(requestType)) {
                    (this.parserMap.get(requestType)).parser(context);
                }

                String contextSign = MetaDataService.getInstance().calcRequestMetaVersion(context);
                context.setMetaVersion(contextSign);
                //context.setCacheTimestamp(CacheService.getInstance().getLastUpdatePartitionStatusTime());
                QueryLogger.updateLog("metaVersion", contextSign);
                //QueryLogger.updateLog("lastUpdatePartitionStatusTime", CacheService.getInstance().getLastUpdatePartitionStatusTime());
                /*
                if(isCache) {
                    CacheResponse var9 = CacheService.getInstance().getCacheData(context);
                    context.setCacheResponse(var9);
                    if(var9 != null) {
                        long var10 = var9.getOldestTimestamp().longValue();
                        if(System.currentTimeMillis() - var10 >= 604800000L) {
                            ;
                        }
                    }
                }
                */
                while(true) {
                    QueryExecutor executor = (QueryExecutor)((Class)this.queryExecutorMap.get(requestType)).newInstance();
                    QueryResponse response = executor.execute(context);
                    context.setQueryResponse(response);
                    if(this.rewriterMap.containsKey(requestType)) {
                        response = (this.rewriterMap.get(requestType)).rewriteResponse(context);
                    }

                    String reSign = MetaDataService.getInstance().calcRequestMetaVersion(context);
                    if(contextSign.equals(reSign)) {
                        if (context.getNewCacheResponse() != null) {
                            //CacheService.getInstance().updateCacheData(context);
                            QueryLogger.updateLog("updateCache", true);
                        }

                        if (request.getHandleSampling() && response != null && request.getSamplingFactor() != null
                                && request.getSamplingFactor() >= 1 && request.getSamplingFactor() < 64)
                        {
                            executor.restoreSamplingData(request, response);
                        }

                        if(!request.isInternal()) {
                            executor.truncateResponse(context);
                        }

                        long timecost = System.currentTimeMillis() - startTime;
                        logger.info("execute one query request. [request={}, response={}, elapse={}, cacheResponse={}]",
                                Constants.GSON.toJson(request), Constants.GSON.toJson(response), timecost, context.getCacheResponse());
                        QueryLogger.updateLog("elapse", timecost);
                        QueryLogger.updateLog("success", true);
                        //QueryResponse var13 = var19;
                        return response;
                    }

                    logger.warn("try to re-query. [new=\'{}\', old=\'{}\']", contextSign, reSign);
                    contextSign = reSign;
                    context.setMetaVersion(reSign);
                    QueryLogger.updateLog("metaVersion", reSign);
                    QueryLogger.updateLog("retry", true);
                }
            }
        } catch (Exception ex) {
            logger.debug("receive request={}", Constants.GSON.toJson(context.getQueryRequest()));
            QueryLogger.updateLog("success", true);
            throw ex;
        } finally {
            QueryLogger.updateLog("endTime", DateFormat.DEFAULT_DATETIME_FORMAT.format(new Date()));
            QueryLogger.endQuery();
        }
    }

}
