package com.sensor.db.dao;

import com.sensor.common.utils.SqlUtil;
import com.sensor.db.bean.PropertyMappingBean;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by tianyi on 01/09/2017.
 */
public class PropertyMappingDao extends AbstractDao{
    public static final String TABLE_NAME = "property_mapping";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PROPERTY_ID = "property_id";

    public PropertyMappingDao() {
    }

    public List<PropertyMappingBean> getAllPropertyMapping(int id, int limit) throws SQLException {
        String sql = SqlUtil.getSelectAllQuery("property_mapping");
        sql = sql + " WHERE id > ? ORDER BY id LIMIT ?";
        return this.query(sql, SqlUtil.createListResultHandler(PropertyMappingBean.class), id, limit);
    }

    public List<PropertyMappingBean> getAllPropertyMapping(int propertyId) throws SQLException {
        String sql = SqlUtil.getSelectQuery("property_mapping", "AND", "property_id");
        return this.query(sql, SqlUtil.createListResultHandler(PropertyMappingBean.class), propertyId);
    }
}
