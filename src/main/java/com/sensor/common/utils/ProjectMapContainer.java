package com.sensor.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tianyi on 17/08/2017.
 */
public class ProjectMapContainer <KEY, VALUE> extends ProjectContainer<Map<KEY, VALUE>>  {
    private static final Logger logger = LoggerFactory.getLogger(ProjectMapContainer.class);
    private String desc = "default";

    public ProjectMapContainer() {
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public String getDesc() {
        return this.desc;
    }

    public VALUE get(int var1, KEY var2) {
        Map<KEY, VALUE> var3 = this.get(var1);
        return var3 == null ? null : var3.get(var2);
    }

    private Map<KEY, VALUE> newMapInstance() {
        return new ConcurrentHashMap<>();
    }

    protected Map<KEY, VALUE> emptyValue() {
        return Collections.emptyMap();
    }

    public void put(int projectId, KEY eventName, VALUE eventBean) {
        Map<KEY,VALUE> var4 = this.get(projectId);
        if(var4 == null) {
            var4 = this.newMapInstance();
            this.put(projectId, var4);
        }

        var4.put(eventName, eventBean);
    }

    public void clear(int var1) {
        Map var2 = (Map)this.get(var1);
        if(var2 != null) {
            var2.clear();
        }
    }

    public void remove(int var1, KEY var2) {
        Map var3 = (Map)this.get(var1);
        if(var3 != null) {
            var3.remove(var2);
        }
    }

    public boolean containsKey(int var1, KEY var2) {
        Map var3 = (Map)this.get(var1);
        return var3 != null && var3.containsKey(var2);
    }

    public Object clone() {
        ProjectMapContainer var1 = new ProjectMapContainer();
        Iterator var2 = this.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry var3 = (Map.Entry)var2.next();
            Map var4 = this.newMapInstance();
            Iterator var5 = ((Map)var3.getValue()).entrySet().iterator();

            while(var5.hasNext()) {
                Map.Entry var6 = (Map.Entry)var5.next();
                var4.put(var6.getKey(), var6.getValue());
            }

            var1.put(((Integer)var3.getKey()), var4);
        }

        return var1;
    }
}
