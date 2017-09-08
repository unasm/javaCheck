package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;

/**
 * Created by tianyi on 01/09/2017.
 */
public class SamplingFilter extends AbstractFilter {
    protected Integer samplingFactor;

    public SamplingFilter(AbstractColumn idColumn, Integer samplingFactor) {
        super(idColumn);
        this.samplingFactor = samplingFactor;
    }

    public String constructSql() throws Exception {
        return String.format("(%s%%%d+%d)%%%d < %d", this.column.getId(), 64, 64, 64, this.samplingFactor);
    }
}
