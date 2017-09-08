package com.sensor.queryengine.query;

/**
 * Created by tianyi on 18/08/2017.
 */
public class JoinOptimizer {
    public JoinOptimizer() {
    }

    public static String optimize(String var0, String var1, boolean var2) {
        if(var2 && !var0.contains("/* +SHUFFLE */") && var1.contains("/* +SHUFFLE */")) {
            var0 = var0.replaceAll("LEFT JOIN ", "LEFT JOIN /* +SHUFFLE */ ");
        }

        return var0;
    }
}
