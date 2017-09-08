package com.sensor.common.request;

import com.sensor.queryengine.RequestElementFilter;

import java.io.Serializable;

/**
 * Created by tianyi on 29/08/2017.
 */
public class RequestElementEventWithFilter implements Serializable{
    private String eventName;
    private RequestElementFilter filter;
    private String byField;

    public RequestElementEventWithFilter() {
    }

    public String getEventName() {
        return this.eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public RequestElementFilter getFilter() {
        return this.filter;
    }

    public void setFilter(RequestElementFilter filter) {
        this.filter = filter;
    }

    public String getByField() {
        return this.byField;
    }

    public void setByField(String field) {
        this.byField = field;
    }
}
