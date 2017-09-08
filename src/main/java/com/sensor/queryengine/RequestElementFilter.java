package com.sensor.queryengine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tianyi on 01/08/2017.
 */
public class RequestElementFilter implements Serializable, Cloneable{
    private String relation;
    private List<RequestElementCondition> conditions;

    public RequestElementFilter() {
    }

    public String getRelation() {
        return this.relation;
    }

    public void setRelation(String var1) {
        this.relation = var1;
    }

    public void setConditions(List<RequestElementCondition> var1) {
        this.conditions = var1;
    }

    public List<RequestElementCondition> getConditions() {
        return this.conditions;
    }

    public boolean isAnd() {
        return StringUtils.isEmpty(this.relation) || this.relation.toUpperCase().equals("AND");
    }

    public boolean isOr() {
        return !this.isAnd();
    }

    public RequestElementFilter clone() {
        return SerializationUtils.clone(this);
    }

    public void setAbsoluteTimeByRelativeTime(Date var1) {
        if(this.conditions != null) {
            Iterator var2 = this.conditions.iterator();

            while(var2.hasNext()) {
                RequestElementCondition var3 = (RequestElementCondition)var2.next();
                var3.setAbsoluteTimeByRelativeTime(var1);
            }

        }
    }
}
