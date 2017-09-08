package com.sensor.db;

import com.mchange.v2.c3p0.AbstractComboPooledDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import com.sensor.common.client.ZookeeperClient;
import com.sensor.common.config.ImpalaConfigInfo;
import com.sensor.common.config.OLAPEngineConfigInfo;
import com.sensor.common.config.VerticaConfigInfo;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * 初始化 OLAP 引擎的链接
 * Created by tianyi on 14/08/2017.
 */
public class OLAPEngineConnectionPool {
    public static final int DEFAULT_CHECKOUT_TIMEOUT = 30000;
    public static final int DEFAULT_ACQUIRE_RETRY_ATTEMPTS = 3;
    public static final int DEFAULT_TEST_PERIOD = 300;
    private static final Logger logger = LoggerFactory.getLogger(OLAPEngineConnectionPool.class);
    private static final int DEFAULT_MIN_POOL_SIZE = 1;
    private static final int DEFAULT_MAX_POOL_SIZE = 10;
    private static final int DEFAULT_INIT_POOL_SIZE = 1;
    private static final int DEFAULT_ACQUIRE_INCREMENT = 1;
    private static final int DEFAULT_MAX_IDLE_TIME = 600;
    private static List<AbstractComboPooledDataSource> dataSourceList;
    private static OLAPEngineType dbType;

    public OLAPEngineConnectionPool() {
    }
    private static OLAPEngineConfigInfo configInfo;

    private static synchronized OLAPEngineConfigInfo getOLAPEngineConfigFromZookeeper() throws Exception {
        ZookeeperClient client = null;

        OLAPEngineConfigInfo config;
        try {
            client = new ZookeeperClient();
            config = client.getOLAPEngineConfigInfo();
        } catch (Exception var10) {
            throw new Exception("fail to get Vertica config.", var10);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception var9) {
                    logger.warn("fail to close zk client", var9);
                }
            }

        }

        return config;
    }

    public static void init(int init, int min, int max) throws Exception {
        initWithConfig(getOLAPEngineConfigFromZookeeper(), init, min, max);
    }

    public static void init() throws Exception {
        init(DEFAULT_INIT_POOL_SIZE, DEFAULT_MIN_POOL_SIZE, DEFAULT_MAX_POOL_SIZE);
    }

    public static void initWithConfig(OLAPEngineConfigInfo config) throws Exception {
        initWithConfig(config, DEFAULT_INIT_POOL_SIZE, DEFAULT_MIN_POOL_SIZE, DEFAULT_MAX_POOL_SIZE);
    }

    public static void initWithConfig(OLAPEngineConfigInfo config, int initPoolSize, int minPoolSize, int maxPoolSize) throws Exception {
        if(dataSourceList != null) {
            destroy();
        }

        dataSourceList = new ArrayList<>();
        if (config.getEngineType() == OLAPEngineType.VERTICA) {
            VerticaConfigInfo verticaConfigInfo = config.getVerticaConfigInfo();
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            dataSource.setJdbcUrl(verticaConfigInfo.getJdbcUrl());
            Properties properties = new Properties();
            properties.put("User", verticaConfigInfo.getUser());
            properties.put("Password", verticaConfigInfo.getPassword());
            properties.put("ConnectionLoadBalance", "1");
            properties.put("BackupServerNode", verticaConfigInfo.getJdbcBackupUrl());
            properties.put("Label", "SensorsAnalytics");
            dataSource.setProperties(properties);
            maxPoolSize = verticaConfigInfo.getMaxPoolSize();
            dataSourceList.add(dataSource);
        } else {
            ImpalaConfigInfo impalaConfigInfo = config.getImpalaConfigInfo();
            for (String jdbcUrl :impalaConfigInfo.getJdbcUrlList()) {
                ComboPooledDataSource dataSource = new ComboPooledDataSource();
                dataSource.setJdbcUrl(jdbcUrl);
                dataSource.setUser(impalaConfigInfo.getUser());
                dataSource.setPassword(impalaConfigInfo.getPassword());
                dataSourceList.add(dataSource);
            }

            maxPoolSize = impalaConfigInfo.getMaxPoolSize();
        }

        dbType = config.getEngineType();
        configInfo = config;
        setDataSourceParams(initPoolSize, minPoolSize, maxPoolSize);
    }

    private static void setDataSourceParams(int initPoolSize, int minPoolSize, int maxPoolSize) {
        AbstractComboPooledDataSource dataSource;
        for (Iterator var3 = dataSourceList.iterator(); var3.hasNext(); dataSource.setIdleConnectionTestPeriod(DEFAULT_TEST_PERIOD)) {
            dataSource = (AbstractComboPooledDataSource)var3.next();
            dataSource.setInitialPoolSize(initPoolSize);
            dataSource.setMinPoolSize(minPoolSize);
            dataSource.setMaxPoolSize(maxPoolSize);
            dataSource.setAcquireIncrement(DEFAULT_ACQUIRE_INCREMENT);
            dataSource.setMaxIdleTime(DEFAULT_MAX_IDLE_TIME);
            dataSource.setAcquireRetryAttempts(DEFAULT_ACQUIRE_RETRY_ATTEMPTS);
            dataSource.setCheckoutTimeout(DEFAULT_CHECKOUT_TIMEOUT);
            if (getEngineType() == OLAPEngineType.IMPALA) {
                dataSource.setPreferredTestQuery(null);
                dataSource.setTestConnectionOnCheckin(false);
                dataSource.setTestConnectionOnCheckout(false);
            } else {
                dataSource.setPreferredTestQuery("SELECT 1");
            }
        }

    }

    public static DataSource getDataSource() {
        if (dataSourceList == null) {
            try {
                init();
            } catch (Exception ex) {
                System.out.println("init_OLAPEngineConnectionPool failed");
                logger.warn("init_OLAPEngineConnectionPool failed {}", ex);
            }
        }
        return dataSourceList != null ? dataSourceList.get(RandomUtils.nextInt(0, dataSourceList.size())) : null;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static void destroy() {
        if (dataSourceList != null) {
            try {
                for (AbstractComboPooledDataSource dataSource: dataSourceList) {
                    DataSources.destroy(dataSource);
                }
                dataSourceList = null;
            } catch (SQLException ex) {
                logger.warn("fail to destroy data source {}", ex);
            }

        }
    }

    public static OLAPEngineType getEngineType() {
        return dbType;
    }

    public static OLAPEngineConfigInfo getConfigInfo() {
        return configInfo;
    }

    static {
        dbType = OLAPEngineType.IMPALA;

        try {
            System.out.println("loaded_vector_driver");
            Class.forName("com.vertica.jdbc.Driver");
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            logger.warn("loaded_vector_driver_exception {}", ex);
        }

    }
    /*
    class test extends Driver {

    }
    */
}
