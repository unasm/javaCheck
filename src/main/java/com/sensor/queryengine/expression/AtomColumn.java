package com.sensor.queryengine.expression;

import com.sensor.common.DataType;
import com.sensor.queryengine.query.SQLQueryService;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * first_id, second_id这些 列的对象
 *
 * Created by tianyi on 14/08/2017.
 */
public class AtomColumn extends  AbstractColumn{

    protected String name;
    private String propertyName = null;
    private Integer propertyId = null;
    private AbstractColumn preColumn = null;
    private static final Logger logger = LoggerFactory.getLogger(AtomColumn.class);

    /**
     *
     * @param alias         example : a_0_$first_id
     * @param table
     * @param columnName    example : $id
     * @param propertyName  example :  $id, 在属性表中的名称
     * @param propertyId    example : 0 , 在属性表中的id
     * @param dataType      example : 1, 数据类型， 还有字符串类型，布尔类型
     * @param valueMapping  example : false
     * @param defaultValue  example : null
     */

    public AtomColumn(String alias, AbstractTable table, String columnName, String propertyName, Integer propertyId,
                      Integer dataType, Boolean valueMapping, String defaultValue) {
        super(alias, table);

        logger.debug("elements_create_atom, alias : {}, table : {}, columnName : {}, propertyName : {}, " +
                "propertyId : {}, dataType : {} , valueMapping : {}, defaultValue : {}", alias, table,
                columnName, propertyName, propertyId, dataType, valueMapping, defaultValue);

        this.name = columnName;
        this.propertyName = propertyName;
        this.propertyId = propertyId;
        this.valueMapping = valueMapping;
        if (null != dataType) {
            this.dataType = DataType.fromInt(dataType);
        }

        this.defaultValue = defaultValue;
    }

    public AtomColumn(String alias, AbstractTable table, AbstractColumn preColumn) {
        super(alias, table);
        this.preColumn = preColumn;
        this.name = preColumn.getAlias();
        if (preColumn instanceof AtomColumn) {
            this.propertyName = ((AtomColumn)preColumn).getPropertyName();
            this.propertyId = ((AtomColumn)preColumn).getPropertyId();
        }

        this.dataType = preColumn.getDataType();
        this.valueMapping = preColumn.isValueMapping();
        this.defaultValue = preColumn.getDefaultValue();
    }

    public boolean isEvent() {
        if (this.table instanceof Table) {
            return ((Table)this.table).getEventId() != 0;
        } else {
            throw new RuntimeException("error table type");
        }
    }

    public String constructSql() throws Exception {
        return String.format(" %s as %s ", this.getId(), this.escapeColumn(this.getAlias()));
    }

    public String getId() throws Exception {
        // this.name 替换掉  $
        String name = String.format("%s.%s", this.escapeColumn(this.table.getAlias()), this.escapeColumn(this.name.replace("$", "")));
        String res = this.defaultValue != null && this.preColumn == null ?
                    String.format("COALESCE(%s, %s)", name, this.defaultValue) : name;
        logger.debug("autoColumnId {}, {}, {}", name, this.name, this.toString());
        return res;
    }

    public String getById() {
        return String.format("%s.%s", this.escapeColumn(this.table.getAlias()), this.escapeColumn(this.alias.replace("$", "")));
    }

    public String getName() {
        return this.name.replace("$", "");
    }

    public void setName(String var1) {
        this.name = var1;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public void setPropertyName(String var1) {
        this.propertyName = var1;
    }

    public Integer getPropertyId() {
        return this.propertyId;
    }

    public void setPropertyId(Integer var1) {
        this.propertyId = var1;
    }

    public void setDataType(DataType var1) {
        this.dataType = var1;
    }

    public String escapeColumn(String var1) {
        return SQLQueryService.escapeColumn(var1);
    }

    public String toString() {
        return (new ToStringBuilder(this)).append("name", this.name).append("propertyName", this.propertyName).append("propertyId",
                    this.propertyId).append("dataType", this.dataType).append("preColumn", this.preColumn).toString();
    }
}
