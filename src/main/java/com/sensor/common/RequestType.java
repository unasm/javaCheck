package com.sensor.common;

/**
 * Created by tianyi on 01/08/2017.
 */
public enum RequestType {
    SEGMENTATION,
    FUNNEL,
    RETENTION,
    ADDICTION,
    TEST,
    SEGMENTATION_USER,
    FUNNEL_USER,
    RETENTION_USER,
    ADDICTION_USER,
    USER_EVENTS,
    COMMON_SEQUENCE,
    SEGMENTATION_SEQUENCE,
    FUNNEL_SEQUENCE,
    RETENTION_SEQUENCE,
    ADDICTION_SEQUENCE,
    USER,
    USER_ANALYTICS,
    PATH_ANALYTICS,
    PATH_ANALYTICS_USER;

    private RequestType() {
    }
}
