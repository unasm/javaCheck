package com.sensor.common.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.sensor.common.Constants;
import com.sensor.common.SensorsAnalyticsException;
import com.sensor.common.config.*;
import com.sensor.common.utils.SensorsAnalyticsUtils;
import com.sensor.db.OLAPEngineType;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.framework.api.BackgroundPathAndBytesable;
import org.apache.curator.framework.api.BackgroundPathable;
import org.apache.curator.framework.api.PathAndBytesable;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by tianyi on 18/08/2017.
 */
public class ZookeeperClient implements AutoCloseable {
    public static final int ZOOKEEPER_SESSION_TIMEOUT = 20000;
    public static final int ZOOKEEPER_CONNECT_TIMEOUT = 5000;
    public static final int CURATOR_RETRY_TIMES = 3;
    public static final int CURATOR_BASE_RETRY_INTREVAL = 1000;
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);
    private CuratorFramework curatorFramework;
    protected String zookeeperRootPath;
    private Map<String, InterProcessMutex> lockMap;
    private NodeCache globalConfigCache = null;

    public static String getPath(String var0, String... var1) {
        return Paths.get(var0, var1).toString().replace("\\", "/");
    }


    public WebServerConfig getWebServerConfig() throws Exception {
        String path = getPath(this.zookeeperRootPath, "server_conf", "web");
        String data = this.getData(path);
        logger.info("getWebServerConfig {}", data);
        return Constants.DEFAULT_OBJECT_MAPPER.readValue(data, WebServerConfig.class); }

    public HiveConfigInfo getHiveInfo() throws Exception {
        String path = getPath(this.zookeeperRootPath, "client_conf/hive");
        String data = this.getData(path);
        JsonNode node = Constants.DEFAULT_OBJECT_MAPPER.readTree(data);
        return Constants.DEFAULT_OBJECT_MAPPER.readValue(node.toString(), HiveConfigInfo.class);
    }

    /**
     * 获得 olap系统的配置信息
     *
     * @return
     * @throws Exception
     */
    public OLAPEngineConfigInfo getOLAPEngineConfigInfo() throws Exception {
        String path = getPath(this.zookeeperRootPath, "client_conf/vertica");
        OLAPEngineConfigInfo config = new OLAPEngineConfigInfo();
        if (this.curatorFramework.checkExists().forPath(path) != null) {
            config.setEngineType(OLAPEngineType.VERTICA);
            config.setVerticaConfigInfo(this.getVerticaInfo());
        } else {
            config.setEngineType(OLAPEngineType.IMPALA);
            config.setImpalaConfigInfo(this.getImpalaInfo());
        }
        return config;
    }

    /**
     *
     * 读取impala信息
     *
     * @return HiveConfigInfo
     * @throws Exception
     */
    public HiveConfigInfo getImpalaInfoAsHiveConfig() throws Exception {
        String path = getPath(this.zookeeperRootPath, "client_conf/impala");
        String data = this.getData(path);
        JsonNode node = Constants.DEFAULT_OBJECT_MAPPER.readTree(data);
        return Constants.DEFAULT_OBJECT_MAPPER.readValue(node.toString(), HiveConfigInfo.class);
    }


    /**
     * 获得 impala 信息
     * @return  ImpalaConfigInfo
     * @throws Exception
     */
    public ImpalaConfigInfo getImpalaInfo() throws Exception {
        HiveConfigInfo hive = this.getImpalaInfoAsHiveConfig();
        ImpalaConfigInfo impala = new ImpalaConfigInfo();
        impala.setJdbcUrlList(hive.getHiveUrlList());
        impala.setUser(hive.getHiveUser());
        impala.setMaxPoolSize(hive.getMaxPoolSize());
        if (hive.getHivePassword() != null) {
            impala.setPassword(hive.getHivePassword());
        }

        return impala;
    }

    /**
     *  从zookeeper中获得 vertical 的配置信息
     * @return
     * @throws Exception
     */
    public VerticaConfigInfo getVerticaInfo() throws Exception {
        String path = getPath(this.zookeeperRootPath, "client_conf/vertica");
        String data = this.getData(path);
        JsonNode var3 = Constants.DEFAULT_OBJECT_MAPPER.readTree(data);
        return Constants.DEFAULT_OBJECT_MAPPER.readValue(var3.toString(), VerticaConfigInfo.class);
    }



    public ZookeeperClient() throws IOException, InterruptedException {
        Properties properties = SensorsAnalyticsUtils.getSAProperties();
        String connect = properties.getProperty("zookeeper.connect");
        this.zookeeperRootPath = properties.getProperty("zookeeper.rootPath");
        int timeout = Integer.valueOf(properties.getProperty("zookeeper.session.timeout", String.valueOf(20000)));
        int connectionTimeout = Integer.valueOf(properties.getProperty("zookeeper.connection.timeout", String.valueOf(5000)));
        int retryInterval = Integer.valueOf(properties.getProperty("zookeeper.retry.interval", String.valueOf(1000)));
        int retryTimes = Integer.valueOf(properties.getProperty("zookeeper.retry.times", String.valueOf(3)));
        this.lockMap = new HashMap<>();
        this.curatorFramework = CuratorFrameworkFactory.builder().connectString(connect).sessionTimeoutMs(timeout).
                connectionTimeoutMs(connectionTimeout).retryPolicy(new ExponentialBackoffRetry(retryInterval, retryTimes)).build();
        this.curatorFramework.start();
        Long sessionId = null;

        try {
            sessionId = this.curatorFramework.getZookeeperClient().getZooKeeper().getSessionId();
        } catch (Exception ex) {
            logger.warn("fail to get session id", ex);
        }
        logger.info("curator start end. [sessionId={}]", sessionId);
    }

    public QueryEngineServerConfig getQueryEngineServerConfig() throws Exception {
        String path = getPath(this.zookeeperRootPath, "server_conf", "query_engine");
        String data = this.getData(path);
        return Constants.DEFAULT_OBJECT_MAPPER.readValue(data, QueryEngineServerConfig.class);
    }

    public void close() throws Exception {
        if(this.globalConfigCache != null) {
            this.globalConfigCache.close();
        }

        if(this.curatorFramework != null) {
            this.curatorFramework.close();
        }

    }

    public void setData(String var1, String var2, String var3, String var4, String var5) throws Exception {
        String var6 = getPath(var1, var2);
        this.setData(var6, var3);
        String var7 = getPath(var6, var4);
        this.setData(var7, var5);
    }

    public void setData(String var1, String var2) throws Exception {
        this.setData(var1, var2, false);
    }

    public void setData(String path, String value, boolean isBackground) throws Exception {
        if(this.curatorFramework.checkExists().forPath(path) == null) {
            ((BackgroundPathAndBytesable)((ACLBackgroundPathAndBytesable)this.curatorFramework.create().
                    withMode(CreateMode.PERSISTENT)).withACL(Ids.OPEN_ACL_UNSAFE)).forPath(path, value.getBytes("UTF8"));
        } else if(isBackground) {
            ((PathAndBytesable)this.curatorFramework.setData().inBackground()).forPath(path, value.getBytes("UTF8"));
        } else {
            this.curatorFramework.setData().forPath(path, value.getBytes("UTF8"));
        }

    }

    public void deletePath(String var1) throws Exception {
        try {
            this.curatorFramework.delete().forPath(var1);
        } catch (Exception var3) {
            logger.error("delete path:" + var1 + " fail", var3);
            throw var3;
        }
    }

    public void createPath(String path) throws Exception {
        if(this.checkExists(path)) {
            logger.warn("create path existed: ", path);
        } else {
            try {
                ((BackgroundPathAndBytesable)((ACLBackgroundPathAndBytesable)this.curatorFramework.create().
                        withMode(CreateMode.PERSISTENT)).withACL(Ids.OPEN_ACL_UNSAFE)).forPath(path);
            } catch (Exception var3) {
                logger.error("create path:" + path + " fail", var3);
                throw var3;
            }
        }
    }

    public boolean checkExists(String path) throws Exception {
        try {
            Stat var2 = this.curatorFramework.checkExists().forPath(path);
            return var2 != null;
        } catch (Exception var3) {
            logger.error("check exists path: " + path + " fail", var3);
            throw var3;
        }
    }

    public void checkExists(String path, Watcher watcher) throws Exception {
        try {
            ((BackgroundPathable)this.curatorFramework.checkExists().usingWatcher(watcher)).forPath(path);
        } catch (Exception var4) {
            logger.error("check exists path: " + path + " fail", var4);
            throw var4;
        }
    }

    public List<String> lsPath(String path) throws Exception {
        return this.curatorFramework.getChildren().forPath(path);
    }

    public String getData(String configStr) throws Exception {
        return new String(this.curatorFramework.getData().forPath(configStr), "UTF8");
    }

    public MetaConfigInfo getMySqlInfo() throws Exception {
        String config = getPath(this.zookeeperRootPath, "client_conf/mysql");
        logger.debug("zk_get_mysql {} success", config);
        String dataJson = this.getData(config);

        logger.debug("zk_get_mysql_data {} success", dataJson);
        JsonNode node = Constants.DEFAULT_OBJECT_MAPPER.readTree(dataJson);
        return Constants.DEFAULT_OBJECT_MAPPER.readValue(node.toString(), MetaConfigInfo.class);
    }

    /*
    public KafkaConfigInfo getKafkaInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"client_conf/kafka"});
        String var2 = this.getData(var1);
        JsonNode var3 = Constants.DEFAULT_OBJECT_MAPPER.readTree(var2);
        return (KafkaConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(var3.toString(), KafkaConfigInfo.class);
    }

    public KuduConfigInfo getKuduInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"client_conf/kudu"});
        String var2 = this.getData(var1);
        JsonNode var3 = Constants.DEFAULT_OBJECT_MAPPER.readTree(var2);
        return (KuduConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(var3.toString(), KuduConfigInfo.class);
    }

    public HiveConfigInfo getHiveInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"client_conf/hive"});
        String var2 = this.getData(var1);
        JsonNode var3 = Constants.DEFAULT_OBJECT_MAPPER.readTree(var2);
        return (HiveConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(var3.toString(), HiveConfigInfo.class);
    }



    public VerticaConfigInfo getVerticaInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"client_conf/vertica"});
        String var2 = this.getData(var1);
        JsonNode var3 = Constants.DEFAULT_OBJECT_MAPPER.readTree(var2);
        return (VerticaConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(var3.toString(), VerticaConfigInfo.class);
    }

    public ImpalaConfigInfo getImpalaInfo() throws Exception {
        HiveConfigInfo var1 = this.getImpalaInfoAsHiveConfig();
        ImpalaConfigInfo var2 = new ImpalaConfigInfo();
        var2.setJdbcUrlList(var1.getHiveUrlList());
        var2.setUser(var1.getHiveUser());
        var2.setMaxPoolSize(var1.getMaxPoolSize());
        if(var1.getHivePassword() != null) {
            var2.setPassword(var1.getHivePassword());
        }

        return var2;
    }

    public HiveConfigInfo getImpalaInfoAsHiveConfig() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"client_conf/impala"});
        String var2 = this.getData(var1);
        JsonNode var3 = Constants.DEFAULT_OBJECT_MAPPER.readTree(var2);
        return (HiveConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(var3.toString(), HiveConfigInfo.class);
    }

    public OLAPEngineConfigInfo getOLAPEngineConfigInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"client_conf/vertica"});
        OLAPEngineConfigInfo var2 = new OLAPEngineConfigInfo();
        if(this.curatorFramework.checkExists().forPath(var1) != null) {
            var2.setEngineType(OLAPEngineType.VERTICA);
            var2.setVerticaConfigInfo(this.getVerticaInfo());
        } else {
            var2.setEngineType(OLAPEngineType.IMPALA);
            var2.setImpalaConfigInfo(this.getImpalaInfo());
        }

        return var2;
    }

    public void setQueryEngineInfo(QueryEngineConfigInfo var1) throws Exception {
        String var2 = getPath(this.zookeeperRootPath, new String[]{"client_conf", "query_engine"});
        this.setData(var2, Constants.DEFAULT_OBJECT_MAPPER.writeValueAsString(var1));
    }

    public QueryEngineServerConfig getQueryEngineServerConfig() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"server_conf", "query_engine"});
        String var2 = this.getData(var1);
        return (QueryEngineServerConfig)Constants.DEFAULT_OBJECT_MAPPER.readValue(var2, QueryEngineServerConfig.class);
    }

    public WebInfo getWebInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"client_conf", "web"});
        String var2 = this.getData(var1);
        return (WebInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(var2, WebInfo.class);
    }

    public WebServerConfig getWebServerConfig() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"server_conf", "web"});
        String var2 = this.getData(var1);
        return (WebServerConfig)Constants.DEFAULT_OBJECT_MAPPER.readValue(var2, WebServerConfig.class);
    }

    public String getServerModuleConfig(String var1) throws Exception {
        String var2 = getPath(this.zookeeperRootPath, new String[]{"server_conf", var1});
        return this.getData(var2);
    }

    public void setWebInfo(WebInfo var1) throws Exception {
        String var2 = getPath(this.zookeeperRootPath, new String[]{"client_conf", "web"});
        this.setData(var2, Constants.DEFAULT_OBJECT_MAPPER.writeValueAsString(var1));
    }

    public StreamloaderConfigInfo getStreamloaderInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"client_conf", "stream_loader"});
        String var2 = this.getData(var1);
        return (StreamloaderConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(var2, StreamloaderConfigInfo.class);
    }

    public SchedulerServerConfig getSchedulerServerConfig() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"server_conf", "scheduler"});
        String var2 = this.getData(var1);
        return (SchedulerServerConfig)Constants.DEFAULT_OBJECT_MAPPER.readValue(var2, SchedulerServerConfig.class);
    }
    */
    public RedisConfigInfo getRedisInfo() throws Exception {
        String clientPath = getPath(this.zookeeperRootPath, new String[]{"client_conf/redis"});
        String data = this.getData(clientPath);
        return (RedisConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(data, RedisConfigInfo.class);
    }
    public void registerScheduler() throws Exception {
        this.registerModule("scheduler");
    }

    public void releaseScheduler() throws Exception {
        this.releaseModule("scheduler");
    }

    public void registerModule(String var1) throws Exception {
        String var2 = var1.toLowerCase();
        String var3 = getPath(this.zookeeperRootPath, new String[]{var2});
        InterProcessMutex var4 = new InterProcessMutex(this.curatorFramework, var3);

        for(int var5 = 0; var5 != 3; ++var5) {
            if(var4.acquire(3L, TimeUnit.SECONDS)) {
                try {
                    this.lockMap.put(var3, var4);
                    logger.debug("register {} success", var2);
                    return;
                } catch (Exception var7) {
                    logger.warn("failed to register {}, ", var2, var7);
                    if(this.lockMap.containsKey(var3)) {
                        this.lockMap.remove(var3);
                    }

                    var4.release();
                }
            }
        }

        throw new SensorsAnalyticsException("failed to register " + var2);
    }

    public void releaseModule(String var1) throws Exception {
        String var2 = var1.toLowerCase();
        String var3 = getPath(this.zookeeperRootPath, new String[]{var2});
        if(this.lockMap.containsKey(var3)) {
            InterProcessMutex var4 = (InterProcessMutex)this.lockMap.get(var3);
            var4.release();
            this.lockMap.remove(var3);
            logger.debug("release {} succeed", var2);
        }

    }

    public CuratorFramework getCuratorFramework() {
        return this.curatorFramework;
    }

    public String getZookeeperRootPath() {
        return this.zookeeperRootPath;
    }

    /*
    public MonitorServerConfig getMonitorServerConfig() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"server_conf/monitor"});
        String var2 = this.getData(var1);
        return (MonitorServerConfig)Constants.DEFAULT_OBJECT_MAPPER.readValue(var2, MonitorServerConfig.class);
    }

    public void setMonitorServerConfig(MonitorServerConfig var1) throws Exception {
        String var2 = getPath(this.zookeeperRootPath, new String[]{"server_conf/monitor"});
        String var3 = Constants.DEFAULT_OBJECT_MAPPER.writeValueAsString(var1);
        this.setData(var2, var3);
    }

    public List<String> getBlackList() throws Exception {
        MonitorServerConfig var1 = this.getMonitorServerConfig();
        return var1.getBlackList();
    }

    public void updateBlackList(List<String> var1) throws Exception {
        MonitorServerConfig var2 = this.getMonitorServerConfig();
        var2.setBlackList(var1);
        this.setMonitorServerConfig(var2);
    }

    public void updateLastReportTime(String var1) throws Exception {
        MonitorServerConfig var2 = this.getMonitorServerConfig();
        var2.setLastReportTime(var1);
        this.setMonitorServerConfig(var2);
    }

    public void updatePauseMonitorTime(String var1) throws Exception {
        MonitorServerConfig var2 = this.getMonitorServerConfig();
        var2.setPauseMonitorTime(var1);
        this.setMonitorServerConfig(var2);
    }
    public void updateAlarmFailTime(String var1) throws Exception {
        MonitorServerConfig var2 = this.getMonitorServerConfig();
        var2.setLastAlarmFailTime(var1);
        this.setMonitorServerConfig(var2);
    }
    */

    public List<Triple<String, String, String>> getAllLocalRoles() throws Exception {
        ArrayList var1 = new ArrayList();
        String var2 = InetAddress.getLocalHost().getCanonicalHostName();
        String var3 = getPath(this.zookeeperRootPath, new String[]{"deploy"});
        Iterator var4 = ((List)this.curatorFramework.getChildren().forPath(var3)).iterator();

        while(var4.hasNext()) {
            String var5 = (String)var4.next();
            Iterator var6 = ((List)this.curatorFramework.getChildren().forPath(getPath(var3, new String[]{var5}))).iterator();

            while(var6.hasNext()) {
                String var7 = (String)var6.next();
                Iterator var8 = ((List)this.curatorFramework.getChildren().forPath(getPath(var3, new String[]{var5, var7}))).iterator();

                while(var8.hasNext()) {
                    String var9 = (String)var8.next();
                    if(var9.equals(var2)) {
                        var1.add(Triple.of(var5, var7, var9));
                    }
                }
            }
        }

        return var1;
    }

    public List<Triple<String, String, String>> getAllRoles() throws Exception {
        ArrayList var1 = new ArrayList();
        String var2 = getPath(this.zookeeperRootPath, new String[]{"deploy"});
        Iterator var3 = ((List)this.curatorFramework.getChildren().forPath(var2)).iterator();

        while(var3.hasNext()) {
            String var4 = (String)var3.next();
            Iterator var5 = ((List)this.curatorFramework.getChildren().forPath(getPath(var2, new String[]{var4}))).iterator();

            while(var5.hasNext()) {
                String var6 = (String)var5.next();
                Iterator var7 = ((List)this.curatorFramework.getChildren().forPath(getPath(var2, new String[]{var4, var6}))).iterator();

                while(var7.hasNext()) {
                    String var8 = (String)var7.next();
                    var1.add(Triple.of(var4, var6, var8));
                }
            }
        }

        return var1;
    }

    /*
    public BatchLoaderConfigInfo getBatchLoaderConfigInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"server_conf/batch_loader"});
        String var2 = this.getData(var1);
        return (BatchLoaderConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(var2, BatchLoaderConfigInfo.class);
    }

    public SegmenterConfigInfo getSegmenterConfigInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"server_conf/segmenter"});
        String var2 = this.getData(var1);
        return (SegmenterConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(var2, SegmenterConfigInfo.class);
    }

    public ClouderaManagerConfigInfo getClouderaManagerConfigInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"client_conf/cloudera"});
        String var2 = this.getData(var1);
        return (ClouderaManagerConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(var2, ClouderaManagerConfigInfo.class);
    }
    */
    public boolean isPathExists(String var1) throws Exception {
        return this.curatorFramework.checkExists().forPath(var1) != null;
    }

    public synchronized GlobalConfigInfo getGlobalConfigInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"global_conf"});
        if(this.globalConfigCache == null) {
            if(this.isPathExists(var1)) {
                String var2 = this.getData(var1);
                return (GlobalConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(var2, GlobalConfigInfo.class);
            } else {
                logger.warn("global conf doesn\'t exists.");
                return null;
            }
        } else {
            return (GlobalConfigInfo)Constants.DEFAULT_OBJECT_MAPPER.readValue(this.globalConfigCache.getCurrentData().getData(), GlobalConfigInfo.class);
        }
    }

    public void setGlobalConfigInfo(GlobalConfigInfo var1) throws Exception {
        String var2 = getPath(this.zookeeperRootPath, new String[]{"global_conf"});
        String var3 = Constants.DEFAULT_OBJECT_MAPPER.writeValueAsString(var1);
        this.setData(var2, var3);
    }

    public synchronized void watchGlobalConfigInfo() throws Exception {
        String var1 = getPath(this.zookeeperRootPath, new String[]{"global_conf"});
        if(this.globalConfigCache == null) {
            this.globalConfigCache = new NodeCache(this.curatorFramework, var1);
            this.globalConfigCache.start(true);
        }

    }

    public int getInternalModulePort(String var1) throws Exception {
        String var2 = getPath(this.zookeeperRootPath, new String[]{"server_conf", var1});
        JsonNode var3 = Constants.DEFAULT_OBJECT_MAPPER.readTree(this.getData(var2));
        return var3.get("port").asInt();
    }

    public List<String> getHostList() throws Exception {
        ArrayList var1 = new ArrayList();
        String var2 = getPath(this.zookeeperRootPath, new String[]{"deploy", "monitor", "monitor"});
        var1.addAll((Collection)((List)this.curatorFramework.getChildren().forPath(var2)).stream().collect(Collectors.toList()));
        return var1;
    }
}
