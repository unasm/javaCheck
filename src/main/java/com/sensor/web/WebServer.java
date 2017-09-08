package com.sensor.web;

import com.sensor.common.Constants;
import com.sensor.common.RedisClient;
import com.sensor.common.client.ZookeeperClient;
import com.sensor.common.config.QueryEngineServerConfig;
import com.sensor.common.config.RoleStatus;
import com.sensor.common.config.WebServerConfig;
import com.sensor.db.MetaConnectionPool;
import com.sensor.db.OLAPEngineConnectionPool;
import com.sensor.queryengine.QueryEngineUtil;
import com.sensor.queryengine.query.SQLQueryService;
import com.sensor.web.db.WebMetaDataService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by tianyi on 26/08/2017.
 */
public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static CountDownLatch shutdownSignal = new CountDownLatch(1);
    private static Server server;
    private static ZookeeperClient zookeeperClient;
    private static WebServerConfig webServerConfig = new WebServerConfig();

    public WebServer() {
    }

    private void start() throws Exception {
        try {
            MetaConnectionPool.init();
            OLAPEngineConnectionPool.init();
            RedisClient.init();
            zookeeperClient = new ZookeeperClient();
            zookeeperClient.watchGlobalConfigInfo();
            QueryEngineServerConfig config = zookeeperClient.getQueryEngineServerConfig();
            WebMetaDataService.getInstance();

            SQLQueryService.init(config, zookeeperClient.getHiveInfo());

            webServerConfig =  zookeeperClient.getWebServerConfig();


            QueuedThreadPool pool = new QueuedThreadPool();
            pool.setMaxThreads(50);
            pool.setMinThreads(10);
            pool.setName("webServer");
            server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(webServerConfig.getPort());
            server.addConnector(connector);

            HashSessionIdManager sessionIdManager = new HashSessionIdManager();
            server.setSessionIdManager(sessionIdManager);


            DispatcherServlet servlet = new DispatcherServlet();
            servlet.setContextConfigLocation("classpath:config/spring-servlet.xml");

            HashSessionManager sessionManager = new HashSessionManager();
            SessionHandler sessionHandler = new SessionHandler(sessionManager);

            ServletContextHandler servletContextHandler = new ServletContextHandler();
            servletContextHandler.setSessionHandler(sessionHandler);
            //servletContextHandler.addServlet(new ServletHolder(servlet), "/*");
            servletContextHandler.setContextPath("/");
            servletContextHandler.addServlet(new ServletHolder(servlet), "/*");
            servletContextHandler.addServlet(new ServletHolder(new HttpServlet() {
                protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                    RoleStatus status = QueryEngineUtil.queryEngineCounter.calcRoleStatus();
                    System.out.println(status);
                    //var3.setStatus(AliveStatus.ALIVE);
                    //var3.addCounter("cachable_query_count", Long.valueOf(CacheService.getInstance().getCachableQueryCount()));
                    //var3.addCounter("cache_hit_count", Long.valueOf(CacheService.getInstance().getHitCount()));
                    response.getWriter().write(Constants.DEFAULT_OBJECT_MAPPER.writeValueAsString(status));
                    response.setContentType("application/json");
                    response.setStatus(202);
                }
            }), "/status");
            server.setHandler(servletContextHandler);
            //server.setHandler(new com.sensor.web.Demo());
            server.start();
            server.join();
        } finally {
            logger.info("login started ended");
            if (zookeeperClient != null) {
                zookeeperClient.close();
            }
            MetaConnectionPool.destroy();
        }
    }

    public static void main(String[] params) throws Exception {
        //Runtime.getRuntime().addShutdownHook(new WebServer.CleanWorkThread(null));
        byte exit = 0;
        WebServer server = new WebServer();
        try {
            server.start();
        } catch (Exception ex) {
            logger.warn("start web with exception", ex);
            exit = 1;
        }
        logger.info("web server stopped");
        shutdownSignal.countDown();
        System.exit(exit);
    }

    private static class CleanWorkThread extends Thread {
        private static final Logger logger = LoggerFactory.getLogger(WebServer.CleanWorkThread.class);
        private CleanWorkThread() {
        }

        public void run() {
            logger.info("clean work starts");
            try {
                if (WebServer.server != null) {
                    WebServer.server.stop();
                }
                WebServer.shutdownSignal.await();
            } catch (Exception ex) {
                logger.debug("exception for waitting");
            }
            logger.info("clean work ends");
        }
    }
}
