package com.sensor.queryengine.executor;

import com.sensor.queryengine.QueryContext;
import com.sensor.queryengine.QueryRequest;
import com.sensor.queryengine.response.QueryResponse;
import com.sensor.queryengine.expression.AliasGenerator;

/**
 * Created by tianyi on 10/08/2017.
 */
public interface QueryExecutor {
    QueryResponse execute(QueryContext var1) throws Exception;

    default void restoreSamplingData(QueryRequest var1, QueryResponse var2) {
    }

    default String constructSQL(QueryContext var1, AliasGenerator var2) throws Exception {
        return null;
    }

    default void truncateResponse(QueryContext var1) {
    }
}
