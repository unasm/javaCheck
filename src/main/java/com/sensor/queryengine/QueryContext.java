package com.sensor.queryengine;

import com.sensor.db.bean.UserBean.EventPermissionData;
import com.sensor.queryengine.response.QueryResponse;
import org.apache.commons.lang3.tuple.Pair;
import com.sensor.queryengine.parser.result.ParseResult;

import java.io.Writer;
import java.net.CacheResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyi on 01/08/2017.
 */
public class QueryContext {
    private QueryRequest queryRequest;
    private QueryResponse queryResponse;
    private CacheResponse cacheResponse;
    private String metaVersion;
    private CacheResponse newCacheResponse;
    private Long cacheTimestamp;
    private ParseResult parseResult = new ParseResult();
    private List<Pair<String, byte[]>> fragmentCacheDataList;
    private boolean entireCache;
    private Writer writer;
    private EventPermissionData eventPermissionData;
    private List<String> sqlList = new ArrayList<>();
    private boolean dryRun = false;

    public QueryContext() {
    }

    public QueryRequest getQueryRequest() {
        return this.queryRequest;
    }

    public void setQueryRequest(QueryRequest request) {
        this.queryRequest = request;
    }

    public QueryResponse getQueryResponse() {
        return this.queryResponse;
    }

    public void setQueryResponse(QueryResponse response) {
        this.queryResponse = response;
    }

    public CacheResponse getCacheResponse() {
        return this.cacheResponse;
    }

    public void setCacheResponse(CacheResponse cacheResponse) {
        this.cacheResponse = cacheResponse;
    }

    public CacheResponse getNewCacheResponse() {
        return this.newCacheResponse;
    }

    public void setNewCacheResponse(CacheResponse cacheResponse) {
        this.newCacheResponse = cacheResponse;
    }

    public String getMetaVersion() {
        return this.metaVersion;
    }

    public void setMetaVersion(String metaVersion) {
        this.metaVersion = metaVersion;
    }

    public Long getCacheTimestamp() {
        return this.cacheTimestamp;
    }

    public void setCacheTimestamp(Long cacheTimestamp) {
        this.cacheTimestamp = cacheTimestamp;
    }

    public ParseResult getParseResult() {
        return this.parseResult;
    }

    public void setParseResult(ParseResult parser) {
        this.parseResult = parser;
    }

    public List<Pair<String, byte[]>> getFragmentCacheDataList() {
        return this.fragmentCacheDataList;
    }

    public void setFragmentCacheDataList(List<Pair<String, byte[]>> cacheDataList) {
        this.fragmentCacheDataList = cacheDataList;
    }

    public boolean isEntireCache() {
        return this.entireCache;
    }

    public void setEntireCache(boolean entryCache) {
        this.entireCache = entryCache;
    }

    public EventPermissionData getEventPermissionData() {
        return this.eventPermissionData;
    }

    public void setEventPermissionData(EventPermissionData permissionData) {
        this.eventPermissionData = permissionData;
    }

    public Writer getWriter() {
        return this.writer;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public boolean isDryRun() {
        return this.dryRun;
    }

    public void setDryRun(boolean isDryRun) {
        this.dryRun = isDryRun;
    }

    public List<String> getSqlList() {
        return this.sqlList;
    }

    public void addSql(String sql) {
        this.sqlList.add(sql);
    }
}
