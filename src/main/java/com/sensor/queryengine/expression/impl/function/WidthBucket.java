package com.sensor.queryengine.expression.impl.function;

import com.sensor.queryengine.expression.AbstractColumn;
import com.sensor.queryengine.expression.ExecutableExpression;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by tianyi on 05/09/2017.
 */
public class WidthBucket implements ExecutableExpression {
    private long max;
    private long min;
    private int bucketNum;
    private long step;

    public WidthBucket(long var1, long var3, int var5, long var6) {
        this.max = var1;
        this.min = var3;
        this.bucketNum = var5;
        this.step = var6;
    }

    public long getMax() {
        return this.max;
    }

    public void setMax(long var1) {
        this.max = var1;
    }

    public long getMin() {
        return this.min;
    }

    public void setMin(int var1) {
        this.min = (long)var1;
    }

    public void setMin(long var1) {
        this.min = var1;
    }

    public long getStep() {
        return this.step;
    }

    public void setStep(long var1) {
        this.step = var1;
    }

    public int getBucketNum() {
        return this.bucketNum;
    }

    public void setBucketNum(int var1) {
        this.bucketNum = var1;
    }

    public String eval(List<AbstractColumn> var1) throws Exception {
        if(var1.size() != 1) {
            throw new SQLException("invalid param number");
        } else {
            return String.format("WIDTH_BUCKET(%s, %d, %d, %d)", (var1.get(0)).getId(), this.min, this.max, this.bucketNum);
        }
    }

    public String toString() {
        return (new ToStringBuilder(this)).append("step", this.step).append("max", this.max).append("min", this.min).append("bucketNum", this.bucketNum).toString();
    }
}
