package com.sensor.queryengine.expression.filter;

import com.sensor.queryengine.expression.AbstractColumn;

/**
 * Created by tianyi on 02/09/2017.
 */
public class Less extends SimpleFilter {
    public Less(AbstractColumn var1, Object var2) {
        super(var1, "<", var2);
    }
}
