package com.sensor.db.dao;

import com.sensor.common.utils.SqlUtil;
import com.sensor.db.bean.FunnelBean;

import java.sql.SQLException;
import java.util.List;

/**
 * 用于处理 funel 表
 * Created by tianyi on 05/09/2017.
 */
public class FunnelDao extends AbstractDao {
    public static final String TABLE_NAME = "funnel";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_MAX_CONVERT_TIME = "max_convert_time";
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_PROJECT_ID = "project_id";

    public FunnelDao() {
    }

    public List<FunnelBean> getAllFunnel() throws SQLException {
        String var1 = SqlUtil.getSelectAllQuery("funnel");
        return this.query(var1, SqlUtil.createListResultHandler(FunnelBean.class));
    }

    public List<FunnelBean> getAllFunnelByProjectId(int projectId) throws SQLException {
        String sql = SqlUtil.getSelectQuery("funnel", "AND", "project_id");
        return this.query(sql, SqlUtil.createListResultHandler(FunnelBean.class), projectId);
    }

    public FunnelBean getFunnel(int id) throws SQLException {
        String sql = SqlUtil.getSelectQuery("funnel", "OR", "id");
        return this.query(sql, SqlUtil.createResultHandler(FunnelBean.class), id);
    }

    public FunnelBean getFunnel(String name, int projectId) throws SQLException {
        String var3 = SqlUtil.getSelectQuery("funnel", "AND", "name", "project_id");
        return this.query(var3, SqlUtil.createResultHandler(FunnelBean.class), name, projectId);
    }

    public Long addFunnel(int var1, String var2, int var3, String var4, String var5, Integer var6) throws SQLException {
        String var7 = SqlUtil.getInsertQuery("funnel", "user_id", "name", "max_convert_time", "steps", "comment", "project_id");
        return this.insert(var7, var1, var2, var3, var4, var5, var6);
    }

    public void updateFunnel(int var1, int var2, String var3, int var4, String var5, String var6) throws SQLException {
        String var7 = SqlUtil.getUpdateByIdQuery("funnel", "id", "user_id", "name", "max_convert_time", "steps", "comment");
        this.update(var7, var2, var3, var4, var5, var6, var1);
    }

    public void updateFunnel(int var1, int var2, String var3, int var4, String var5) throws SQLException {
        String var6 = SqlUtil.getUpdateByIdQuery("funnel", "id", "user_id", "name", "max_convert_time", "steps");
        this.update(var6, var2, var3, var4, var5, var1);
    }

    public void deleteFunnel(int id) throws SQLException {
        String sql = SqlUtil.getDeleteByColumnQuery("funnel", "id");
        this.update(sql, id);
    }
}
