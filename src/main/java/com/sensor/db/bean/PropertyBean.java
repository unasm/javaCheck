package com.sensor.db.bean;

import com.sensor.common.DataType;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by tianyi on 11/08/2017.
 */
public class PropertyBean implements Serializable, Cloneable {
    private int id;
    private int tableType;
    private String name;
    private String cname;
    private String dbColumnName;
    private String dbColumnNameRedundancy;
    private int dataType;
    private boolean isValueMapping;
    private int cardinality;
    private boolean isDimension;
    private boolean isMeasure;
    private boolean isInUse;
    private String comment;
    private String unit;
    private boolean hasDict;
    private int projectId;
    private String defaultValue;
    private Date updateTime;
    private boolean isSegmenter = false;
    private boolean typeFlexible = false;

    public PropertyBean() {
    }

    public Object clone() throws CloneNotSupportedException {
        PropertyBean var1 = new PropertyBean();
        var1.setId(this.id);
        var1.setTableType(this.tableType);
        var1.setName(this.name);
        var1.setCname(this.cname);
        var1.setDbColumnName(this.dbColumnName);
        var1.setDbColumnNameRedundancy(this.dbColumnNameRedundancy);
        var1.dataType = this.dataType;
        var1.isValueMapping = this.isValueMapping;
        var1.cardinality = this.cardinality;
        var1.isDimension = this.isDimension;
        var1.isMeasure = this.isMeasure;
        var1.isInUse = this.isInUse;
        var1.comment = this.comment;
        var1.unit = this.unit;
        var1.hasDict = this.hasDict;
        var1.projectId = this.projectId;
        var1.defaultValue = this.defaultValue;
        var1.updateTime = this.updateTime;
        var1.isSegmenter = this.isSegmenter;
        var1.typeFlexible = this.typeFlexible;
        return var1;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTableType() {
        return this.tableType;
    }

    public void setTableType(int var1) {
        this.tableType = var1;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getCname() {
        return this.cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getDbColumnName() {
        return this.dbColumnName;
    }

    public void setDbColumnName(String columnName) {
        this.dbColumnName = columnName;
    }

    public String getDbColumnNameRedundancy() {
        return this.dbColumnNameRedundancy;
    }

    public void setDbColumnNameRedundancy(String dbColumnNameRedundancy) {
        this.dbColumnNameRedundancy = dbColumnNameRedundancy;
    }

    public int getDataType() {
        return this.dataType;
    }

    public void setDataType(int var1) {
        this.dataType = var1;
    }

    public boolean isValueMapping() {
        return this.isValueMapping;
    }

    public void setIsValueMapping(boolean var1) {
        this.isValueMapping = var1;
    }

    public int getCardinality() {
        return this.cardinality;
    }

    public void setCardinality(int var1) {
        this.cardinality = var1;
    }

    public boolean isDimension() {
        return this.isDimension;
    }

    public void setDimension(boolean var1) {
        this.isDimension = var1;
    }

    public boolean isMeasure() {
        return this.isMeasure;
    }

    public void setMeasure(boolean var1) {
        this.isMeasure = var1;
    }

    public boolean isInUse() {
        return this.isInUse;
    }

    public void setInUse(boolean var1) {
        this.isInUse = var1;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String var1) {
        this.comment = var1;
    }

    public String getUnit() {
        return this.unit;
    }

    public void setUnit(String var1) {
        this.unit = var1;
    }

    public boolean hasDict() {
        return this.hasDict;
    }

    public void setHasDict(boolean var1) {
        this.hasDict = var1;
    }

    public boolean isEventProperty() {
        return this.tableType == 0;
    }

    public boolean isSuperEventProperty() {
        return this.tableType == 0 && this.name.startsWith("$");
    }

    public boolean isUserProfile() {
        return this.tableType == 1;
    }

    public boolean isSessionProperty() {
        return this.tableType == 0 && this.name.contains("$session");
    }

    public String toString() {
        StringBuilder beanStr = new StringBuilder("PropertyBean{");
        beanStr.append("id=").append(this.id);
        beanStr.append(", tableType=").append(this.tableType);
        beanStr.append(", name=\'").append(this.name).append('\'');
        beanStr.append(", cname=\'").append(this.cname).append('\'');
        beanStr.append(", dbColumnName=\'").append(this.dbColumnName).append('\'');
        beanStr.append(", dbColumnNameRedundancy=\'").append(this.dbColumnNameRedundancy).append('\'');
        beanStr.append(", dataType=").append(this.dataType);
        beanStr.append(", isValueMapping=").append(this.isValueMapping);
        beanStr.append(", cardinality=").append(this.cardinality);
        beanStr.append(", isDimension=").append(this.isDimension);
        beanStr.append(", isMeasure=").append(this.isMeasure);
        beanStr.append(", isInUse=").append(this.isInUse);
        beanStr.append(", comment=\'").append(this.comment).append('\'');
        beanStr.append(", unit=\'").append(this.unit).append('\'');
        beanStr.append(", hasDict=").append(this.hasDict);
        beanStr.append(", projectId=").append(this.projectId);
        beanStr.append(", defaultValue=\'").append(this.defaultValue).append('\'');
        beanStr.append(", updateTime=").append(this.updateTime);
        beanStr.append(", isSegmenter=").append(this.isSegmenter);
        beanStr.append(", typeFlexible=").append(this.typeFlexible);
        beanStr.append('}');
        return beanStr.toString();
    }

    public int getProjectId() {
        return this.projectId;
    }

    public void setProjectId(int var1) {
        this.projectId = var1;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String var1) {
        this.defaultValue = var1;
    }

    public boolean needDivThousand() {
        return this.dataType == DataType.NUMBER.getIndex() && !this.name.startsWith("$session_") && !this.name.equals("$event_id$session") && !this.name.equals("$event_id");
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date var1) {
        this.updateTime = var1;
    }

    public boolean isSegmenter() {
        return this.isSegmenter;
    }

    public void setIsSegmenter(boolean var1) {
        this.isSegmenter = var1;
    }

    public boolean isTypeFlexible() {
        return this.typeFlexible;
    }

    public void setTypeFlexible(boolean var1) {
        this.typeFlexible = var1;
    }
}
