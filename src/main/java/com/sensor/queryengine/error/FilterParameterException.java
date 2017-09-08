package com.sensor.queryengine.error;

/**
 * Created by tianyi on 05/09/2017.
 */
public class FilterParameterException extends ParameterException {
    public FilterParameterException(String var1, int var2, ErrorCode var3) {
        super(String.format("%s need %d parameters.", var1, var2), var3);
    }

    public FilterParameterException(String var1, String var2, ErrorCode var3) {
        super(String.format("%s need %s parameters.", var1, var2), var3);
    }

    public FilterParameterException(String var1, ErrorCode var2) {
        super(String.format("filter function can not be %s", var1), var2);
    }
}
