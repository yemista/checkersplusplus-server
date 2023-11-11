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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.GameEventRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.enums.GameEvent;
import com.checklersplusplus.server.model.GameEventModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.SessionModel;

@Service
@Transactional
public class TimeoutService {

	private static final int QUEUE_SIZE = 500;
	private static final int TEN_SECONDS_MILLIS = 10 * 1000;
	
	private static final Logger logger = LoggerFactory.getLogger(TimeoutService.class);
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private GameEventRepository gameEventRepository;
	
	@Value("${checkersplusplus.timeout.minutes}")
	private Integer timeoutMinutes;
	
	@Scheduled(fixedDelay = TEN_SECONDS_MILLIS)
	public void checkForTimeouts() {
		try {
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime timeoutThreshold = now.minusMinutes(timeoutMinutes);
			List<SessionModel> sessions = sessionRepository.findAll();
			List<SessionModel> expiredSessions = sessionRepository.getActiveSessionsOlderThan(timeoutThreshold);
			List<UUID> sessionModelsToInactivate = new ArrayList<>();
			List<UUID> accountIdsToCheck = new ArrayList<>();
			
			for (int counter = 0; counter < QUEUE_SIZE && counter < expiredSessions.size(); ++counter) {
				sessionModelsToInactivate.add(expiredSessions.get(counter).getSessionId());
				accountIdsToCheck.add(expiredSessions.get(counter).getAccountId());
			}
			
			invalidateSessions(sessionModelsToInactivate);
			
			List<GameModel> activeGames = gameRepository.getActiveGamesInProgressByAccountId(accountIdsToCheck);
			
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
						game.setWinnerId(accountToSendEvent);
						game.setInProgress(false);
						game.setActive(false);
						gameRepository.save(game);
						
						GameEventModel gameEvent = new GameEventModel();
						gameEvent.setActive(true);
						gameEvent.setEvent(GameEvent.TIMEOUT.getMessage());
						gameEvent.setEventRecipientAccountId(accountToSendEvent);
						gameEvent.setGameId(game.getGameId());
						gameEventRepository.save(gameEvent);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception thrown in timeout service body", e);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void invalidateSessions(List<UUID> sessionModels) {
		sessionRepository.invalidateSessionsBySessionIds(sessionModels);		
	}
}
