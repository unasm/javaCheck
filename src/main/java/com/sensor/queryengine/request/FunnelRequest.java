package com.sensor.queryengine.request;

import com.sensor.common.RequestType;
import com.sensor.queryengine.QueryRequest;
import com.sensor.queryengine.RequestElementFilter;

import java.util.List;

/**
 * Created by tianyi on 05/09/2017.
 */
public class FunnelRequest extends QueryRequest implements Cloneable{
    private int funnelId;
    private RequestElementFilter filter;
    private String byField;
    private List<Number> bucketParam;

    public FunnelRequest() {
        this.setRequestType(RequestType.FUNNEL);
    }

    public int getFunnelId() {
        return this.funnelId;
    }

    public void setFunnelId(int var1) {
        this.funnelId = var1;
    }

    public RequestElementFilter getFilter() {
        return this.filter;
    }

    public void setFilter(RequestElementFilter var1) {
        this.filter = var1;
    }

    public String getByField() {
        return this.byField;
    }

    public void setByField(String var1) {
        this.byField = var1;
    }

    public List<Number> getBucketParam() {
        return this.bucketParam;
    }

    public void setBucketParam(List<Number> var1) {
        this.bucketParam = var1;
    }

    public String toString() {
        return "FunnelRequest{funnelId=" + this.funnelId + ", filter=" + this.filter + ", byField=\'" + this.byField + '\'' + '}';
    }

    public FunnelRequest clone() {
        return (FunnelRequest)super.clone();
    }
}
