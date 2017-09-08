package com.sensor.queryengine.expression;

import java.io.Serializable;

/**
 * Created by tianyi on 14/08/2017.
 */
public interface SqlGenerator extends Serializable {
    String constructSql() throws Exception;
}
