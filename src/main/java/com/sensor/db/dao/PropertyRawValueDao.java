package com.sensor.db.dao;

import com.sensor.common.utils.SqlUtil;
import com.sensor.db.bean.PropertyRawValueBean;

import java.sql.SQLException;
import java.util.List;

/**
 * 用于操作处理 property_raw_value 表
 *
 * 目前 数据库中 raw_value 表为空
 *
 * Created by tianyi on 01/09/2017.
 */
public class PropertyRawValueDao extends AbstractDao {
    public static final String TABLE_NAME = "property_raw_value";
    public static final String COLUMN_ID = "id";

    public PropertyRawValueDao() {
    }

    public List<PropertyRawValueBean> getAllPropertyRawValue(int id, int limit) throws SQLException {
        String sql = SqlUtil.getSelectAllQuery("property_raw_value");
        sql = sql + " WHERE id > ? LIMIT ?";
        return this.query(sql, SqlUtil.createListResultHandler(PropertyRawValueBean.class), id, limit);
    }
}
