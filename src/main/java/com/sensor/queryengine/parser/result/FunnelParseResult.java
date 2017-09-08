package com.sensor.queryengine.parser.result;

import com.sensor.common.request.RequestElementEventWithFilter;

import java.util.List;

/**
 * Created by tianyi on 05/09/2017.
 */
public class FunnelParseResult extends  ParseResult {
    private List<RequestElementEventWithFilter> steps;
    private int maxConvertTime;

    public FunnelParseResult() {
    }

    public int getMaxConvertTime() {
        return this.maxConvertTime;
    }

    public void setMaxConvertTime(int maxConvertTime) {
        this.maxConvertTime = maxConvertTime;
    }

    public List<RequestElementEventWithFilter> getSteps() {
        return this.steps;
    }

    public void setSteps(List<RequestElementEventWithFilter> steps) {
        this.steps = steps;
    }
}
