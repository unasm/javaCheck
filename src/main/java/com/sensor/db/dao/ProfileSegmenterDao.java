package com.sensor.db.dao;

import com.sensor.common.utils.SqlUtil;
import com.sensor.db.bean.ProfileSegmenterBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 *
 * 用户分群表， profile_segmentation
 *
 * Created by tianyi on 27/08/2017.
 */
public class ProfileSegmenterDao extends AbstractDao{
    public static final String TABLE_NAME = "profile_segmentation";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CNAME = "cname";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_LAST_PARTITION = "last_partition";
    public static final String COLUMN_DEFAULT_VALUE = "default_value";

    public ProfileSegmenterDao() {
    }

    public Long addSegmenter(ProfileSegmenterBean var1) throws SQLException {
        String var2 = SqlUtil.getInsertQuery("profile_segmentation", "name", "cname", "type", "status", "content", "project_id", "last_partition", "default_value");
        return this.insert(var2, var1.getName(), var1.getCname(), var1.getType(), var1.getStatus(), var1.getContent(),
                    var1.getProjectId(), var1.getLastPartition(), var1.getDefaultValue());
    }

    public ProfileSegmenterBean getSegmenterBeanInAssignedStatus(int var1) throws SQLException {
        String var2 = "SELECT * from `profile_segmentation` WHERE id=? and (status in (\'new \', \'finish\', \'failed\') or status rlike \'failed.*\')";
        return this.query(var2, SqlUtil.createResultHandler(ProfileSegmenterBean.class), var1);
    }

    public int updateStatusToPreparing(int var1) throws SQLException {
        String var2 = "UPDATE `profile_segmentation` SET status=\'preparing\' WHERE id=? and (status in (\'new\', \'finish\', \'failed\') or status rlike \'failed.*\')";
        return this.update(var2, var1);
    }

    public int updateStatusToFailed(int var1) throws SQLException {
        String var2 = "UPDATE `profile_segmentation` SET status=\'failed\' WHERE id=?";
        return this.update(var2, var1);
    }

    public void updateSegmenterBean(ProfileSegmenterBean var1) throws SQLException {
        String var2 = SqlUtil.getUpdateByIdQuery("profile_segmentation", "id", "name", "cname", "type", "status", "content", "default_value");
        this.update(var2, var1.getName(), var1.getCname(), var1.getType(), var1.getStatus(), var1.getContent(), var1.getDefaultValue(), var1.getId());
    }

    public void deleteSegmenter(int id) throws SQLException {
        String sql = SqlUtil.getDeleteByColumnQuery("profile_segmentation", "id");
        this.update(sql, id);
    }

    public List<ProfileSegmenterBean> getAllSegmenterByProjectId(int projectId) throws SQLException {
        String var2 = SqlUtil.getSelectQuery("profile_segmentation", "AND", "project_id");
        return this.query(var2, SqlUtil.createListResultHandler(ProfileSegmenterBean.class), projectId);
    }

    public List<ProfileSegmenterBean> getAllSegmenter(Set<Integer> var1) throws SQLException {
        String var2 = SqlUtil.getSelectAllQuery("profile_segmentation");
        if(CollectionUtils.isNotEmpty(var1)) {
            var2 = var2 + " WHERE project_id IN (" + StringUtils.join(var1, ',') + ")";
        }

        return this.query(var2, SqlUtil.createListResultHandler(ProfileSegmenterBean.class));
    }

    public List<ProfileSegmenterBean> getAllSegmenter(int var1) throws SQLException {
        String var2 = SqlUtil.getSelectAllQuery("profile_segmentation", var1);
        return this.query(var2, SqlUtil.createListResultHandler(ProfileSegmenterBean.class));
    }
}
