package com.checklersplusplus.server.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.model.SessionModel;

@Service
public class TimeoutService {

	private static final int QUEUE_SIZE = 500;
	
	private static final Logger logger = LoggerFactory.getLogger(TimeoutService.class);
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Value("${checkersplusplus.timeout.minutes}")
	private Integer timeoutMinutes;
	
	@Scheduled(fixedDelay = 10000)
	public void checkForTimeouts() {
		// TODO test
		try {
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime fiveMinutesAgo = now.minusMinutes(timeoutMinutes);
			List<SessionModel> expiredSessions = sessionRepository.getActiveSessionsOlderThan(fiveMinutesAgo);
			List<UUID> sessionModelsToInactivate = new ArrayList<>();
			
			for (int counter = 0; counter < QUEUE_SIZE && counter < expiredSessions.size(); ++counter) {
				sessionModelsToInactivate.add(expiredSessions.get(counter).getSessionId());
			}
			
			sessionRepository.invalidateSessionsBySessionIds(sessionModelsToInactivate);
		} catch (Exception e) {
			logger.error("Exception thrown in timeout service body", e);
		}
	}
}
