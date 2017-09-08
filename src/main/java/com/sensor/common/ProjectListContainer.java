package com.sensor.common;

import com.sensor.common.utils.ProjectContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by tianyi on 28/08/2017.
 */
public class ProjectListContainer<VALUE> extends ProjectContainer<List<VALUE>> {
    public ProjectListContainer() {
    }

    public void add(int key, VALUE list) {
        List<VALUE> value = this.get(key);
        if(value == null) {
            value= new ArrayList<>();
            this.put(key, value);
        }

        value.add(list);
    }

    protected List<VALUE> emptyValue() {
        return Collections.emptyList();
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
