package com.sensor.queryengine.error;

import com.sensor.common.SensorsAnalyticsException;

/**
 * Created by tianyi on 21/08/2017.
 */
public class ParameterException  extends SensorsAnalyticsException {
    private ErrorCode errorCode;

    public ParameterException(String var1, ErrorCode var2) {
        super(var1);
        this.errorCode = var2;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }
}
