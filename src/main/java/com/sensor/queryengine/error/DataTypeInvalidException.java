package com.sensor.queryengine.error;

/**
 * Created by tianyi on 21/08/2017.
 */
public class DataTypeInvalidException extends ParameterException {
    public DataTypeInvalidException(String var1, String var2, ErrorCode var3) {
        super(String.format("need %s but get %s", new Object[]{var1, var2}), var3);
    }
}
