package com.sensor.db.bean;

/**
 * Created by tianyi on 01/09/2017.
 */
public class PropertyMappingBean {
    private int id;
    private int propertyId;
    private int rawValueId;

    public PropertyMappingBean() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPropertyId() {
        return this.propertyId;
    }

    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }

    public int getRawValueId() {
        return this.rawValueId;
    }

    public void setRawValueId(int rawValueId) {
        this.rawValueId = rawValueId;
    }

    public String toString() {
        return "PropertyMappingBean{id=" + this.id + ", propertyId=" + this.propertyId + ", rawValueId=" + this.rawValueId + '}';
    }
}
