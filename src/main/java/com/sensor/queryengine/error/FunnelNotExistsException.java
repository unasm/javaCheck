package com.sensor.queryengine.error;

/**
 * Created by tianyi on 05/09/2017.
 */
public class FunnelNotExistsException extends ParameterException{
    public FunnelNotExistsException(int var1, ErrorCode var2) {
        super(String.format("funnel isn\'t exit, funnel_id = %s", new Object[]{Integer.valueOf(var1)}), var2);
    }
}
