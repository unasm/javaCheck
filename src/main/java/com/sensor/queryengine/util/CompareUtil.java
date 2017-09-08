package com.sensor.queryengine.util;

import com.sensor.db.bean.EventBean;
import com.sensor.db.bean.PropertyBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyi on 21/08/2017.
 */
public class CompareUtil {
    private static final Logger logger = LoggerFactory.getLogger(CompareUtil.class);
    private static final Map<String, String> closeCnameMap = new HashMap();
    public static final Comparator<PropertyBean> cnameComparator;
    public static final Comparator<EventBean> eventCnameComparator;

    public CompareUtil() {
    }

    public static boolean equals(Object var0, Object var1) {
        if(var0 == null && var1 == null) {
            return true;
        } else {
            boolean var2 = false;
            if(var0 != null && var1 != null) {
                if(var0 instanceof Number) {
                    var2 = NumericUtil.compare((double)((Number)var0).floatValue(), (double)((Number)var1).floatValue()) == 0;
                } else {
                    var2 = var0.equals(var1);
                }
            }

            if(logger.isDebugEnabled() && !var2) {
                logger.debug("Object not equal. objA={}, classA={}, objB={}, classB={}", new Object[]{var0, var0 == null?null:var0.getClass(), var1, var1 == null?null:var1.getClass()});
            }

            return var2;
        }
    }

    public static boolean equals(List var0, List var1) {
        if(var0 == null && var1 == null) {
            return true;
        } else if(var0 != null && var1 != null) {
            if(var0.size() != var1.size()) {
                logger.debug("List size not equal. a={}, b={}", Integer.valueOf(var0.size()), Integer.valueOf(var1.size()));
                return false;
            } else {
                for(int var2 = 0; var2 < var0.size(); ++var2) {
                    Object var3 = var0.get(var2);
                    Object var4 = var1.get(var2);
                    if(!equals(var3, var4)) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    static {
        closeCnameMap.put("城市", "省份1");
        cnameComparator = new Comparator<PropertyBean>() {
            public int compare(PropertyBean var1, PropertyBean var2) {
                String var3 = this.getCname(var1);
                String var4 = this.getCname(var2);
                return var3.compareTo(var4);
            }

            private String getCname(PropertyBean var1) {
                String var2 = (String)CompareUtil.closeCnameMap.get(var1.getCname());
                if(null == var2) {
                    var2 = var1.getCname();
                    if(null == var2) {
                        var2 = var1.getName();
                    }
                }

                return var2;
            }
        };
        eventCnameComparator = new Comparator<EventBean>() {
            public int compare(EventBean var1, EventBean var2) {
                return var1.getCname() != null && var2.getCname() != null?var1.getCname().toLowerCase().compareTo(var2.getCname().toLowerCase()):0;
            }
        };
    }
}
