package com.sensor.common.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by tianyi on 14/08/2017.
 */
public class ProjectContainer<T> {
    private Map<Integer, T> map = new ConcurrentHashMap<>();

    public ProjectContainer() {
    }

    public T get(int mapId) {
        return this.map.get(mapId);
    }

    public T getOrEmpty(int mapId) {
        T value = this.map.get(mapId);
        //Object value = this.map.get(mapId);
        return value == null ? this.emptyValue() : value;
    }

    protected T emptyValue() {
        return null;
    }

    public void put(int key, T value) {
        this.map.put(key, value);
    }

    public Set<Integer> keySet() {
        return this.map.keySet();
    }

    public Set<Entry<Integer, T>> entrySet() {
        return this.map.entrySet();
    }

    public Collection<T> values() {
        return this.map.values();
    }

    public void clear() {
        this.map.clear();
    }

    public int size() {
        return this.map.size();
    }

    public void remove(Integer mapId) {
        this.map.remove(mapId);
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }
}
