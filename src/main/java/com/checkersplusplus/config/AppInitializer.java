package com.checkersplusplus.config;


import org.apache.log4j.Logger;

//@Configuration
public class AppInitializer { //extends AbstractAnnotationConfigDispatcherServletInitializer implements WebApplicationInitializer {
	
	private static final Logger logger = Logger.getLogger(AppInitializer.class);
//	
//	@Override
//	protected Class<?>[] getRootConfigClasses() {
//	  return new Class[] { HibernateConfig.class };
//	}
//	
//
//	@Override
//	protected String[] getServletMappings() {
//	  return new String[] { "/" };
//	}
//	
//	@Override
//	protected String getServletName() {
//		return "checkers-plus-plus";
//	}
//	
//	@Override
//    public void onStartup(ServletContext servletContext) throws ServletException {
//		try {
//			JobDetail job1 = JobBuilder.newJob(ActiveGamesJob.class).withIdentity("activeGames", "group1").build();
//	        Trigger trigger1 = TriggerBuilder.newTrigger().withIdentity("simpleTrigger", "group1")
//	                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(ActiveGamesJob.MINUTES_BETWEEN_JOB_EXECUTION)).build();   
//	        Scheduler scheduler1 = new StdSchedulerFactory().getScheduler(); 
//	        scheduler1.start(); 
//	        scheduler1.scheduleJob(job1, trigger1); 
//		} catch (SchedulerException e) {
//			logger.error("Failed to start ActiveGamesJob", e);
//			throw new ServletException();
//		}
//    }
}

