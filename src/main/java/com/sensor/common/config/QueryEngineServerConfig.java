package com.sensor.common.config;

import java.util.List;

/**
 * Created by tianyi on 17/08/2017.
 */
public class QueryEngineServerConfig {
    private long port;
    private boolean useCache;
    private int maxQueryTimeInSeconds = 600;
    private int maxHueQueryTimeInSeconds = 600;
    private boolean forceUseShuffleJoin = false;
    private List<String> visiblePropertyInSequence;
    private int maxVisiblePropertyInSequence = 100;
    private int defaultMaxGroupSize = 300;
    private QueryEngineServerConfig.DashBoardCachePolicy dashboardCachePolicy;
    private int cacheExpireDelayInSecs;
    private int approxDistinctThreshold;
    private String hueExternalTablesPattern;

    public QueryEngineServerConfig() {
        this.dashboardCachePolicy = QueryEngineServerConfig.DashBoardCachePolicy.NORMAL;
        this.cacheExpireDelayInSecs = 0;
        this.approxDistinctThreshold = -1;
        this.hueExternalTablesPattern = null;
    }

    public QueryEngineServerConfig.DashBoardCachePolicy getDashboardCachePolicy() {
        return this.dashboardCachePolicy;
    }

    public void setDashboardCachePolicy(QueryEngineServerConfig.DashBoardCachePolicy dashboardCachePolicy) {
        this.dashboardCachePolicy = dashboardCachePolicy;
    }

    public long getPort() {
        return this.port;
    }

    public void setPort(long port) {
        this.port = port;
    }

    public boolean isUseCache() {
        return this.useCache;
    }

    public void setUseCache(boolean isUseCache) {
        this.useCache = isUseCache;
    }

    public int getMaxQueryTimeInSeconds() {
        return this.maxQueryTimeInSeconds;
    }

    public void setMaxQueryTimeInSeconds(int maxQueryTimeInSeconds) {
        this.maxQueryTimeInSeconds = maxQueryTimeInSeconds;
    }

    public boolean isForceUseShuffleJoin() {
        return this.forceUseShuffleJoin;
    }

    public void setForceUseShuffleJoin(boolean forceUseShuffleJoin) {
        this.forceUseShuffleJoin = forceUseShuffleJoin;
    }

    public List<String> getVisiblePropertyInSequence() {
        return this.visiblePropertyInSequence;
    }

    public void setVisiblePropertyInSequence(List<String> inSequence) {
        this.visiblePropertyInSequence = inSequence;
    }

    public int getDefaultMaxGroupSize() {
        return this.defaultMaxGroupSize;
    }

    public void setDefaultMaxGroupSize(int maxGroupSize) {
        this.defaultMaxGroupSize = maxGroupSize;
    }

    public int getCacheExpireDelayInSecs() {
        return this.cacheExpireDelayInSecs;
    }

    public void setCacheExpireDelayInSecs(int cacheExpireDelayInSecs) {
        this.cacheExpireDelayInSecs = cacheExpireDelayInSecs;
    }

    public int getMaxVisiblePropertyInSequence() {
        return this.maxVisiblePropertyInSequence;
    }

    public void setMaxVisiblePropertyInSequence(int maxVisiblePropertyInSequence) {
        this.maxVisiblePropertyInSequence = maxVisiblePropertyInSequence;
    }

    public int getApproxDistinctThreshold() {
        return this.approxDistinctThreshold;
    }

    public void setApproxDistinctThreshold(int approxDistinctThreshold) {
        this.approxDistinctThreshold = approxDistinctThreshold;
    }

    public int getMaxHueQueryTimeInSeconds() {
        return this.maxHueQueryTimeInSeconds;
    }

    public void setMaxHueQueryTimeInSeconds(int maxHueQueryTimeInSeconds) {
        this.maxHueQueryTimeInSeconds = maxHueQueryTimeInSeconds;
    }

    public String getHueExternalTablesPattern() {
        return this.hueExternalTablesPattern;
    }

    public void setHueExternalTablesPattern(String hueExternalTablesPattern) {
        this.hueExternalTablesPattern = hueExternalTablesPattern;
    }

    public static enum DashBoardCachePolicy {
        NORMAL,
        FAST;

        private DashBoardCachePolicy() {
        }
    }
}
