package com.checklersplusplus.server.service.job;

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

import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.SessionModel;

@Service
public class TimeoutService {

	private static final int QUEUE_SIZE = 500;
	
	private static final Logger logger = LoggerFactory.getLogger(TimeoutService.class);
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
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
			List<UUID> accountIdsToCheck = new ArrayList<>();
			
			for (int counter = 0; counter < QUEUE_SIZE && counter < expiredSessions.size(); ++counter) {
				sessionModelsToInactivate.add(expiredSessions.get(counter).getSessionId());
				accountIdsToCheck.add(expiredSessions.get(counter).getAccountId());
			}
			
			sessionRepository.invalidateSessionsBySessionIds(sessionModelsToInactivate);
			
			// TODO should we only timeout games that are inProgress=true?
			List<GameModel> activeGames = gameRepository.getActiveGamesByAccountId(accountIdsToCheck);
			
			for (GameModel game : activeGames) {
				if (game.isInProgress()) {
					UUID accountToSendEvent = null;
					
					if (accountIdsToCheck.contains(game.getBlackId())) {
						accountToSendEvent = game.getRedId();
					}
					
					if (accountIdsToCheck.contains(game.getRedId())) {
						accountToSendEvent = game.getBlackId();
					}
					
					if (accountToSendEvent != null) {
						
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception thrown in timeout service body", e);
		}
	}
}
