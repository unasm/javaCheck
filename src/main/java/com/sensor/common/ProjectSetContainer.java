package com.sensor.common;

import com.sensor.common.utils.ProjectContainer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tianyi on 29/08/2017.
 */
public class ProjectSetContainer<VALUE> extends ProjectContainer<Set<VALUE>>  {
    public ProjectSetContainer() {
    }

    public void add(int key, VALUE value) {
        Set<VALUE> set = this.get(key);
        if(set == null) {
            set = new HashSet<>();
            this.put(key, set);
        }

        set.add(value);
    }

    public boolean contains(int key, VALUE value) {
        Set set = (Set)this.get(key);
        return set != null && set.contains(value);
    }

    protected Set<VALUE> emptyValue() {
        return Collections.emptySet();
    }
}
