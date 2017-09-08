package com.sensor.queryengine.error;

/**
 * Created by tianyi on 01/09/2017.
 */
public class RequestCancelException extends  Exception {
    public RequestCancelException(String msg) {
        super(msg);
    }
}
