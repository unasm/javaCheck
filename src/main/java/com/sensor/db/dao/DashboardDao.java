package com.sensor.db.dao;

import com.sensor.common.utils.SqlUtil;
import com.sensor.db.MetaConnectionPool;
import com.sensor.db.bean.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tianyi on 06/09/2017.
 */
public class DashboardDao extends AbstractDao {
    private static final Logger logger = LoggerFactory.getLogger(DashboardDao.class);
    public static final String TABLE_NAME = "dashboard";
    public static final String ITEM_TABLE_NAME = "dashboard_item";
    public static final String USER_TABLE_NAME = "dashboard_user";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_DASHBOARD_ID = "dashboard_id";
    public static final String COLUMN_BOOKMARK_ID = "bookmark_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CREATE_TIME = "create_time";
    public static final String COLUMN_ITEM_CONFIG = "config";
    public static final String COLUMN_SHOW_ORDER = "show_order";
    public static final String COLUMN_IS_DEFAULT = "is_default";
    public static final String COLUMN_IS_PUBLIC = "is_public";
    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_CONFIG = "config";

    public DashboardDao() {
    }

    public List<DashboardBean> getAllDashboards() throws SQLException {
        String var1 = SqlUtil.getSelectAllQuery("dashboard");
        return (List)this.query(var1, SqlUtil.createListResultHandler(DashboardBean.class));
    }

    public List<DashboardUserBean> getAllDashboardsUser() throws SQLException {
        String var1 = SqlUtil.getSelectAllQuery("dashboard_user");
        return (List)this.query(var1, SqlUtil.createListResultHandler(DashboardUserBean.class));
    }

    public List<DashboardItemBean> getAllDashboardItem() throws SQLException {
        String var1 = SqlUtil.getSelectAllQuery("dashboard_item");
        return (List)this.query(var1, SqlUtil.createListResultHandler(DashboardItemBean.class));
    }

    public DashboardBean getDashboard(int var1) throws SQLException {
        BookmarkDao var2 = new BookmarkDao();
        String var3 = SqlUtil.getSelectQuery("dashboard", "AND", new String[]{"id"});
        ResultSetHandler var4 = SqlUtil.createResultHandler(DashboardBean.class);
        DashboardBean var5 = (DashboardBean)this.query(var3, var4, new Object[]{Integer.valueOf(var1)});
        if(var5 == null) {
            return null;
        } else {
            var3 = SqlUtil.getSelectQuery("dashboard_item", "AND", new String[]{"dashboard_id"});
            var3 = var3 + " ORDER BY show_order ASC";
            ResultSetHandler var6 = SqlUtil.createListResultHandler(DashboardItemBean.class);
            List var7 = (List)this.query(var3, var6, new Object[]{Integer.valueOf(var1)});
            Iterator var8 = var7.iterator();

            while(var8.hasNext()) {
                DashboardItemBean var9 = (DashboardItemBean)var8.next();
                BookmarkBean var10 = var2.getBookmarkById(var9.getBookmarkId());
                if(var10 != null) {
                    var9.setBookmark(var10);
                }
            }

            var5.setItems(var7);
            return var5;
        }
    }

    public Integer addDashboard(DashboardBean var1) throws SQLException {
        Connection var2 = null;

        Integer var15;
        try {
            QueryRunner var3 = new QueryRunner();
            var2 = MetaConnectionPool.getConnection();
            var2.setAutoCommit(false);
            String var4 = SqlUtil.getInsertQuery("dashboard", new String[]{"user_id", "name", "create_time", "is_default", "project_id", "config"});
            Long var5 = (Long)var3.insert(var2, var4, new ScalarHandler(), new Object[]{var1.getUserId(), var1.getName(), new Date(System.currentTimeMillis()), var1.getIsDefault(), var1.getProjectId(), var1.getConfig()});
            if(!CollectionUtils.isEmpty(var1.getItems())) {
                int var6 = 0;
                Iterator var7 = var1.getItems().iterator();

                while(var7.hasNext()) {
                    DashboardItemBean var8 = (DashboardItemBean)var7.next();
                    var4 = SqlUtil.getInsertQuery("dashboard_item", new String[]{"dashboard_id", "bookmark_id", "config", "show_order"});
                    Object[] var10003 = new Object[]{var5, Integer.valueOf(var8.getBookmarkId()), var8.getConfig(), null};
                    ++var6;
                    var10003[3] = Integer.valueOf(var6);
                    var3.update(var2, var4, var10003);
                }
            }

            var4 = "SELECT MAX(show_order) FROM dashboard_user WHERE user_id = ?";
            Integer var14 = (Integer)var3.query(var2, var4, new ScalarHandler(), new Object[]{var1.getUserId()});
            if(var14 == null) {
                var14 = Integer.valueOf(0);
            }

            var4 = SqlUtil.getInsertQuery("dashboard_user", new String[]{"user_id", "dashboard_id", "show_order"});
            var3.update(var2, var4, new Object[]{var1.getUserId(), var5, Integer.valueOf(var14.intValue() + 1)});
            var2.commit();
            var15 = Integer.valueOf(var5.intValue());
        } catch (SQLException var12) {
            if(var2 != null) {
                var2.rollback();
            }

            logger.warn("update dashboard with exception", var12);
            throw var12;
        } finally {
            if(var2 != null) {
                var2.setAutoCommit(true);
                DbUtils.closeQuietly(var2);
            }

        }

        return var15;
    }

    public void updateDashboard(DashboardBean var1) throws SQLException {
        Connection var2 = null;

        try {
            QueryRunner var3 = new QueryRunner();
            var2 = MetaConnectionPool.getConnection();
            var2.setAutoCommit(false);
            String var4 = SqlUtil.getUpdateByIdQuery("dashboard", "id", new String[]{"user_id", "name", "is_public", "config"});
            var3.update(var2, var4, new Object[]{var1.getUserId(), var1.getName(), var1.getIsPublic(), var1.getConfig(), var1.getId()});
            var4 = SqlUtil.getDeleteByColumnQuery("dashboard_item", new String[]{"dashboard_id"});
            var3.update(var2, var4, var1.getId());
            if(!CollectionUtils.isEmpty(var1.getItems())) {
                int var5 = 0;
                Iterator var6 = var1.getItems().iterator();

                while(var6.hasNext()) {
                    DashboardItemBean var7 = (DashboardItemBean)var6.next();
                    var4 = SqlUtil.getInsertQuery("dashboard_item", new String[]{"dashboard_id", "bookmark_id", "config", "show_order"});
                    Object[] var10003 = new Object[]{var1.getId(), Integer.valueOf(var7.getBookmarkId()), var7.getConfig(), null};
                    ++var5;
                    var10003[3] = Integer.valueOf(var5);
                    var3.update(var2, var4, var10003);
                }
            }

            var2.commit();
        } catch (SQLException var11) {
            if(var2 != null) {
                var2.rollback();
            }

            logger.warn("update dashboard with exception", var11);
            throw var11;
        } finally {
            if(var2 != null) {
                var2.setAutoCommit(true);
                DbUtils.closeQuietly(var2);
            }

        }

    }

    public void updateDashboardsOrder(int var1, int var2, List<Integer> var3) throws SQLException {
        Connection var4 = null;

        try {
            QueryRunner var5 = new QueryRunner();
            var4 = MetaConnectionPool.getConnection();
            var4.setAutoCommit(false);
            String var6 = SqlUtil.getDeleteByColumnQuery("dashboard_user", new String[]{"user_id"});
            var5.update(var4, var6, Integer.valueOf(var1));
            int var7 = 0;
            Iterator var8 = var3.iterator();

            while(var8.hasNext()) {
                Integer var9 = (Integer)var8.next();
                var6 = SqlUtil.getInsertQuery("dashboard_user", new String[]{"user_id", "dashboard_id", "show_order"});
                Object[] var10003 = new Object[]{Integer.valueOf(var1), var9, null};
                ++var7;
                var10003[2] = var7;
                var5.update(var4, var6, var10003);
            }

            var4.commit();
        } catch (SQLException var13) {
            if(var4 != null) {
                var4.rollback();
            }

            logger.warn("update dashboard with exception", var13);
            throw var13;
        } finally {
            if(var4 != null) {
                var4.setAutoCommit(true);
                DbUtils.closeQuietly(var4);
            }

        }
    }

    public void deleteDashboard(int var1) throws SQLException, ClassNotFoundException {
        Connection var2 = null;

        try {
            QueryRunner var3 = new QueryRunner();
            var2 = MetaConnectionPool.getConnection();
            var2.setAutoCommit(false);
            String sql = SqlUtil.getDeleteByColumnQuery("dashboard", "id");
            int var5 = var3.update(var2, sql, var1);
            if(var5 == 1) {
                sql = SqlUtil.getDeleteByColumnQuery("dashboard_user", "dashboard_id");
                var3.update(var2, sql, var1);
                sql = SqlUtil.getDeleteByColumnQuery("dashboard_item", "dashboard_id");
                var3.update(var2, sql, var1);
            }

            var2.commit();
        } catch (SQLException var9) {
            if(var2 != null) {
                var2.rollback();
            }

            logger.warn("update dashboard with exception", var9);
            throw var9;
        } finally {
            if(var2 != null) {
                var2.setAutoCommit(true);
                DbUtils.closeQuietly(var2);
            }

        }

    }

    public List<DashboardBean> getUsersOwnDashboardsByProjectId(int var1, int var2) throws SQLException {
        String var3 = String.format("SELECT * FROM %s WHERE %s = ? AND %s = ?", new Object[]{"dashboard", "user_id", "project_id"});
        return (List)this.query(var3, SqlUtil.createListResultHandler(DashboardBean.class), new Object[]{Integer.valueOf(var1), Integer.valueOf(var2)});
    }

    public List<Integer> getUsersOwnDashboardsIdsByProjectId(int var1, int var2) throws SQLException {
        String var3 = String.format("SELECT %s FROM %s WHERE (%s = ? OR is_public != 0) AND %s = ?", new Object[]{"id", "dashboard", "user_id", "project_id"});
        HashSet var4 = new HashSet();
        var4.addAll((Collection)this.query(var3, new Object[]{Integer.valueOf(var1), Integer.valueOf(var2)}).stream().map((var0) -> {
            return (Integer)var0.get("id");
        }).collect(Collectors.toList()));
        var3 = "SELECT dashboard_id FROM dashboard_user a INNER JOIN dashboard b ON a.dashboard_id = b.id WHERE a.user_id = ? AND b.project_id = ?";
        var4.addAll((Collection)this.query(var3, new Object[]{Integer.valueOf(var1), Integer.valueOf(var2)}).stream().map((var0) -> {
            return (Integer)var0.get("dashboard_id");
        }).collect(Collectors.toList()));
        return new ArrayList(var4);
    }

    public List<UserBean> getDashboardUsers(int var1) throws SQLException {
        List var2 = this.query("SELECT dashboard_user.user_id, user.username FROM dashboard_user INNER JOIN user ON dashboard_user.user_id = user.id WHERE dashboard_id = ?", new Object[]{Integer.valueOf(var1)});
        ArrayList var3 = new ArrayList();
        Iterator var4 = var2.iterator();

        while(var4.hasNext()) {
            Map var5 = (Map)var4.next();
            UserBean var6 = new UserBean();
            var6.setId(((Integer)var5.get("user_id")).intValue());
            var6.setUsername((String)var5.get("username"));
            var3.add(var6);
        }

        return var3;
    }

    public void shareDashboardToUser(int var1, int var2) throws SQLException {
        this.update("INSERT IGNORE INTO `dashboard_user` (`user_id`, `dashboard_id`, `show_order`) SELECT ?, ?, MAX(show_order) + 1 FROM dashboard_user WHERE user_id = ?", new Object[]{Integer.valueOf(var2), Integer.valueOf(var1), Integer.valueOf(var2)});
    }

    public void deleteUserFromDashboardShare(int var1, int var2) throws SQLException {
        this.update("DELETE FROM dashboard_user WHERE dashboard_id = ? AND user_id = ?", new Object[]{Integer.valueOf(var1), Integer.valueOf(var2)});
    }

    public void deleteDashboardUsers(int var1, int var2) throws SQLException {
        this.update("DELETE FROM dashboard_user WHERE dashboard_user.dashboard_id = ? AND dashboard_user.user_id != ?", new Object[]{Integer.valueOf(var1), Integer.valueOf(var2)});
    }
}
