package init;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import database.PooledDataSourceWrapper;

public class CheckersPlusPlusServletContextListener implements ServletContextListener {
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("ServletContextListener destroyed");
		PooledDataSourceWrapper.getInstance().shutdown();
	}

        //Run this before web application is started
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("ServletContextListener started");	
		PooledDataSourceWrapper.getInstance();
	}
}
