package com.sensor.queryengine.error;

/**
 * Created by tianyi on 21/08/2017.
 */
public class PropertyNotExistsException extends  ParameterException{
    public PropertyNotExistsException(String ex, ErrorCode errorCode) {
        super(String.format("event name %s doesn\'t exit.", new Object[]{ex}), errorCode);
    }
}
