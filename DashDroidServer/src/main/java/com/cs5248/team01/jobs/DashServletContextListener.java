package com.cs5248.team01.jobs;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class DashServletContextListener implements ServletContextListener {
	Logger logger = Logger.getLogger(DashServletContextListener.class.getSimpleName());
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
        try {
            
            ThreadExecutor.shutdown();
            
        } catch (Exception e) {
        	logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
        }
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		ThreadExecutor.init();
	}
	
	
}
