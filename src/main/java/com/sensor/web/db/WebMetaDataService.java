package com.sensor.web.db;

import com.sensor.db.bean.DashboardBean;
import com.sensor.db.dao.DashboardDao;
import com.sensor.service.MetaDataService;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by tianyi on 26/08/2017.
 */
public class WebMetaDataService  extends MetaDataService {
    private static WebMetaDataService instance;
    private static DashboardDao dashboardDao = new DashboardDao();
    private volatile Map<Integer, DashboardBean> dashboardIdMap;


    private WebMetaDataService() {
    }

    public static synchronized WebMetaDataService getInstance() {
        if(instance == null) {
            instance = new WebMetaDataService();
            MetaDataService.setMetaDataService(instance);
        }

        return instance;
    }

    public DashboardBean getDashboard(int dashId) throws SQLException {
        DashboardBean dash = this.dashboardIdMap.get(dashId);
        if (dash == null) {
            dash = dashboardDao.getDashboard(dashId);
            if (dash != null) {
                this.dashboardIdMap.put(dashId, dash);
            }
        }

        return dash;
    }
}
