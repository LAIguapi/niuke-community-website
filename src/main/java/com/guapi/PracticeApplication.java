package com.guapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication

public class PracticeApplication {

    private static Logger logger= LoggerFactory.getLogger(PracticeApplication.class);


    public static void main(String[] args) {
        ConfigurableApplicationContext application = SpringApplication.run(PracticeApplication.class, args);
        String domain = application.getEnvironment().getProperty("community.path.domain");
        String contextPath = application.getEnvironment().getProperty("server.servlet.context-path");
        logger.info("\n\n-------------------------------------------------------\n\n\t" + "Local访问网址: "+domain+contextPath+"/index\n\n"+ "-------------------------------------------------------");

    }

}
