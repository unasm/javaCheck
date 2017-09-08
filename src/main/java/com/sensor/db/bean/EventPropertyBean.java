package com.sensor.db.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by tianyi on 27/08/2017.
 */
public class EventPropertyBean implements Serializable, Cloneable{
    private int id;
    private int propertyId;
    private int eventId;
    private Date updateTime;

    public EventPropertyBean() {
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

    public int getEventId() {
        return this.eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String toString() {
        return "EventPropertyBean{id=" + this.id + ", propertyId=" + this.propertyId + ", eventId=" + this.eventId + '}';
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
