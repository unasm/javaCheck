package com.sensor.queryengine.error;

/**
 * Created by tianyi on 21/08/2017.
 */
public class EventNotExistsException  extends ParameterException{
    public EventNotExistsException(String ex, ErrorCode errorCode) {
        super(String.format("event name %s doesn\'t exit.", new Object[]{ex}), errorCode);
    }
}
