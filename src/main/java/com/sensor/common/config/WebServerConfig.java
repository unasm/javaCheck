package com.sensor.common.config;

/**
 * Created by tianyi on 26/08/2017.
 */
public class WebServerConfig {
    private Integer port;
    private Boolean autoRefresh = true;
    private Boolean showAllProject = true;
    private Boolean exportTemplate = false;
    private Boolean importTemplate = false;
    private Integer consumeDataMacConnection = 4;
    private Long consumeDataTotalRateLimit = 102400L;
    private Integer maxSegmenterNum = 20;
    private Integer listAccountLimit = 1000000;
    private String oauthAuthorizeUrl;
    private String oauthClientId;
    private String oauthRedirectUri;
    private String oauthScope;
    private String oauthClientSecret;
    private String oauthAccessTokenRequestUri;
    private String userInfoFetcherClass;
    private String defaultFetcherRequestUri;
    private Boolean customerExperienceImprovementProgram = false;

    public WebServerConfig() {
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Boolean getAutoRefresh() {
        return this.autoRefresh;
    }

    public void setAutoRefresh(Boolean isRefresh) {
        this.autoRefresh = isRefresh;
    }

    public Boolean getShowAllProject() {
        return this.showAllProject;
    }

    public void setShowAllProject(Boolean isShowAllPage) {
        this.showAllProject = isShowAllPage;
    }

    public Boolean getExportTemplate() {
        return this.exportTemplate;
    }

    public void setExportTemplate(Boolean exportTemplate) {
        this.exportTemplate = exportTemplate;
    }

    public Boolean getImportTemplate() {
        return this.importTemplate;
    }

    public void setImportTemplate(Boolean importTemplate) {
        this.importTemplate = importTemplate;
    }

    public int getMaxSegmenterNum() {
        return this.maxSegmenterNum;
    }

    public Integer getConsumeDataMacConnection() {
        return this.consumeDataMacConnection;
    }

    public void setConsumeDataMacConnection(Integer connection) {
        this.consumeDataMacConnection = connection;
    }

    public Long getConsumeDataTotalRateLimit() {
        return this.consumeDataTotalRateLimit;
    }

    public void setConsumeDataTotalRateLimit(Long limit) {
        this.consumeDataTotalRateLimit = limit;
    }

    public Integer getListAccountLimit() {
        return this.listAccountLimit;
    }

    public void setListAccountLimit(Integer limit) {
        this.listAccountLimit = limit;
    }

    public String getOauthAuthorizeUrl() {
        return this.oauthAuthorizeUrl;
    }

    public void setOauthAuthorizeUrl(String oauthAuthorizeUrl) {
        this.oauthAuthorizeUrl = oauthAuthorizeUrl;
    }

    public String getOauthClientId() {
        return this.oauthClientId;
    }

    public void setOauthClientId(String clientId) {
        this.oauthClientId = clientId;
    }

    public String getOauthRedirectUri() {
        return this.oauthRedirectUri;
    }

    public void setOauthRedirectUri(String uri) {
        this.oauthRedirectUri = uri;
    }

    public String getOauthScope() {
        return this.oauthScope;
    }

    public void setOauthScope(String oauthScope) {
        this.oauthScope = oauthScope;
    }

    public String getOauthClientSecret() {
        return this.oauthClientSecret;
    }

    public void setOauthClientSecret(String secret) {
        this.oauthClientSecret = secret;
    }

    public String getOauthAccessTokenRequestUri() {
        return this.oauthAccessTokenRequestUri;
    }

    public void setOauthAccessTokenRequestUri(String uri) {
        this.oauthAccessTokenRequestUri = uri;
    }

    public String getUserInfoFetcherClass() {
        return this.userInfoFetcherClass;
    }

    public void setUserInfoFetcherClass(String userInfo) {
        this.userInfoFetcherClass = userInfo;
    }

    public String getDefaultFetcherRequestUri() {
        return this.defaultFetcherRequestUri;
    }

    public void setDefaultFetcherRequestUri(String var1) {
        this.defaultFetcherRequestUri = var1;
    }

    public Boolean getCustomerExperienceImprovementProgram() {
        return this.customerExperienceImprovementProgram;
    }

    public void setCustomerExperienceImprovementProgram(Boolean var1) {
        this.customerExperienceImprovementProgram = var1;
    }
}
