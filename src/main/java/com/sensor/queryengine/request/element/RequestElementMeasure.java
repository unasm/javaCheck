package com.sensor.queryengine.request.element;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tianyi on 05/09/2017.
 */
public class RequestElementMeasure implements Serializable, Cloneable{

    private String eventName;
    private String aggregator;
    private String field;
    private String expression;
    private Boolean bySession;
    private List<String> events;

    public RequestElementMeasure() {
    }

    public Boolean isBySession() {
        return this.bySession != null && this.bySession;
    }

    public void setBySession(Boolean var1) {
        this.bySession = var1;
    }

    public String getEventName() {
        return this.eventName;
    }

    public void setEventName(String var1) {
        this.eventName = var1;
    }

    public String getAggregator() {
        return this.aggregator;
    }

    public void setAggregator(String var1) {
        this.aggregator = var1;
    }

    public String getField() {
        return this.field;
    }

    public void setField(String var1) {
        this.field = var1;
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String var1) {
        this.expression = var1;
    }

    public List<String> getEvents() {
        return this.events;
    }

    public void setEvents(List<String> var1) {
        this.events = var1;
    }

    public RequestElementMeasure clone() {
        return (RequestElementMeasure) SerializationUtils.clone(this);
    }
}
