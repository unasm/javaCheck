package com.sensor.queryengine.request;

import com.sensor.common.RequestType;
import com.sensor.queryengine.QueryRequest;
import com.sensor.queryengine.RequestElementFilter;
import com.sensor.queryengine.request.element.RequestElementMeasure;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyi on 05/09/2017.
 */
public class SegmentationRequest extends QueryRequest implements Cloneable{

    private RequestElementFilter filter;
    private List<String> byFields;
    private Boolean rollupDate = false;
    private Map<String, List<Number>> bucketParams;
    private List<RequestElementMeasure> measures;
    private String sessionName;
    private Boolean download = false;
    private Collection<Object> byValuesFilter;

    public SegmentationRequest() {
        this.setRequestType(RequestType.SEGMENTATION);
    }

    public RequestElementFilter getFilter() {
        return this.filter;
    }

    public void setFilter(RequestElementFilter var1) {
        this.filter = var1;
    }

    public List<String> getByFields() {
        return this.byFields;
    }

    public void setByFields(List<String> var1) {
        this.byFields = var1;
    }

    public Boolean isRollupDate() {
        return this.rollupDate;
    }

    public void setRollupDate(Boolean var1) {
        this.rollupDate = var1;
    }

    public Map<String, List<Number>> getBucketParams() {
        return this.bucketParams;
    }

    public void setBucketParams(Map<String, List<Number>> var1) {
        this.bucketParams = var1;
    }

    public List<RequestElementMeasure> getMeasures() {
        return this.measures;
    }

    public void setMeasures(List<RequestElementMeasure> var1) {
        this.measures = var1;
    }

    public String getSessionName() {
        return this.sessionName;
    }

    public void setSessionName(String var1) {
        this.sessionName = var1;
    }

    public Boolean isDownload() {
        return this.download;
    }

    public void setDownload(Boolean var1) {
        this.download = var1;
    }

    public Collection<Object> getByValuesFilter() {
        return this.byValuesFilter;
    }

    public void setByValuesFilter(Collection<Object> var1) {
        this.byValuesFilter = var1;
    }
}
