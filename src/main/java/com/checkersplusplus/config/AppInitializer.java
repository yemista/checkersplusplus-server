package com.checkersplusplus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

@Configuration
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
	
	@Override
	protected Class<?>[] getRootConfigClasses() {
	  return new Class[] { HibernateConfig.class };
	}
	
	@Override
	protected Class<?>[] getServletConfigClasses() {
	  return new Class[] { WebMvcConfig.class, HibernateConfig.class, AppContext.class };
	}
	
	@Override
	protected String[] getServletMappings() {
	  return new String[] { "/" };
	}
	
	@Override
	protected String getServletName() {
		return "checkers-plus-plus";
	}
}

