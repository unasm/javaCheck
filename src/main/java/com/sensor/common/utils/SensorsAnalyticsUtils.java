package com.sensor.common.utils;

import com.sensor.common.DateFormat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

/**
 * Created by tianyi on 18/08/2017.
 */

public class SensorsAnalyticsUtils {
    private static final Logger logger = LoggerFactory.getLogger(SensorsAnalyticsUtils.class);
    private static String saHome = null;
    private static Properties properties = null;
    private static String saBuildVersion = null;
    private static String saBuildTime = null;

    public SensorsAnalyticsUtils() {
    }

    public static String getSAHome() {
        if(saHome == null) {
            saHome = (String)System.getenv().get("SENSORS_ANALYTICS_HOME");
            if (saHome == null) {
                saHome = "/Users/tianyi/project/javaweb2";
            }
        }

        return saHome;
    }

    public static void setSaHome(String home) {
        saHome = home;
    }

    /**
     * 获取 神策的 配置 属性
     *
     * @return
     * @throws IOException
     */
    public static synchronized Properties getSAProperties() throws IOException {
        if (properties == null) {
            String saHome = getSAHome();
            Path path = Paths.get(saHome, "conf", "sensors_analytics.property");
            //Path var1 = Paths.get(var0, new String[]{"conf", "sensors_analytics.property"});
            File file = new File(path.toString());
            properties = new Properties();
            FileInputStream inputStream = new FileInputStream(file);
            Throwable exception = null;

            try {
                properties.load(inputStream);
            } catch (Throwable ex) {
                exception = ex;
                throw ex;
            } finally {
                if (inputStream != null) {
                    if(exception != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable ex) {
                            exception.addSuppressed(ex);
                        }
                    } else {
                        inputStream.close();
                    }
                }

            }
        }

        return properties;
    }

    public static synchronized void setProperties(Properties prop) {
        properties = prop;
    }

    private static synchronized void initBuildInfo() {
        if(saBuildVersion == null) {
            InputStream var0;
            Throwable var1;
            Properties var2;
            String var3;
            try {
                var0 = ClassLoader.getSystemResourceAsStream("build_info.properties");
                var1 = null;

                try {
                    var2 = new Properties();
                    var2.load(var0);
                    var3 = var2.getProperty("build.version");
                    if(!StringUtils.isEmpty(var3)) {
                        saBuildVersion = var3;
                    }

                    String var4 = var2.getProperty("build.time");
                    if(!StringUtils.isEmpty(var4)) {
                        Date var5 = new Date(Long.parseLong(var4) * 1000L);
                        saBuildTime = DateFormat.DEFAULT_DATETIME_FORMAT.format(var5);
                    }
                } catch (Throwable var32) {
                    var1 = var32;
                    throw var32;
                } finally {
                    if(var0 != null) {
                        if(var1 != null) {
                            try {
                                var0.close();
                            } catch (Throwable var31) {
                                var1.addSuppressed(var31);
                            }
                        } else {
                            var0.close();
                        }
                    }

                }
            } catch (IOException var36) {
                ;
            }

            if(StringUtils.isEmpty(saBuildTime)) {
                try {
                    var0 = ClassLoader.getSystemResourceAsStream("git.properties");
                    var1 = null;

                    try {
                        var2 = new Properties();
                        var2.load(var0);
                        var3 = var2.getProperty("git.build.time");
                        if(!StringUtils.isEmpty(var3)) {
                            saBuildTime = var3;
                        }
                    } catch (Throwable var30) {
                        var1 = var30;
                        throw var30;
                    } finally {
                        if(var0 != null) {
                            if(var1 != null) {
                                try {
                                    var0.close();
                                } catch (Throwable var29) {
                                    var1.addSuppressed(var29);
                                }
                            } else {
                                var0.close();
                            }
                        }

                    }
                } catch (IOException var34) {
                    ;
                }

                if(StringUtils.isEmpty(saBuildTime)) {
                    saBuildTime = "";
                }
            }

            if(StringUtils.isEmpty(saBuildVersion)) {
                saBuildVersion = "1.6.5";
            }
        }

    }

    public static String getSensorsAnalyticsBuildVersion() {
        initBuildInfo();
        return saBuildVersion;
    }

    public static String getSensorsAnalyticsBuildTime() {
        initBuildInfo();
        return saBuildTime;
    }
}
