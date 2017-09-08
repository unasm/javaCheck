package com.sensor.common.listener;

import com.sensor.common.BonusContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;

/**
 * Created by woodle on 15/10/22.
 * 系统上下文环境
 */
public class SystemContextLoaderListener extends ContextLoaderListener {

    private static final Logger log = LoggerFactory.getLogger(SystemContextLoaderListener.class);

    private static final String[] resources = {"environment","cache"};

    @Override
    public void contextInitialized(ServletContextEvent event) {
     //   try {
     //       /*
     //       Properties props = new Properties();
     //       props.load(new FileInputStream("log4j.properties"));
     //       PropertyConfigurator.configure(props);
     //       */

     //       log.warn("get log 4j_redefined");
     //   } catch (FileNotFoundException ex) {
     //       System.out.println("started  loader FileNotFoundException ");
     //   } catch (IOException ex) {
     //       System.out.println("started  loader IOException ");
        //}

        System.out.println("started  loader listerner");
        // 加载environment系统配置文件
        BonusContext.init();
        super.contextInitialized(event);
    }
}