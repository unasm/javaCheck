package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;

/**
 * Created by tianyi on 02/09/2017.
 */
public class Greater extends SimpleFilter {
    public Greater(AbstractColumn var1, boolean var2, Object var3) {
        super(var1, ">=", var3);
    }

    public Greater(AbstractColumn var1, Object var2) {
        super(var1, ">", var2);
    }
}
