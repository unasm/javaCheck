package com.sensor.db.dao;

import com.sensor.common.utils.SqlUtil;
import com.sensor.db.bean.ProjectBean;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * 用于处理 project 表
 * Created by tianyi on 25/08/2017.
 */
public class ProjectDao  extends AbstractDao{
    public static final String TABLE_NAME = "project";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CNAME = "cname";
    public static final String COLUMN_CREATE_TIME = "create_time";
    public static final String COLUMN_DELETE_TIME = "delete_time";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_SUPER_TOKEN = "super_token";
    public static final String COLUMN_NORMAL_TOKEN = "normal_token";
    public static final String COLUMN_AUTO_CREATE = "is_auto_create";

    public ProjectDao() {
    }

    public static String generateDeleteName(ProjectBean projectBean) {
        return String.format("._%s_.%d", projectBean.getName(), projectBean.getId());
    }

    public String generateDeleteNamePattern(String names) {
        return String.format("._%s_.%%", names);
    }

    public List<ProjectBean> getAllProjects() throws SQLException {
        String var1 = SqlUtil.getSelectAllQuery("project");
        return this.query(var1, SqlUtil.createListResultHandler(ProjectBean.class));
    }

    public ProjectBean getProjectByName(String projectName) throws SQLException {
        String var2 = SqlUtil.getSelectQuery("project", "AND", "name");
        return this.query(var2, SqlUtil.createResultHandler(ProjectBean.class), projectName);
    }

    public ProjectBean getProjectById(int projectId) throws SQLException {
        String sql = SqlUtil.getSelectQuery("project", "AND", "id");
        ProjectBean bean = this.query(sql, SqlUtil.createResultHandler(ProjectBean.class), projectId);
        return bean == null ? null : bean;
    }

    public Long addProject(ProjectBean var1) throws SQLException {
        String sql = SqlUtil.getInsertQuery("project", "name", "cname", "create_time", "status", "super_token", "normal_token", "is_auto_create");
        return this.insert(sql, var1.getName(), var1.getCname(), new Date(System.currentTimeMillis()),
                        var1.getStatus(), var1.getSuperToken(), var1.getNormalToken(), var1.getAutoCreate());
    }

    public List<ProjectBean> getAllEnabledProjects() throws SQLException {
        String sql = SqlUtil.getSelectQuery("project", "AND", "status");
        ResultSetHandler<List<ProjectBean>> fields = SqlUtil.createListResultHandler(ProjectBean.class);
        return this.query(sql, fields, 1);
        //return (List<ProjectBean>)this.query(sql, SqlUtil.createListResultHandler(ProjectBean.class), 1);
    }

    public List<ProjectBean> getAllDisabledProjects() throws SQLException {
        String sql = "SELECT * FROM project WHERE status != 1";
        return this.query(sql, SqlUtil.createListResultHandler(ProjectBean.class));
    }

    public int updateProject(ProjectBean bean) throws SQLException {
        String sql = SqlUtil.getUpdateByIdQuery("project", "id", "name", "cname", "delete_time", "status", "super_token", "normal_token");
        return this.update(sql, bean.getName(), bean.getCname(), bean.getDeleteTime(),
                        bean.getStatus(), bean.getSuperToken(), bean.getNormalToken(), bean.getId());
    }

    public List<ProjectBean> getDeleteProjectWithSameName(String var1) throws SQLException {
        String sql = String.format("SELECT * FROM project WHERE name LIKE \'%s\'", this.generateDeleteNamePattern(var1));
        return this.query(sql, SqlUtil.createListResultHandler(ProjectBean.class));
    }
}
