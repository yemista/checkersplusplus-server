package com.checklersplusplus.server.service.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BotMoveService {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

	private static final long TEN_SECOND_MILLIS = 1000 * 10;
	
	@Scheduled(fixedDelay = TEN_SECOND_MILLIS)
	public void doBotMove() {
		
		
	}
}
