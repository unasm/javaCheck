package com.sensor.db;

import com.mchange.v2.c3p0.AbstractComboPooledDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import com.sensor.common.config.MetaConfigInfo;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by tianyi on 02/08/2017.
 */
public class MetaConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(MetaConnectionPool.class);

    private static AbstractComboPooledDataSource dataSource;

    public MetaConnectionPool() {
    }

    public static DataSource getDataSource() {
        return dataSource;
    }


    public static void init() throws Exception {
        init(1, 1, 10);
    }

    public static void init(int initPoolSize, int minPoolSize, int maxPoolSize) throws Exception {
        if(dataSource == null) {
            dataSource = new MHADataSource();
        }

        setDataSourceParams(initPoolSize, minPoolSize, maxPoolSize);
    }



    public static void destroy() {
        try {
            DataSources.destroy(dataSource);
            dataSource = null;
        } catch (SQLException ex) {
            logger.warn("fail to destroy data source", ex);
        }

    }


    public static void initWithConfig(MetaConfigInfo var0) {
        initWithConfig(var0, 1, 1, 10);
    }

    public static void initWithConfig(MetaConfigInfo var0, int var1, int var2, int var3) {
        if(dataSource != null) {
            destroy();
        }

        dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl((String)var0.getJdbcUrlList().get(var0.getMasterIndex()));
        dataSource.setUser(var0.getUser());
        dataSource.setPassword(var0.getPassword());
        setDataSourceParams(var1, var2, var3);
    }

    private static void setDataSourceParams(int var0, int var1, int var2) {
        dataSource.setInitialPoolSize(var0);
        dataSource.setMinPoolSize(var1);
        dataSource.setMaxPoolSize(var2);
        dataSource.setAcquireIncrement(1);
        dataSource.setMaxIdleTime(600);
        dataSource.setAcquireRetryAttempts(3);
        dataSource.setCheckoutTimeout(5000);
        dataSource.setPreferredTestQuery("SELECT 1");
        dataSource.setIdleConnectionTestPeriod(60);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static String stackAndTime(String var0) {
        if(null == var0) {
            var0 = "";
        }

        StackTraceElement[] var1 = Thread.currentThread().getStackTrace();
        return "date=" + (new DateTime()).toString() + "\n" + var0 + "\n" + "\nstacktrace=\n" + stackTraceToString(var1);
    }

    private static String stackTraceToString(StackTraceElement[] var0) {
        StringWriter var1 = new StringWriter();
        printStackTrace(var0, new PrintWriter(var1));
        return var1.toString();
    }

    private static void printStackTrace(StackTraceElement[] var0, PrintWriter var1) {
        StackTraceElement[] var2 = var0;
        int var3 = var0.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            StackTraceElement var5 = var2[var4];
            var1.println(var5);
        }

    }


    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            //Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
