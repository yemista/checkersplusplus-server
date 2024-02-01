package com.checklersplusplus.server.service.job;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
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
import com.checklersplusplus.server.service.RatingService;

@Profile("websocket")
@Service
@Transactional
public class TimeoutService {

	private static final int QUEUE_SIZE = 200;
	private static final int TEN_SECONDS_MILLIS = 10 * 1000;
	
	private static final Logger logger = LoggerFactory.getLogger(TimeoutService.class);
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private GameEventRepository gameEventRepository;
	
	@Autowired
	private RatingService ratingService;
	
	@Value("${checkersplusplus.timeout.minutes}")
	private Integer timeoutMinutes;
	
	@Scheduled(fixedDelay = TEN_SECONDS_MILLIS)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void checkForTimeouts() {
		try {
			//logger.debug("Checking for timeouts");
			
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime timeoutThreshold = now.minusMinutes(timeoutMinutes);
			
			List<SessionModel> expiredSessions = sessionRepository.getActiveSessionsOlderThan(timeoutThreshold);
			List<UUID> sessionModelsToInactivate = new ArrayList<>();
			List<UUID> accountIdsToCheck = new ArrayList<>();
			
			for (int counter = 0; counter < QUEUE_SIZE && counter < expiredSessions.size(); ++counter) {
				sessionModelsToInactivate.add(expiredSessions.get(counter).getSessionId());
				accountIdsToCheck.add(expiredSessions.get(counter).getAccountId());
			}
			
			List<GameModel> activeGames = gameRepository.getActiveGamesInProgressByAccountId(accountIdsToCheck);
			List<Pair<GameModel, UUID>> gamesToUpdate = new ArrayList<>();
			List<Pair<GameModel, UUID>> gamesToUpdateAsLosers = new ArrayList<>();
			
			for (GameModel game : activeGames) {
				if (game.isInProgress()) {
					UUID accountToSendEvent = null;
					UUID accountWhichLoses = null;
					
					if (accountIdsToCheck.contains(game.getBlackId())) {
						accountToSendEvent = game.getRedId();
						accountWhichLoses = game.getBlackId();
					}
					
					if (accountIdsToCheck.contains(game.getRedId())) {
						accountToSendEvent = game.getBlackId();
						accountWhichLoses = game.getRedId();
					}
					
					if (accountToSendEvent != null) {
						gamesToUpdate.add(Pair.of(game,  accountToSendEvent));
						gamesToUpdateAsLosers.add(Pair.of(game,  accountWhichLoses));
					}
				}
			}
			
			for (Pair<GameModel, UUID> forUpdate : gamesToUpdate) {
				GameModel game = forUpdate.getFirst();
				UUID accountToSendEvent = forUpdate.getSecond();
				game.setWinnerId(accountToSendEvent);
				game.setInProgress(false);
				game.setActive(false);
				gameRepository.save(game);
				
				GameEventModel gameEvent = new GameEventModel();
				gameEvent.setActive(true);
				gameEvent.setCreated(LocalDateTime.now());
				gameEvent.setEvent(GameEvent.TIMEOUT.getMessage());
				gameEvent.setEventRecipientAccountId(accountToSendEvent);
				gameEvent.setGameId(game.getGameId());
				gameEventRepository.save(gameEvent);
			}
			
			for (Pair<GameModel, UUID> forUpdate : gamesToUpdateAsLosers) {
				GameModel game = forUpdate.getFirst();
				UUID accountToSendEvent = forUpdate.getSecond();
				
				createTimeoutLossGameEvent(accountToSendEvent, game.getGameId());
			}	
			
			for (Pair<GameModel, UUID> forUpdate : gamesToUpdate) {
				GameModel game = forUpdate.getFirst();
				ratingService.updatePlayerRatings(game.getGameId());
			}
		} catch (Exception e) {
			logger.error("Exception thrown in timeout service body", e);
		}
	}
	
	private void createTimeoutLossGameEvent(UUID accountId, UUID gameId) {
		Optional<SessionModel> session = sessionRepository.getActiveByAccountId(accountId);
		
		if (session.isEmpty()) {
			logger.error("Empty session for " + accountId.toString());
			return;
		}
		
		logger.debug(String.format("Forwarding timeout loss event: accountId %s gameId: %s", accountId.toString(), gameId.toString()));
		
		GameEventModel gameEvent = new GameEventModel();
		gameEvent.setActive(true);
		gameEvent.setEvent(GameEvent.TIMEOUT_LOSS.getMessage());
		gameEvent.setEventRecipientAccountId(accountId);
		gameEvent.setCreated(LocalDateTime.now());
		gameEvent.setGameId(gameId);
		gameEventRepository.save(gameEvent);
	}
}


