package com.sensor.common;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by tianyi on 17/08/2017.
 */
public class DbUtils {
    public DbUtils() {
    }

    public static void close(Connection conn) throws SQLException {
        if(conn != null) {
            conn.close();
        }

    }

    public static void close(ResultSet rs) throws SQLException {
        if(rs != null) {
            rs.close();
        }

    }

    public static void close(Statement stmt) throws SQLException {
        if(stmt != null) {
            stmt.close();
        }

    }

    public static void closeQuietly(Connection conn) {
        try {
            close(conn);
        } catch (SQLException var2) {
            ;
        }

    }

    /**
     * 结束请求
     * @param conn  请求的网络连接
     * @param stmt  请求的statement
     * @param rs    请求的结果数据
     */
    public static void closeQuietly(Connection conn, Statement stmt, ResultSet rs) {
        try {
            closeQuietly(rs);
        } finally {
            try {
                closeQuietly(stmt);
            } finally {
                closeQuietly(conn);
            }
        }

    }

    private static void closeQuietly(ResultSet rs) {
        try {
            close(rs);
        } catch (SQLException var2) {
            ;
        }

    }

    public static void closeQuietly(Statement stmt) {
        try {
            close(stmt);
        } catch (SQLException var2) {
            ;
        }

    }

    public static void commitAndClose(Connection conn) throws SQLException {
        if(conn != null) {
            try {
                conn.commit();
            } finally {
                conn.close();
            }
        }

    }

    public static void commitAndCloseQuietly(Connection conn) {
        try {
            commitAndClose(conn);
        } catch (SQLException var2) {
            ;
        }

    }

    public static boolean loadDriver(String driverClassName) {
        return loadDriver(DbUtils.class.getClassLoader(), driverClassName);
    }

    public static boolean loadDriver(ClassLoader classLoader, String driverClassName) {
        try {
            Class e = classLoader.loadClass(driverClassName);
            if(!Driver.class.isAssignableFrom(e)) {
                return false;
            } else {
                Constructor driverConstructor = e.getConstructor(new Class[0]);
                boolean isConstructorAccessible = driverConstructor.isAccessible();
                if(!isConstructorAccessible) {
                    driverConstructor.setAccessible(true);
                }

                try {
                    Driver driver = (Driver)driverConstructor.newInstance(new Object[0]);
                    DriverManager.registerDriver(new DbUtils.DriverProxy(driver));
                } finally {
                    driverConstructor.setAccessible(isConstructorAccessible);
                }

                return true;
            }
        } catch (RuntimeException var12) {
            return false;
        } catch (Exception var13) {
            return false;
        }
    }

    public static void printStackTrace(SQLException e) {
        printStackTrace(e, new PrintWriter(System.err));
    }

    public static void printStackTrace(SQLException e, PrintWriter pw) {
        SQLException next = e;

        while(next != null) {
            next.printStackTrace(pw);
            next = next.getNextException();
            if(next != null) {
                pw.println("Next SQLException:");
            }
        }

    }

    public static void printWarnings(Connection conn) {
        printWarnings(conn, new PrintWriter(System.err));
    }

    public static void printWarnings(Connection conn, PrintWriter pw) {
        if(conn != null) {
            try {
                printStackTrace(conn.getWarnings(), pw);
            } catch (SQLException ex) {
                printStackTrace(ex, pw);
            }
        }

    }

    public static void rollback(Connection conn) throws SQLException {
        if(conn != null) {
            conn.rollback();
        }

    }

    public static void rollbackAndClose(Connection conn) throws SQLException {
        if(conn != null) {
            try {
                conn.rollback();
            } finally {
                conn.close();
            }
        }

    }

    public static void rollbackAndCloseQuietly(Connection conn) {
        try {
            rollbackAndClose(conn);
        } catch (SQLException ex) {
            ;
        }

    }

    private static final class DriverProxy implements Driver {
        private boolean parentLoggerSupported = true;
        private final Driver adapted;

        public DriverProxy(Driver adapted) {
            this.adapted = adapted;
        }

        public boolean acceptsURL(String url) throws SQLException {
            return this.adapted.acceptsURL(url);
        }

        public Connection connect(String url, Properties info) throws SQLException {
            return this.adapted.connect(url, info);
        }

        public int getMajorVersion() {
            return this.adapted.getMajorVersion();
        }

        public int getMinorVersion() {
            return this.adapted.getMinorVersion();
        }

        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return this.adapted.getPropertyInfo(url, info);
        }

        public boolean jdbcCompliant() {
            return this.adapted.jdbcCompliant();
        }

        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            if(this.parentLoggerSupported) {
                try {
                    Method e = this.adapted.getClass().getMethod("getParentLogger", new Class[0]);
                    return (Logger)e.invoke(this.adapted, new Object[0]);
                } catch (NoSuchMethodException var2) {
                    this.parentLoggerSupported = false;
                    throw new SQLFeatureNotSupportedException(var2);
                } catch (IllegalAccessException var3) {
                    this.parentLoggerSupported = false;
                    throw new SQLFeatureNotSupportedException(var3);
                } catch (InvocationTargetException var4) {
                    this.parentLoggerSupported = false;
                    throw new SQLFeatureNotSupportedException(var4);
                }
            } else {
                throw new SQLFeatureNotSupportedException();
            }
        }
    }
}
