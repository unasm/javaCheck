package com.sensor.queryengine;

import com.sensor.common.DateFormat;
import com.sensor.common.RequestType;
import com.sensor.common.util.DateUnit;
import com.sensor.common.util.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by tianyi on 01/08/2017.
 */
public abstract class QueryRequest implements Cloneable {
    private String fromDate;
    private String toDate;
    private String unit;
    private RequestType requestType;
    private Integer samplingFactor;
    private String requestId;
    private Boolean useCache = Boolean.valueOf(true);
    private Boolean ignoreCacheExpire = Boolean.valueOf(false);
    private Boolean internal = Boolean.valueOf(false);
    private Boolean handleSampling = Boolean.valueOf(true);
    private Boolean extendOverEndDate = Boolean.valueOf(true);
    private Long limit;
    private Integer dashboardId;

    public QueryRequest() {
    }

    public Boolean isIgnoreCacheExpire() {
        return this.ignoreCacheExpire;
    }

    public void setIgnoreCacheExpire(Boolean var1) {
        this.ignoreCacheExpire = var1;
    }

    public Date getNormalizedFromDate() {
        return DateUtil.parseInputDate(this.fromDate, this.getNormalizedDateUnit());
    }

    public Date getNormalizedToDate() {
        return DateUtil.parseInputDate(this.toDate, this.getNormalizedDateUnit());
    }

    public Date getNormalizedFromDateTime() {
        return DateUtil.parseInputDate(this.fromDate, DateUnit.HOUR);
    }

    public Date getNormalizedToDateTime() {
        if(this.getNormalizedDateUnit().isLessThenHourLevel()) {
            Date var1 = null;

            try {
                var1 = DateFormat.DEFAULT_DATETIME_FORMAT.parse(this.toDate);
            } catch (ParseException var7) {
                try {
                    var1 = DateFormat.SHORT_DATETIME_FORMAT.parse(this.toDate);
                } catch (ParseException var6) {
                    ;
                }
            }

            if(var1 == null) {
                try {
                    var1 = DateFormat.DEFAULT_DAY_FORMAT.parse(this.toDate);
                } catch (ParseException var5) {
                    try {
                        var1 = DateFormat.SHORT_DAY_FORMAT.parse(this.toDate);
                    } catch (ParseException var4) {
                        ;
                    }
                }

                return DateUtil.nextDateUnit(var1, 23, DateUnit.HOUR);
            }
        }

        return DateUtil.parseInputDate(this.toDate, DateUnit.HOUR);
    }

    public DateUnit getNormalizedDateUnit() {
        return StringUtils.isEmpty(this.unit)?DateUnit.DAY:DateUnit.valueOf(this.unit.toUpperCase());
    }

    public String getFromDate() {
        return this.fromDate;
    }

    public void setFromDate(String var1) {
        this.fromDate = var1;
    }

    public String getToDate() {
        return this.toDate;
    }

    public void setToDate(String var1) {
        this.toDate = var1;
    }

    public QueryRequest clone() {
        try {
            return (QueryRequest)super.clone();
        } catch (CloneNotSupportedException var2) {
            return null;
        }
    }

    public String getUnit() {
        return this.unit;
    }

    public void setUnit(String var1) {
        this.unit = var1;
    }

    public RequestType getRequestType() {
        return this.requestType;
    }

    public void setRequestType(RequestType var1) {
        this.requestType = var1;
    }

    public Integer getSamplingFactor() {
        if(this.samplingFactor != null && this.samplingFactor.intValue() == 10) {
            this.samplingFactor = Integer.valueOf(64);
        }

        return this.samplingFactor;
    }

    public void setSamplingFactor(Integer var1) {
        this.samplingFactor = var1;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String var1) {
        this.requestId = var1;
    }

    public Boolean getUseCache() {
        return this.useCache;
    }

    public void setUseCache(Boolean var1) {
        this.useCache = var1;
    }

    public Boolean isInternal() {
        return this.internal;
    }

    public void setInternal(Boolean var1) {
        this.internal = var1;
    }

    public Boolean getHandleSampling() {
        return this.handleSampling;
    }

    public void setHandleSampling(Boolean var1) {
        this.handleSampling = var1;
    }

    public Long getLimit() {
        return this.limit;
    }

    public void setLimit(Long var1) {
        this.limit = var1;
    }

    public Boolean isExtendOverEndDate() {
        return this.extendOverEndDate;
    }

    public void setExtendOverEndDate(Boolean var1) {
        this.extendOverEndDate = var1;
    }

    public Integer getDashboardId() {
        return this.dashboardId;
    }

    public void setDashboardId(Integer var1) {
        this.dashboardId = var1;
    }
}
