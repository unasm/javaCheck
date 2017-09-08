package com.sensor.common.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.joda.time.DateTime;

/**
 * Created by tianyi on 01/08/2017.
 */
public class GlobalConfigInfo {
    private String defaultTimeZoneId;
    private String dataSinkUrl;
    private String webUrl;
    private String vtrackServerUrl;
    private String customerId;
    private DateTime installTime;
    private DateTime expireTime;
    private DateTime deadTime;
    private DateTime remindTime;
    private long maxMessageCount;
    private long maxCoreNum;
    private long maxMemoryGB;
    private long maxNodeNum;
    private long maxProjectNum;
    private String license;
    private GlobalConfigInfo.InstallType installType;
    private long bkdrHashFactor = 31L;

    public GlobalConfigInfo() {
    }

    public String getWebUrl() {
        return this.webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getVtrackServerUrl() {
        return this.vtrackServerUrl;
    }

    public void setVtrackServerUrl(String serverUrl) {
        this.vtrackServerUrl = serverUrl;
    }

    public synchronized void copy(GlobalConfigInfo config) {
        this.defaultTimeZoneId = config.defaultTimeZoneId;
        this.dataSinkUrl = config.dataSinkUrl;
        this.customerId = config.customerId;
        this.installTime = config.installTime;
        this.expireTime = config.expireTime;
        this.deadTime = config.deadTime;
        this.remindTime = config.remindTime;
        this.maxMessageCount = config.maxMessageCount;
        this.maxCoreNum = config.maxCoreNum;
        this.maxMemoryGB = config.maxMemoryGB;
        this.maxNodeNum = config.maxNodeNum;
        this.maxProjectNum = config.maxProjectNum;
        this.license = config.license;
        this.installType = config.installType;
        this.bkdrHashFactor = config.bkdrHashFactor;
    }

    public String getDefaultTimeZoneId() {
        return this.defaultTimeZoneId;
    }

    public void setDefaultTimeZoneId(String defaultTimeZoneId) {
        this.defaultTimeZoneId = defaultTimeZoneId;
    }

    public String getDataSinkUrl() {
        return this.dataSinkUrl;
    }

    public void setDataSinkUrl(String sinkUrl) {
        this.dataSinkUrl = sinkUrl;
    }

    public synchronized String getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /*
    @JsonSerialize(
            using = JodaDatetimeSerializer.class
    )
    public synchronized DateTime getInstallTime() {
        return this.installTime;
    }

    @JsonDeserialize(
            using = JodaDatetimeDeserializer.class
    )
    public void setInstallTime(DateTime var1) {
        this.installTime = var1;
    }

    @JsonSerialize(
            using = JodaDatetimeSerializer.class
    )
    public synchronized DateTime getExpireTime() {
        return this.expireTime;
    }

    @JsonDeserialize(
            using = JodaDatetimeSerializer.class
    )
    public void setExpireTime(DateTime var1) {
        this.expireTime = var1;
    }

    @JsonSerialize(
            using = JodaDatetimeSerializer.class
    )
    public synchronized DateTime getDeadTime() {
        return this.deadTime;
    }

    @JsonDeserialize(
            using = JodaDatetimeDeserializer.class
    )
    public void setDeadTime(DateTime var1) {
        this.deadTime = var1;
    }

    @JsonSerialize(
            using = JodaDatetimeSerializer.class
    )
    public synchronized DateTime getRemindTime() {
        return this.remindTime;
    }

    @JsonDeserialize(
            using = JodaDatetimeDeserializer.class
    )
    public void setRemindTime(DateTime var1) {
        this.remindTime = var1;
    }
    */
    public synchronized long getMaxMessageCount() {
        return this.maxMessageCount;
    }

    public void setMaxMessageCount(long maxMessageCount) {
        this.maxMessageCount = maxMessageCount;
    }

    public synchronized long getMaxCoreNum() {
        return this.maxCoreNum;
    }

    public void setMaxCoreNum(long maxCoreNum) {
        this.maxCoreNum = maxCoreNum;
    }

    public synchronized long getMaxMemoryGB() {
        return this.maxMemoryGB;
    }

    public void setMaxMemoryGB(long maxMemoryGB) {
        this.maxMemoryGB = maxMemoryGB;
    }

    public synchronized long getMaxNodeNum() {
        return this.maxNodeNum;
    }

    public void setMaxNodeNum(long var1) {
        this.maxNodeNum = var1;
    }

    public synchronized long getMaxProjectNum() {
        return this.maxProjectNum;
    }

    public void setMaxProjectNum(long var1) {
        this.maxProjectNum = var1;
    }

    public synchronized String getLicense() {
        return this.license;
    }

    public void setLicense(String var1) {
        this.license = var1;
    }

    public GlobalConfigInfo.InstallType getInstallType() {
        return this.installType;
    }

    public void setInstallType(GlobalConfigInfo.InstallType type) {
        this.installType = type;
    }

    public String toString() {
        StringBuilder str = new StringBuilder("GlobalConfigInfo{");
        str.append("defaultTimeZoneId=\'").append(this.defaultTimeZoneId).append('\'');
        str.append(", dataSinkUrl=\'").append(this.dataSinkUrl).append('\'');
        str.append(", webUrl=\'").append(this.webUrl).append('\'');
        str.append(", vtrackServerUrl=\'").append(this.vtrackServerUrl).append('\'');
        str.append(", customerId=\'").append(this.customerId).append('\'');
        str.append(", installTime=").append(this.installTime);
        str.append(", expireTime=").append(this.expireTime);
        str.append(", deadTime=").append(this.deadTime);
        str.append(", remindTime=").append(this.remindTime);
        str.append(", maxMessageCount=").append(this.maxMessageCount);
        str.append(", maxCoreNum=").append(this.maxCoreNum);
        str.append(", maxMemoryGB=").append(this.maxMemoryGB);
        str.append(", maxNodeNum=").append(this.maxNodeNum);
        str.append(", maxProjectNum=").append(this.maxProjectNum);
        str.append(", license=\'").append(this.license).append('\'');
        str.append(", installType=").append(this.installType);
        str.append(", bkdrHashFactor=").append(this.bkdrHashFactor);
        str.append('}');
        return str.toString();
    }

    public long getBkdrHashFactor() {
        return this.bkdrHashFactor;
    }

    public void setBkdrHashFactor(long var1) {
        this.bkdrHashFactor = var1;
    }

    public static enum InstallType {
        STANDALONE("standalone"),
        CLUSTER("cluster");

        private String key;

        private InstallType(String var3) {
            this.key = var3;
        }

        @JsonCreator
        public static GlobalConfigInfo.InstallType fromString(String var0) {
            return var0 == null?null:valueOf(var0.toUpperCase());
        }

        @JsonValue
        public String getKey() {
            return this.key;
        }
    }
}
