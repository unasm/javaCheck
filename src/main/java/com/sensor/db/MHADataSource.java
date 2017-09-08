package com.sensor.db;

import com.mchange.v2.c3p0.AbstractComboPooledDataSource;
import com.sensor.common.client.ZookeeperClient;
import com.sensor.common.config.MetaConfigInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by tianyi on 23/08/2017.
 */
public class MHADataSource extends AbstractComboPooledDataSource {
    private static final Logger logger = LoggerFactory.getLogger(MHADataSource.class);
    private static final int MIN_UPDATE_CONFIG_INTERVAL = 10;
    private volatile Date lastUpdateConfigTime = null;

    public MHADataSource() throws Exception {
        this.updateConfig();
    }

    public Connection getConnection() throws SQLException {
        Connection var1;
        try {
            var1 = super.getConnection();
        } catch (SQLException var5) {
            logger.warn("fail to get connection.", var5);
            if(this.lastUpdateConfigTime != null && System.currentTimeMillis() - this.lastUpdateConfigTime.getTime() <= 10000L) {
                throw var5;
            }

            try {
                logger.info("try to update MySQL config");
                this.updateConfig();
            } catch (Exception var4) {
                logger.warn("fail to update", var4);
                throw var5;
            }

            logger.info("try to get new connection.");
            var1 = super.getConnection();
        }

        return var1;
    }

    private synchronized void updateConfig() throws Exception {
        this.lastUpdateConfigTime = new Date();
        ZookeeperClient zookeeperClient = null;

        MetaConfigInfo mysqlInfo;
        try {
            zookeeperClient = new ZookeeperClient();
            mysqlInfo = zookeeperClient.getMySqlInfo();
            logger.info("success_get_mysql_config. config={}", mysqlInfo);
        } catch (Exception ex) {
            throw new Exception("fail to get MySQL config.", ex);
        } finally {
            if(zookeeperClient != null) {
                try {
                    zookeeperClient.close();
                } catch (Exception ex) {
                    logger.warn("fail to close zk client", ex);
                }
            }

        }

        this.setJdbcUrl((String)mysqlInfo.getJdbcUrlList().get(mysqlInfo.getMasterIndex()));
        this.setUser(mysqlInfo.getUser());
        this.setPassword(mysqlInfo.getPassword());
    }
}
