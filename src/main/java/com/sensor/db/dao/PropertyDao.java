package com.sensor.db.dao;

import com.sensor.common.utils.SqlUtil;
import com.sensor.db.bean.EventPropertyBean;
import com.sensor.db.bean.PropertyBean;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * property_define 表的使用
 *
 * Created by tianyi on 26/08/2017.
 */
public class PropertyDao extends AbstractDao{
    public PropertyDao() {
    }


    public List<PropertyBean> getAllPropertyByUpdateTime(Date updateTime, Set<Integer> projects) throws SQLException {
        String sql = "SELECT * FROM property_define WHERE db_column_name IS NOT NULL AND " +
                "update_time > ? AND project_id IN (" + StringUtils.join(projects, ',') + ")";
        return this.query(sql, SqlUtil.createListResultHandler(PropertyBean.class), updateTime);
    }

    public List<EventPropertyBean> getAllUsedEventPropertiesByUpdateTime(Date updateTime, Set<Integer> projectIds, Set<Integer> properties) throws SQLException {
        String sql;
        if(properties.size() > 100) {
            sql = "SELECT * FROM event_property WHERE property_id IN (SELECT id FROM property_define WHERE db_column_name " +
                    "IS NOT NULL AND is_in_use = 1 AND project_id IN (" + StringUtils.join(projectIds, ',') + "))";
            return this.query(sql, SqlUtil.createListResultHandler(EventPropertyBean.class));
        } else {
            sql = "SELECT * FROM event_property WHERE (update_time > ? AND property_id IN (SELECT id FROM property_define " +
                    "WHERE db_column_name IS NOT NULL AND is_in_use = 1 AND project_id IN (" + StringUtils.join(projectIds, ',') + ")))";
            if(!properties.isEmpty()) {
                sql = sql + " OR property_id IN (" + StringUtils.join(properties, ',') + ")";
            }

            return this.query(sql, SqlUtil.createListResultHandler(EventPropertyBean.class), updateTime);
        }
    }
}
