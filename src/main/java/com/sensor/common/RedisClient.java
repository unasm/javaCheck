package com.sensor.common;

import com.sensor.common.client.ZookeeperClient;
import com.sensor.common.config.GlobalConfigInfo;
import com.sensor.common.config.GlobalConfigInfo.InstallType;
import com.sensor.common.config.RedisConfigInfo;
import com.sensor.common.utils.Timer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.net.URI;
import java.util.*;

/**
 * Created by tianyi on 18/08/2017.
 */
public class RedisClient {
    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);
    private static final int TIMEOUT = 2000;
    private static final int JEDIS_POOL_MAX_IDLE = 5;
    private static final int JEDIS_POOL_MIN_IDLE = 1;
    private static final int JEDIS_MAX_RETRY = 3;
    private static final int JEDIS_SLEEP_WHEN_RETRY_SEC = 1;
    private static JedisPool[] jedisPools = new JedisPool[16];
    private static JedisCluster[] jedisCluster = new JedisCluster[16];
    private static boolean addShutdownHook = false;
    private static JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    private static InstallType installType = null;
    private static boolean addClusterHashTag = true;
    private int databaseIndex = 0;

    public RedisClient() {
        this.databaseIndex = 0;
    }

    public RedisClient(int databaseIndex) {
        this.databaseIndex = databaseIndex;
    }

    private Jedis getJedis() throws Exception {
        if(jedisPools[this.databaseIndex] == null) {
            Class var1 = RedisClient.class;
            synchronized(RedisClient.class) {
                if(jedisPools[this.databaseIndex] == null) {
                    init(this.databaseIndex);
                }
            }
        }

        return jedisPools[this.databaseIndex].getResource();
    }

    private JedisCluster getJedisCluster() throws Exception {
        if(jedisCluster[this.databaseIndex] == null) {
            Class var1 = RedisClient.class;
            synchronized(RedisClient.class) {
                if(jedisCluster[this.databaseIndex] == null) {
                    init(this.databaseIndex);
                }
            }
        }

        return jedisCluster[this.databaseIndex];
    }

    public static void init() throws Exception {
        init(0);
    }

    public static void init(int var0) throws Exception {
        init(null, var0);
    }

    public static void init(RedisConfigInfo config, int var1) throws Exception {
        init(null, config, var1);
    }

    /**
     * 连接 redis
     * @param type  安装的类型，集群，还是单机
     * @param configInfo
     * @param index
     * @throws Exception
     */
    public static void init(InstallType type, RedisConfigInfo configInfo, int index) throws Exception {
        int times = 3;

        while(times-- > 0) {
            try {
                if (type == null || configInfo == null) {
                    ZookeeperClient client = null;

                    try {
                        client = new ZookeeperClient();
                        if (type == null) {
                            GlobalConfigInfo config = client.getGlobalConfigInfo();
                            type = config.getInstallType();
                        }

                        if(configInfo == null) {
                            configInfo = client.getRedisInfo();
                        }
                    } finally {
                        if(client != null) {
                            client.close();
                        }

                    }
                }

                Integer configIndex = RedisConstants.getExtConfigIndex(index);
                if (configIndex != null && !CollectionUtils.isEmpty(configInfo.getExtConfigList())) {
                    configInfo = configInfo.getExtConfigList().get(configIndex);
                }
                //集群模式，单机模式
                installType = type;
                logger.info("redisClient_installType : {}, {}", installType, installType.ordinal());
                //GlobalConfigInfo.InstallType[installType.ordinal()]
                //switch(null.$SwitchMap$com$sensorsdata$analytics$common$config$GlobalConfigInfo$InstallType[installType.ordinal()]) {
                //switch(installType.ordinal()) {
                switch(installType) {
                    case CLUSTER:
                        HashSet<HostAndPort> hostList = new HashSet<>();
                        for (String config : configInfo.getRedisList()) {
                            String[] tmpConfigArr = StringUtils.split(config, ":");
                            hostList.add(new HostAndPort(tmpConfigArr[0], Integer.valueOf(tmpConfigArr[1])));
                        }

                        jedisCluster[index] = new JedisCluster(hostList, jedisPoolConfig);
                        logger.info("Connect to RedisCluster, nodes: {}", jedisCluster[index].getClusterNodes().keySet());
                        break;
                    case STANDALONE:
                        String url = configInfo.getRedisList().get(configInfo.getMasterIndex());
                        URI uriPath = URI.create("redis://" + url);
                        jedisPools[index] = new JedisPool(jedisPoolConfig, uriPath.getHost(), uriPath.getPort(), 2000, configInfo.getPassword(), index);
                        logger.info("Create JedisPool, Redis: {}", url);
                        break;
                    default:
                        throw new Exception("not supported install type.");
                }

                if (!addShutdownHook) {
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                            Timer.sleepSec(2);
                            RedisClient.closePool();
                        }
                    });
                    addShutdownHook = true;
                }

                return;
            } catch (Exception ex) {
                logger.warn("Connect to Redis with exception:", ex);
                if(times > 0) {
                    logger.info("RedisClient try to reconnect to Redis.");
                    Thread.sleep(1000L);
                }
            }
        }

        throw new Exception("Init RedisClient Failed.");
    }

    public static synchronized void closePool() {
        int i;
        for(i = 0; i < jedisPools.length; ++i) {
            if(jedisPools[i] != null) {
                jedisPools[i].destroy();
                jedisPools[i] = null;
                logger.info("RedisClient database {} closed.", i);
            }
        }

        for(i = 0; i < jedisCluster.length; ++i) {
            if(jedisCluster[i] != null) {
                try {
                    jedisCluster[i].close();
                } catch (Exception ex) {
                    logger.info("Close JedisCluster with exception " + i, ex);
                }

                jedisCluster[i] = null;
                logger.info("RedisClient cluster {} closed.", i);
            }
        }

    }

    public static void performancePoolConfig() {
        jedisPoolConfig.setTestOnBorrow(false);
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setMinEvictableIdleTimeMillis(0L);
    }

    public static void setAddClusterHashTag(boolean var0) {
        addClusterHashTag = var0;
    }

    /**
     * 获取jedis command 和jedis客户端
     * @return
     * @throws Exception
     */
    public Pair<JedisCommands, Jedis> getJedisCommandsHandler() throws Exception {
        Jedis jedis = null;
        JedisCommands commands;
        if(installType.equals(InstallType.STANDALONE)) {
            jedis = this.getJedis();
            commands = jedis;
        } else {
            commands = this.getJedisCluster();
        }

        return Pair.of(commands, jedis);
    }

    public void closeJedisCommandsHandler(Pair<JedisCommands, Jedis> pair) {
        if (pair != null && pair.getRight() != null) {
            (pair.getRight()).close();
        }

    }

    private String[] addHashTagToClusterKeys(String[] var1) {
        if(installType == InstallType.CLUSTER && addClusterHashTag) {
            String[] var2 = new String[var1.length];

            for(int var3 = 0; var3 < var1.length; ++var3) {
                var2[var3] = "{" + this.databaseIndex + "}" + var1[var3];
            }

            return var2;
        } else {
            return var1;
        }
    }

    private String addHashTagForClusterKey(String var1) {
        return installType == InstallType.CLUSTER && addClusterHashTag?"{" + this.databaseIndex + "}" + var1:var1;
    }

    public Long del(String[] var1) throws Exception {
        int var2 = 3;
        var1 = this.addHashTagToClusterKeys(var1);

        while(var2-- > 0) {
            Exception var3;
            if(installType.equals(InstallType.STANDALONE)) {
                try {
                    Jedis var4 = this.getJedis();
                    Throwable var5 = null;

                    Long var6;
                    try {
                        var6 = var4.del(var1);
                    } catch (Throwable var17) {
                        var5 = var17;
                        throw var17;
                    } finally {
                        if(var4 != null) {
                            if(var5 != null) {
                                try {
                                    var4.close();
                                } catch (Throwable var16) {
                                    var5.addSuppressed(var16);
                                }
                            } else {
                                var4.close();
                            }
                        }

                    }

                    return var6;
                } catch (Exception var19) {
                    var3 = var19;
                }
            } else {
                try {
                    return this.getJedisCluster().del(var1);
                } catch (Exception var20) {
                    var3 = var20;
                }
            }

            logger.warn("RedisClient handle Redis \'del\' failed, with exception:", var3);
            Thread.sleep(1000L);
        }

        throw new Exception("Redis \'del\' failed");
    }

    public Long del(String var1) throws Exception {
        int times = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(times-- > 0) {
                Pair<JedisCommands, Jedis> pair = this.getJedisCommandsHandler();

                Long var4;
                try {
                    var4 = (pair.getLeft()).del(var1);
                } catch (Exception var8) {
                    logger.warn("RedisClient handle Redis \'del\' failed, with exception:", var8);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(pair);
                }

                return var4;
            }

            throw new Exception("Redis \'del\' failed");
        }
    }

    public String setex(String var1, String var2, int var3) throws Exception {
        int var4 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var4-- > 0) {
                Pair var5 = this.getJedisCommandsHandler();

                String var6;
                try {
                    var6 = ((JedisCommands)var5.getLeft()).setex(var1, var3, var2);
                } catch (Exception var10) {
                    logger.warn("RedisClient handle Redis \'setex\' failed, with exception:", var10);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var5);
                }

                return var6;
            }

            throw new Exception("Redis \'setex\' failed");
        }
    }

    public long setnx(String var1, String var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                long var5;
                try {
                    var5 = ((JedisCommands)var4.getLeft()).setnx(var1, var2).longValue();
                } catch (Exception var10) {
                    logger.warn("RedisClient handle Redis \'setnx\' failed, with exception:", var10);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return var5;
            }

            throw new Exception("Redis \'setnx\' failed");
        }
    }

    public String set(String var1, String var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                String var5;
                try {
                    var5 = ((JedisCommands)var4.getLeft()).set(var1, var2);
                } catch (Exception var9) {
                    logger.warn("RedisClient handle Redis \'set\' failed, with exception:", var9);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return var5;
            }

            throw new Exception("Redis \'set\' failed");
        }
    }

    public String set(String var1, byte[] var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                String var6;
                try {
                    JedisCommands var5 = (JedisCommands)var4.getLeft();
                    if(var5 instanceof BinaryJedisClusterCommands) {
                        var6 = ((BinaryJedisClusterCommands)var5).set(var1.getBytes(), var2);
                        return var6;
                    }

                    if(!(var5 instanceof BinaryJedis)) {
                        continue;
                    }

                    var6 = ((BinaryJedis)var5).set(var1.getBytes(), var2);
                } catch (Exception var10) {
                    logger.warn("RedisClient handle Redis \'set\' failed, with exception:", var10);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return var6;
            }

            throw new Exception("Redis \'set\' failed");
        }
    }

    public Boolean getBit(String var1, long var2) throws Exception {
        int var4 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var4-- > 0) {
                Pair var5 = this.getJedisCommandsHandler();

                Boolean var6;
                try {
                    var6 = ((JedisCommands)var5.getLeft()).getbit(var1, var2);
                } catch (Exception var10) {
                    logger.warn("RedisClient handle Redis \'set\' failed, with exception:", var10);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var5);
                }

                return var6;
            }

            throw new Exception("Redis \'set\' failed");
        }
    }

    public String get(String var1) throws Exception {
        int var2 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var2-- > 0) {
                Pair var3 = this.getJedisCommandsHandler();

                String var4;
                try {
                    var4 = ((JedisCommands)var3.getLeft()).get(var1);
                } catch (Exception var8) {
                    logger.warn("RedisClient handle Redis \'get\' failed, with exception:", var8);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var3);
                }

                return var4;
            }

            throw new Exception("RedisClient \'get\' failed");
        }
    }

    public List<String> mget(String[] var1) throws Exception {
        int var2 = 3;
        var1 = this.addHashTagToClusterKeys(var1);

        while(var2-- > 0) {
            Exception var3;
            if(installType.equals(GlobalConfigInfo.InstallType.STANDALONE)) {
                try {
                    Jedis var4 = this.getJedis();
                    Throwable var5 = null;

                    List var6;
                    try {
                        var6 = var4.mget(var1);
                    } catch (Throwable var17) {
                        var5 = var17;
                        throw var17;
                    } finally {
                        if(var4 != null) {
                            if(var5 != null) {
                                try {
                                    var4.close();
                                } catch (Throwable var16) {
                                    var5.addSuppressed(var16);
                                }
                            } else {
                                var4.close();
                            }
                        }

                    }

                    return var6;
                } catch (Exception var19) {
                    var3 = var19;
                }
            } else {
                try {
                    return this.getJedisCluster().mget(var1);
                } catch (Exception var20) {
                    var3 = var20;
                }
            }

            logger.warn("RedisClient handle Redis \'mget\' failed, with exception:", var3);
            Thread.sleep(1000L);
        }

        throw new Exception("Redis \'mget\' failed");
    }

    public Long msetnx(String[] var1) throws Exception {
        int var2 = 3;
        if(var1.length % 2 != 0) {
            throw new Exception("msetnx fields length must be even");
        } else {
            if(installType == InstallType.CLUSTER) {
                String[] var4 = new String[var1.length];

                for(int var5 = 0; var5 < var1.length; ++var5) {
                    if(var5 % 2 == 0) {
                        var4[var5] = this.addHashTagForClusterKey(var1[var5]);
                    } else {
                        var4[var5] = var1[var5];
                    }
                }

                var1 = var4;
            }

            while(var2-- > 0) {
                Exception var3;
                if(installType.equals(GlobalConfigInfo.InstallType.STANDALONE)) {
                    try {
                        Jedis var21 = this.getJedis();
                        Throwable var22 = null;

                        Long var6;
                        try {
                            var6 = var21.msetnx(var1);
                        } catch (Throwable var17) {
                            var22 = var17;
                            throw var17;
                        } finally {
                            if(var21 != null) {
                                if(var22 != null) {
                                    try {
                                        var21.close();
                                    } catch (Throwable var16) {
                                        var22.addSuppressed(var16);
                                    }
                                } else {
                                    var21.close();
                                }
                            }

                        }

                        return var6;
                    } catch (Exception var19) {
                        var3 = var19;
                    }
                } else {
                    try {
                        return this.getJedisCluster().msetnx(var1);
                    } catch (Exception var20) {
                        var3 = var20;
                    }
                }

                logger.warn("RedisClient handle Redis \'msetnx\' failed, with exception:", var3);
                Thread.sleep(1000L);
            }

            throw new Exception("Redis \'msetnx\' failed");
        }
    }

    public String getAndTouch(String var1, int var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                String var13;
                try {
                    String var5 = ((JedisCommands)var4.getLeft()).get(var1);
                    if(var5 != null) {
                        long var6 = ((JedisCommands)var4.getLeft()).ttl(var1).longValue();
                        if(var6 < (long)var2) {
                            ((JedisCommands)var4.getLeft()).expire(var1, var2);
                        }
                    }

                    var13 = var5;
                } catch (Exception var11) {
                    logger.warn("RedisClient handle Redis \'get and touch\' failed, with exception:", var11);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return var13;
            }

            throw new Exception("RedisClient \'get and touch\' failed");
        }
    }

    public void expire(String var1, int var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                try {
                    ((JedisCommands)var4.getLeft()).expire(var1, var2);
                } catch (Exception var9) {
                    logger.warn("RedisClient handle Redis \'expire\' failed, with exception:", var9);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return;
            }

            throw new Exception("RedisClient \'expire\' failed");
        }
    }

    public boolean persist(String var1) throws Exception {
        int var2 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var2-- > 0) {
                Pair var3 = this.getJedisCommandsHandler();

                boolean var4;
                try {
                    var4 = ((JedisCommands)var3.getLeft()).persist(var1).longValue() == 1L;
                } catch (Exception var8) {
                    logger.warn("RedisClient handle Redis \'persist\' failed, with exception:", var8);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var3);
                }

                return var4;
            }

            throw new Exception("RedisClient \'persist\' failed");
        }
    }

    public boolean exists(String var1) throws Exception {
        int var2 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var2-- > 0) {
                Pair var3 = this.getJedisCommandsHandler();

                boolean var4;
                try {
                    var4 = ((JedisCommands)var3.getLeft()).exists(var1).booleanValue();
                } catch (Exception var8) {
                    logger.warn("RedisClient handle Redis \'exists\' failed, with exception:", var8);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var3);
                }

                return var4;
            }

            throw new Exception("RedisClient \'exists\' failed");
        }
    }

    public Long hset(String var1, String var2, String var3) throws Exception {
        int var4 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var4-- > 0) {
                Pair var5 = this.getJedisCommandsHandler();

                Long var6;
                try {
                    var6 = ((JedisCommands)var5.getLeft()).hset(var1, var2, var3);
                } catch (Exception var10) {
                    logger.warn("RedisClient handle Redis \'hset\' failed, with exception:", var10);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var5);
                }

                return var6;
            }

            throw new Exception("Redis \'hset\' failed");
        }
    }

    public String hmset(String var1, Map<String, String> var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                String var5;
                try {
                    var5 = ((JedisCommands)var4.getLeft()).hmset(var1, var2);
                } catch (Exception var9) {
                    logger.warn("RedisClient handle Redis \'hmset\' failed, with exception:", var9);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return var5;
            }

            throw new Exception("Redis \'hmset\' failed");
        }
    }

    public List<String> hmget(String var1, String[] var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                List<String> var5;
                try {
                    var5 = ((JedisCommands)var4.getLeft()).hmget(var1, var2);
                } catch (Exception var9) {
                    logger.warn("RedisClient handle Redis \'hmget\' failed, with exception:", var9);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return var5;
            }

            throw new Exception("Redis \'hmget\' failed");
        }
    }

    public Long hdel(String var1, String[] var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                Long var5;
                try {
                    var5 = ((JedisCommands)var4.getLeft()).hdel(var1, var2);
                } catch (Exception var9) {
                    logger.warn("RedisClient handle Redis \'hdel\' failed, with exception:", var9);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return var5;
            }

            throw new Exception("Redis \'hdel\' failed");
        }
    }

    public String hget(String var1, String var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                String var5;
                try {
                    var5 = ((JedisCommands)var4.getLeft()).hget(var1, var2);
                } catch (Exception var9) {
                    logger.warn("RedisClient handle Redis \'hget\' failed, with exception:", var9);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return var5;
            }

            throw new Exception("Redis \'hget\' failed");
        }
    }

    public Map<String, String> hgetAll(String var1) throws Exception {
        int var2 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var2-- > 0) {
                Pair var3 = this.getJedisCommandsHandler();

                Map var4;
                try {
                    var4 = ((JedisCommands)var3.getLeft()).hgetAll(var1);
                } catch (Exception var8) {
                    logger.warn("RedisClient handle Redis \'hgetAll\' failed, with exception:", var8);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var3);
                }

                return var4;
            }

            throw new Exception("Redis \'hgetAll\' failed");
        }
    }

    public String flushDB() throws Exception {
        int var1 = 3;

        while(var1-- > 0) {
            if(installType.equals(InstallType.CLUSTER)) {
                throw new Exception("redis cluster, please avoid using \'flushDB\'");
            }

            try {
                Jedis var2 = this.getJedis();
                Throwable var3 = null;

                String var4;
                try {
                    var4 = var2.flushDB();
                } catch (Throwable var13) {
                    var3 = var13;
                    throw var13;
                } finally {
                    if(var2 != null) {
                        if(var3 != null) {
                            try {
                                var2.close();
                            } catch (Throwable var14) {
                                var3.addSuppressed(var14);
                            }
                        } else {
                            var2.close();
                        }
                    }

                }

                return var4;
            } catch (Exception var16) {
                logger.warn("RedisClient handle Redis \'flushDB\' failed, with exception:", var16);
                Thread.sleep(1000L);
            }
        }

        throw new Exception("Redis \'flushDB\' failed");
    }

    public Object eval(String var1, List<String> var2, List<String> var3) throws Exception {
        int var4 = 3;

        while(var4-- > 0) {
            if(!installType.equals(InstallType.STANDALONE)) {
                throw new Exception("Not supported in redis cluster");
            }

            try {
                Jedis var6 = this.getJedis();
                Throwable var7 = null;

                Object var8;
                try {
                    var8 = var6.eval(var1, var2, var3);
                } catch (Throwable var18) {
                    var7 = var18;
                    throw var18;
                } finally {
                    if(var6 != null) {
                        if(var7 != null) {
                            try {
                                var6.close();
                            } catch (Throwable var17) {
                                var7.addSuppressed(var17);
                            }
                        } else {
                            var6.close();
                        }
                    }

                }

                return var8;
            } catch (Exception var20) {
                logger.warn("RedisClient handle Redis \'eval\' failed, script: " + var1 + ", keys: " + var2 + ", args: " + var3, var20);
                Thread.sleep(1000L);
            }
        }

        throw new Exception("Redis \'eval\' failed");
    }

    public List<String> lrange(String key, long left, long right) throws Exception {
        int var6 = 3;
        key = this.addHashTagForClusterKey(key);

        while(true) {
            if(var6-- > 0) {
                Pair handler = this.getJedisCommandsHandler();

                List<String> var8;
                try {
                    var8 = ((JedisCommands)handler.getLeft()).lrange(key, left, right);
                } catch (Exception var12) {
                    logger.warn("RedisClient handle Redis \'lrange\' failed, with exception:", var12);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(handler);
                }

                return var8;
            }

            throw new Exception("Redis \'lrange\' failed");
        }
    }

    public Long rpush(String var1, String... var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                Long var5;
                try {
                    var5 = ((JedisCommands)var4.getLeft()).rpush(var1, var2);
                } catch (Exception var9) {
                    logger.warn("RedisClient handle Redis \'rpush\' failed, with exception:", var9);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return var5;
            }

            throw new Exception("Redis \'rpush\' failed");
        }
    }

    public Long sadd(String var1, String var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                Long var5;
                try {
                    var5 = ((JedisCommands)var4.getLeft()).sadd(var1, new String[]{var2});
                } catch (Exception var9) {
                    logger.warn("RedisClient handle Redis \'sadd\' failed, with exception:", var9);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return var5;
            }

            throw new Exception("Redis \'sadd\' failed");
        }
    }

    public Set<String> smembers(String var1) throws Exception {
        int var2 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var2-- > 0) {
                Pair var3 = this.getJedisCommandsHandler();

                Set var4;
                try {
                    var4 = ((JedisCommands)var3.getLeft()).smembers(var1);
                } catch (Exception var8) {
                    logger.warn("RedisClient handle Redis \'smembers\' failed, with exception:", var8);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var3);
                }

                return var4;
            }

            throw new Exception("Redis \'smembers\' failed");
        }
    }

    public Long incr(String var1) throws Exception {
        int var2 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var2-- > 0) {
                Pair var3 = this.getJedisCommandsHandler();

                Long var4;
                try {
                    var4 = ((JedisCommands)var3.getLeft()).incr(var1);
                } catch (Exception var8) {
                    logger.warn("RedisClient handle Redis \'incr\' failed, with exception:", var8);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var3);
                }

                return var4;
            }

            throw new Exception("RedisClient \'incr\' failed");
        }
    }

    public ScanResult<Map.Entry<String, String>> hscan(String var1, int var2) throws Exception {
        int var3 = 3;
        var1 = this.addHashTagForClusterKey(var1);

        while(true) {
            if(var3-- > 0) {
                Pair var4 = this.getJedisCommandsHandler();

                ScanResult var5;
                try {
                    var5 = ((JedisCommands)var4.getLeft()).hscan(var1, var2);
                } catch (Exception var9) {
                    logger.warn("RedisClient handle Redis \'hscan\' failed, with exception:", var9);
                    Thread.sleep(1000L);
                    continue;
                } finally {
                    this.closeJedisCommandsHandler(var4);
                }

                return var5;
            }

            throw new Exception("RedisClient \'hscan\' failed");
        }
    }

    public ScanResult<String> scan(String var1, ScanParams var2) throws Exception {
        if(!installType.equals(InstallType.STANDALONE)) {
            return null;
        } else {
            int var3 = 3;

            while(var3-- > 0) {
                try {
                    Jedis var5 = this.getJedis();
                    Throwable var6 = null;

                    ScanResult var7;
                    try {
                        var7 = var5.scan(var1, var2);
                    } catch (Throwable var17) {
                        var6 = var17;
                        throw var17;
                    } finally {
                        if(var5 != null) {
                            if(var6 != null) {
                                try {
                                    var5.close();
                                } catch (Throwable var16) {
                                    var6.addSuppressed(var16);
                                }
                            } else {
                                var5.close();
                            }
                        }

                    }

                    return var7;
                } catch (Exception var19) {
                    logger.warn("RedisClient handle Redis \'sscan\' failed", var19);
                    Thread.sleep(1000L);
                }
            }

            throw new Exception("Redis \'sscan\' failed");
        }
    }

    static {
        jedisPoolConfig.setMaxWaitMillis(2000L);
        jedisPoolConfig.setMaxIdle(5);
        jedisPoolConfig.setMinIdle(1);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestWhileIdle(false);
    }
}
