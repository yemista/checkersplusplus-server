package com.checklersplusplus.server.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {
	
	@Scheduled(fixedDelay = 1000)
	public void updateClients() {
		
	}
}
