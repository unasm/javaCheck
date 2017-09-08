package com.sensor.queryengine.expression;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tianyi on 03/09/2017.
 */
public interface ExecutableExpression  extends Serializable{
    String eval(List<AbstractColumn> columns) throws Exception;
}
