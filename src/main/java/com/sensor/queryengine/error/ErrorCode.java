package com.sensor.queryengine.error;

/**
 * Created by tianyi on 21/08/2017.
 */
public enum ErrorCode {
    EVENT_NOT_EXISTS,
    EVENT_INVISIBLE,
    FUNNEL_NOT_EXISTS,
    PROPERTY_NOT_EXISTS,
    FILTER_PARAMETER_NUM_INVALID,
    PARAMETER_FORMAT_ERROR,
    PARAMETER_INVALID,
    FILTER_FUNCTION_INVALID,
    CUSTOM_INDICATOR_INVALID,
    PROJECT_NOT_READY,
    OTHER;

    private ErrorCode() {
    }
}
