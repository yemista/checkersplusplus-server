package com.checklersplusplus.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {
	
	private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
	
	@Scheduled(fixedDelay = 1000)
	public void updateClients() {
		
	}
}
