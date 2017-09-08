package com.sensor.db.bean;

/**
 * Created by tianyi on 01/09/2017.
 */
public class PropertyRawValueBean {
    private int id;
    private String rawValue;

    public PropertyRawValueBean() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRawValue() {
        return this.rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public String toString() {
        return "PropertyRawValue{id=" + this.id + ", rawValue=\'" + this.rawValue + '\'' + '}';
    }
}
