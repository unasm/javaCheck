package com.sensor.db.dao;

import com.sensor.common.utils.SqlUtil;
import com.sensor.db.bean.EventBean;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.*;

/**
 *
 * AppEvent, $AppClick 的记录表， 事件表 ，或者说 元数据表
 *
 * Created by tianyi on 24/08/2017.
 */
public class EventDao  extends AbstractDao {
    // 数据库表 名称
    public static final String TABLE_NAME = "event_define";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CNAME = "cname";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_CREATE_TIME = "create_time";
    public static final String COLUMN_VISIBLE = "visible";
    public static final String COLUMN_VIRTUAL = "virtual";
    public static final String COLUMN_VIRTUAL_DEFINE = "virtual_define";
    public static final String COLUMN_PROJECT_ID = "project_id";

    public EventDao() {
    }

    public List<Integer> getExistsEventIds(Set<Integer> ids, Set<Integer> projects) throws SQLException {
        if(ids.isEmpty()) {
            return Collections.emptyList();
        } else {
            String sql = "SELECT id FROM event_define WHERE id IN (" + StringUtils.join(ids, ',') + ") AND " + "project_id IN (" + StringUtils.join(projects, ',') + ")";
            return this.query(sql, new ColumnListHandler<Integer>());
        }
    }

    public List<EventBean> getAllEventByUpdateTime(Date updateTime, Set<Integer> projects) throws SQLException {
        String var3 = String.format("SELECT * FROM %s WHERE update_time > ? AND project_id IN (" + StringUtils.join(projects, ',') + ")", "event_define");
        return this.query(var3, SqlUtil.createListResultHandler(EventBean.class), updateTime);
    }

    /*
    public List<EventBean> getAllEventByProjectId(int var1) throws SQLException {
        String var2 = SqlUtil.getSelectQuery("event_define", "AND", new String[]{"project_id"});
        return (List)this.query(var2, SqlUtil.createListResultHandler(EventBean.class), new Object[]{Integer.valueOf(var1)});
    }

    public List<EventBean> getAllEvent() throws SQLException {
        String var1 = SqlUtil.getSelectAllQuery("event_define");
        return (List)this.query(var1, SqlUtil.createListResultHandler(EventBean.class));
    }



    public EventBean getEvent(int var1) throws SQLException {
        String var2 = SqlUtil.getSelectQuery("event_define", "AND", new String[]{"id"});
        return (EventBean)this.query(var2, SqlUtil.createResultHandler(EventBean.class), new Object[]{Integer.valueOf(var1)});
    }

    public EventBean getEventByName(String var1, int var2) throws SQLException {
        String var3 = SqlUtil.getSelectQuery("event_define", "AND", new String[]{"name", "project_id"});
        return (EventBean)this.query(var3, SqlUtil.createResultHandler(EventBean.class), new Object[]{var1, Integer.valueOf(var2)});
    }

    public EventBean getEventByCname(String var1, int var2) throws SQLException {
        String var3 = SqlUtil.getSelectQuery("event_define", "AND", new String[]{"cname", "project_id"});
        return (EventBean)this.query(var3, SqlUtil.createResultHandler(EventBean.class), new Object[]{var1, Integer.valueOf(var2)});
    }

    public List<EventBean> getVirtualEventsByProjectId(int var1) throws SQLException {
        String var2 = String.format("SELECT * FROM %s WHERE %s != 0 AND %s = ?", new Object[]{"event_define", "virtual", "project_id"});
        return (List)this.query(var2, SqlUtil.createListResultHandler(EventBean.class), new Object[]{Integer.valueOf(var1)});
    }

    public List<EventBean> getNotVirtualEventsByProjectId(int var1) throws SQLException {
        String var2 = String.format("SELECT * FROM %s WHERE %s != 1 AND %s = ?", new Object[]{"event_define", "virtual", "project_id"});
        return (List)this.query(var2, SqlUtil.createListResultHandler(EventBean.class), new Object[]{Integer.valueOf(var1)});
    }

    public Long addEvent(EventBean var1) throws SQLException {
        String var2 = SqlUtil.getInsertQuery("event_define", new String[]{"name", "cname", "create_time", "visible", "virtual", "virtual_define", "project_id"});
        return this.insert(var2, new Object[]{var1.getName(), var1.getCname(), new Date(), Boolean.valueOf(var1.isVisible()), Boolean.valueOf(var1.isVirtual()), var1.getVirtualDefine(), Integer.valueOf(var1.getProjectId())});
    }

    public void deleteEvent(int var1) throws SQLException {
        String var2 = SqlUtil.getDeleteByColumnQuery("event_define", new String[]{"id"});
        this.update(var2, new Object[]{Integer.valueOf(var1)});
    }

    public void updateEventCname(int var1, String var2) throws SQLException {
        String var3 = SqlUtil.getUpdateByIdQuery("event_define", "id", new String[]{"cname"});
        this.update(var3, new Object[]{var2, Integer.valueOf(var1)});
    }
    */
    public void updateEventVisible(int id, int visible) throws SQLException {
        String var3 = SqlUtil.getUpdateByIdQuery("event_define", "id", "visible");
        this.update(var3, id, visible);
    }

    public void updateEvent(EventBean bean) throws SQLException {
        String sql = SqlUtil.getUpdateByIdQuery("event_define", "id", "cname", "visible", "virtual_define");
        this.update(sql, bean.getCname(), bean.isVisible(), bean.getVirtualDefine(), bean.getId());
    }

    public Long addEvent(String var1, String var2, int var3, int var4, int var5) throws SQLException {
        if (var5 < 0) {
            var5 = this.getCandidateBucketId(var3);
        }

        String var6 = "INSERT IGNORE INTO `event_define` (`name`, `cname`, `create_time`, `visible`, `virtual`, `bucket_id`, `project_id`) VALUES (?, ?, ?, ?, \'0\', ?, ?)";
        return this.insert(var6, var1, var2, new Date(), var4, var5, var3);
    }

    public int getCandidateBucketId(int projectId) throws SQLException {
        String sql = "SELECT `bucket_id`, count(*) AS cnt FROM `event_define` WHERE project_id = ? GROUP BY `bucket_id`";
        List bucketList = this.query(sql, projectId);
        long[] cntList = new long[10];
        Iterator iterator = bucketList.iterator();

        for (Object item : bucketList) {
        //while(iterator.hasNext()) {
            //Map map = (Map)iterator.next();
            Map map = (Map)item;
            Integer bucket_id = (Integer)map.get("bucket_id");
            if (bucket_id != null && bucket_id >= 0 && bucket_id < 10) {
                cntList[bucket_id] = ((Long)map.get("cnt"));
            }
        }

        int minBucket = 0;

        for(int i = 0; i < 10; ++i) {
            if(cntList[i] < cntList[minBucket]) {
                minBucket = i;
            }
        }

        return minBucket;
    }

    public Long addEvent(String var1, String var2, int var3) throws SQLException {
        int var4 = this.getCandidateBucketId(var3);
        String var5 = "INSERT IGNORE INTO `event_define` (`name`, `cname`, `create_time`, `visible`, `virtual`, `bucket_id`, `project_id`) VALUES (?, ?, ?, \'1\', \'0\', ?, ?)";
        return this.insert(var5, var1, var2, new Date(), var4, var3);
    }
}
