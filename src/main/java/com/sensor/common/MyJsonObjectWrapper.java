package com.sensor.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by tianyi on 10/08/2017.
 */
public class MyJsonObjectWrapper extends ObjectMapper{
    public MyJsonObjectWrapper() {
        //this.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
