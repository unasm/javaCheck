package com.sensor.db.dao;

import com.sensor.common.utils.SqlUtil;
import com.sensor.db.MetaConnectionPool;
import com.sensor.db.bean.BookmarkBean;
import com.sensor.db.bean.DashboardItemBean;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 处理 bookmark 函数
 * Created by tianyi on 06/09/2017.
 */
public class BookmarkDao extends AbstractDao {
    private static final Logger logger = LoggerFactory.getLogger(BookmarkDao.class);
    private static DashboardDao dashboardDao = new DashboardDao();
    public static final String TABLE_NAME = "bookmark";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DATA = "data";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_CREATE_TIME = "create_time";
    public static final String COLUMN_RELATED_VIRTUAL_EVENT_NAMES = "related_virtual_event_names";
    public static final String COLUMN_PROJECT_ID = "project_id";

    public BookmarkDao() {
    }

    public List<BookmarkBean> getAllBookmarks() throws SQLException {
        String sql  = SqlUtil.getSelectAllQuery("bookmark");
        return this.query(sql, SqlUtil.createListResultHandler(BookmarkBean.class));
    }

    public List<BookmarkBean> getBookmarksByUserIdAndProjectId(int var1, int var2) throws SQLException {
        String sql = SqlUtil.getSelectQuery("bookmark", "AND", "user_id", "project_id");
        List<BookmarkBean> bookmarkList = this.query(sql, SqlUtil.createListResultHandler(BookmarkBean.class), var1, var2);
        if(!CollectionUtils.isEmpty(bookmarkList)) {
            ArrayList<Integer> bookIdList = new ArrayList<>();
            //Iterator var6 = bookmarkList.iterator();
            for (BookmarkBean bookmark: bookmarkList) {

            //while(var6.hasNext()) {
                //BookmarkBean var7 = (BookmarkBean)var6.next();
                bookIdList.add(bookmark.getId());
                bookmark.setDashboards(new ArrayList<>());
            }

            //从book  mark 中获得数据
            sql = "SELECT * FROM dashboard_item WHERE bookmark_id IN (" + StringUtils.join(bookIdList, ',') + ")";
            List<DashboardItemBean> itemList = this.query(sql, SqlUtil.createListResultHandler(DashboardItemBean.class));
            //Iterator var10 = var9.iterator();

            for (DashboardItemBean item: itemList) {
            //while(var10.hasNext()) {
                //DashboardItemBean var8 = (DashboardItemBean)var10.next();
                bookmarkList.stream().filter((key) -> {
                    return key.getId().equals(item.getBookmarkId());
                }).forEach((key) -> {
                    key.getDashboards().add(item);
                });
            }
        }

        return bookmarkList;
    }

    public BookmarkBean getBookmarkById(int var1) throws SQLException {
        String var2 = SqlUtil.getSelectQuery("bookmark", "AND", "id");
        BookmarkBean var3 = this.query(var2, SqlUtil.createResultHandler(BookmarkBean.class), var1);
        if (var3 == null) {
            return null;
        } else {
            var3.setDashboards(this.getDashboardsByBookmarkId(var1));
            return var3;
        }
    }

    public List<DashboardItemBean> getDashboardsByBookmarkId(int var1) throws SQLException {
        String var2 = SqlUtil.getSelectQuery("dashboard_item", "AND", "bookmark_id");
        return this.query(var2, SqlUtil.createListResultHandler(DashboardItemBean.class), var1);
    }

    public Long addBookmark(BookmarkBean var1) throws SQLException {
        String var2 = SqlUtil.getInsertQuery("bookmark", "user_id", "name", "type", "data", "time", "create_time", "related_virtual_event_names", "project_id");
        return this.insert(var2, var1.getUserId(), var1.getName(), var1.getType(), var1.getData(), var1.getTime(), new Date(System.currentTimeMillis()), var1.getRelatedVirtualEventNames(), var1.getProjectId());
    }

    public void deleteBookmark(int bookMarkId) throws SQLException {
        Connection connection = null;

        try {
            QueryRunner runner = new QueryRunner();
            connection = MetaConnectionPool.getConnection();
            connection.setAutoCommit(false);
            String sql = SqlUtil.getDeleteByColumnQuery("dashboard_item", "bookmark_id");
            runner.update(connection, sql, bookMarkId);
            sql = SqlUtil.getDeleteByColumnQuery("bookmark", "id");
            runner.update(connection, sql, bookMarkId);
            connection.commit();
        } catch (SQLException ex) {
            if(connection != null) {
                connection.rollback();
            }

            logger.warn("delete bookmark with exception", ex);
            throw ex;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                DbUtils.closeQuietly(connection);
            }

        }

    }

    public void addToDashboard(long bookmarkId, List<Integer> dashBoardList) throws SQLException {
        QueryRunner runner = new QueryRunner(MetaConnectionPool.getDataSource());
        //Iterator var5 = dashBoardList.iterator();
        for (Integer dashBoardId: dashBoardList) {
        //while(var5.hasNext()) {
            //Integer var6 = (Integer)var5.next();
            String sql = "SELECT MAX(show_order) FROM dashboard_item WHERE dashboard_id = ?";
            Integer maxShow = (Integer)runner.query(sql, new ScalarHandler(), dashBoardId);
            if (maxShow == null) {
                maxShow = 0;
            }

            sql = SqlUtil.getInsertQuery("dashboard_item", "dashboard_id", "bookmark_id", "show_order");
            this.update(sql, dashBoardId, bookmarkId, maxShow + 1);
        }

    }

    public void updateBookmark(BookmarkBean var1) throws SQLException {
        ArrayList<String> var2 = new ArrayList<>();
        ArrayList<Object> var3 = new ArrayList<>();
        this.getUpdateInfo(var1, var2, var3);
        if(!var2.contains("related_virtual_event_names")) {
            var2.add("related_virtual_event_names");
            var3.add((Object)null);
        }

        String var4 = SqlUtil.getUpdateByIdQuery("bookmark", "id", (String[])var2.toArray(new String[var2.size()]));
        var3.add(var1.getId());
        this.update(var4, var3.toArray(new Object[var3.size()]));
        List<DashboardItemBean> dashs = this.getDashboardsByBookmarkId(var1.getId());
        List var6;
        if (!CollectionUtils.isEmpty(dashs)) {
            //var6 = dashs.stream().map((key) -> DashboardItemBean::getDashboardId).collect(Collectors.toList());
            var6 = (List)dashs.stream().map(DashboardItemBean::getDashboardId).collect(Collectors.toList());
        } else {
            var6 = Collections.emptyList();
        }

        List var7 = dashboardDao.getUsersOwnDashboardsIdsByProjectId(var1.getUserId(), var1.getProjectId());
        List var8;
        if(!CollectionUtils.isEmpty(var1.getDashboards())) {
            var8 = var1.getDashboards().stream().map(DashboardItemBean::getDashboardId).collect(Collectors.toList());
            var8 = (List)CollectionUtils.intersection(var8, var7);
        } else {
            var8 = Collections.emptyList();
        }

        List<Integer> var9 = (List<Integer>)CollectionUtils.subtract(var8, var6);
        List<Integer> var10 = (List<Integer>)CollectionUtils.subtract(var6, var8);
        this.addToDashboard((long)var1.getId(), var9);
        this.removeFromDashboard(var1.getId(), var10);
    }

    public void removeFromDashboard(Integer var1, List<Integer> var2) throws SQLException {
        Iterator var3 = var2.iterator();

        while(var3.hasNext()) {
            Integer var4 = (Integer)var3.next();
            String var5 = "DELETE FROM dashboard_item WHERE bookmark_id = ? AND dashboard_id = ?";
            this.update(var5, var1, var4);
        }

    }

    public void getUpdateInfo(BookmarkBean var1, List<String> var2, List<Object> var3) throws SQLException {
        Field[] var4 = var1.getClass().getDeclaredFields();
        Field[] var5 = var4;
        int var6 = var4.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            Field var8 = var5[var7];
            int var9 = var8.getModifiers();
            if(Modifier.isPrivate(var9)) {
                try {
                    var8.setAccessible(true);
                    Object var10 = var8.get(var1);
                    String var11 = var8.getName();
                    if(!var11.equals("id") && !var11.equals("dashboards") && !var11.equals("userId") && null != var10) {
                        var2.add(this.toUnderscoreName(var11));
                        var3.add(var10);
                    }
                } catch (Exception var12) {
                    var12.printStackTrace();
                }
            }
        }

        logger.debug("columns={}, values={}", var2, var3);
    }

    private String toUnderscoreName(String var1) {
        StringBuilder var2 = new StringBuilder();

        for(int var3 = 0; var3 < var1.length(); ++var3) {
            char var4 = var1.charAt(var3);
            if(var4 >= 65 && var4 <= 90) {
                var2.append('_');
                var4 = (char)(var4 + 32);
            }

            var2.append(var4);
        }

        return var2.toString();
    }

    public List<BookmarkBean> getBookmarksWithVirtualEventsByProjectId(int projectId) throws SQLException {
        String sql = SqlUtil.getSelectAllQuery("bookmark") + " WHERE related_virtual_event_names IS NOT NULL " + "AND project_id=?";
        return this.query(sql, SqlUtil.createListResultHandler(BookmarkBean.class), projectId);
    }
}
