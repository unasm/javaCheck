package com.sensor.db.dao;

import com.sensor.common.utils.SqlUtil;
import com.sensor.db.bean.SessionBean;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 * session的define
 *
 * Created by tianyi on 26/08/2017.
 */
public class SessionDao  extends  AbstractDao{
    public static final String TABLE_NAME = "session_define";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CNAME = "cname";
    public static final String COLUMN_CREATE_TIME = "create_time";
    public static final String COLUMN_SESSION_RULE = "session_rule";
    public static final String COLUMN_EVENT_LIST = "event_list";
    public static final String COLUMN_PROJECT_ID = "project_id";
    public SessionDao() {
    }


    public List<SessionBean> getAllSessionByUpdateTime(Date updateTime, Set<Integer> projectIds) throws SQLException {
        String sql = String.format("SELECT * FROM %s WHERE update_time > ? AND project_id IN (" + StringUtils.join(projectIds, ',') + ")", "session_define");
        return this.query(sql, SqlUtil.createListResultHandler(SessionBean.class), updateTime);
    }

    public List<Integer> getExistsSessionIds(Set<Integer> ids, Set<Integer> projectIds) throws SQLException {
        if(ids.isEmpty()) {
            return Collections.emptyList();
        } else {
            String sql = "SELECT id FROM session_define WHERE id IN (" + StringUtils.join(ids, ',') + ") " + "AND project_id IN (" + StringUtils.join(projectIds, ',') + ")";
            return this.query(sql, new ColumnListHandler<Integer>());
        }
    }
}
