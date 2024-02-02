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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameEventRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.enums.GameEvent;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameEventModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.service.RatingService;

@Profile("websocket")
@Service
@Transactional
public class TimeoutService {

	private static final int TEN_SECONDS_MILLIS = 10 * 1000;
	
	private static final Logger logger = LoggerFactory.getLogger(TimeoutService.class);

	@Autowired
	private AccountRepository accountRepository;
	
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
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	public void checkForTimeouts() {
		try {
			logger.debug("Checking for timeouts");
			
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime timeoutThreshold = now.minusMinutes(timeoutMinutes);
			List<GameModel> deactivatedGames = new ArrayList<>();
			List<GameModel> activeGames = gameRepository.getRunningGames();
			
			for (GameModel game : activeGames) {
				UUID blackId = game.getBlackId();
				UUID redId = game.getRedId();
				
				Optional<SessionModel> blackSession = sessionRepository.getActiveByAccountId(blackId);
				Optional<AccountModel> blackAccount = accountRepository.findById(blackId);
				
				if (!blackAccount.get().isBot()) {
					if (blackSession.isEmpty()) {
						Optional<SessionModel> mostRecentSession = sessionRepository.findFirstByAccountIdAndLastModifiedGreaterThanOrderByLastModifiedDesc(blackId, timeoutThreshold);
						
						if (mostRecentSession.isEmpty()) {
							game.setWinnerId(redId);
							game.setInProgress(false);
							game.setActive(false);
							gameRepository.save(game);
							deactivatedGames.add(game);
							
							GameEventModel gameEvent = new GameEventModel();
							gameEvent.setActive(true);
							gameEvent.setCreated(LocalDateTime.now());
							gameEvent.setEvent(GameEvent.TIMEOUT.getMessage());
							gameEvent.setEventRecipientAccountId(redId);
							gameEvent.setGameId(game.getGameId());
							gameEventRepository.save(gameEvent);
							
							continue;
						}
					} else {
						Optional<SessionModel> expiredSession = sessionRepository.getActiveSessionForAccountOlderThan(timeoutThreshold, blackId);
						
						if (expiredSession.isPresent()) {
							game.setWinnerId(redId);
							game.setInProgress(false);
							game.setActive(false);
							gameRepository.save(game);
							deactivatedGames.add(game);
							
							GameEventModel gameEvent = new GameEventModel();
							gameEvent.setActive(true);
							gameEvent.setCreated(LocalDateTime.now());
							gameEvent.setEvent(GameEvent.TIMEOUT.getMessage());
							gameEvent.setEventRecipientAccountId(redId);
							gameEvent.setGameId(game.getGameId());
							gameEventRepository.save(gameEvent);
							
							GameEventModel lossEvent = new GameEventModel();
							lossEvent.setActive(true);
							lossEvent.setCreated(LocalDateTime.now());
							lossEvent.setEvent(GameEvent.TIMEOUT_LOSS.getMessage());
							lossEvent.setEventRecipientAccountId(blackId);
							lossEvent.setGameId(game.getGameId());
							gameEventRepository.save(lossEvent);
							
							continue;
						}
					}
				}
				
				Optional<SessionModel> redSession = sessionRepository.getActiveByAccountId(redId);
				Optional<AccountModel> redAccount = accountRepository.findById(redId);
				
				if (!redAccount.get().isBot()) {
					if (redSession.isEmpty()) {
						Optional<SessionModel> mostRecentSession = sessionRepository.findFirstByAccountIdAndLastModifiedGreaterThanOrderByLastModifiedDesc(redId, timeoutThreshold);
						
						if (mostRecentSession.isEmpty()) {
							game.setWinnerId(blackId);
							game.setInProgress(false);
							game.setActive(false);
							gameRepository.save(game);
							deactivatedGames.add(game);
							
							GameEventModel gameEvent = new GameEventModel();
							gameEvent.setActive(true);
							gameEvent.setCreated(LocalDateTime.now());
							gameEvent.setEvent(GameEvent.TIMEOUT.getMessage());
							gameEvent.setEventRecipientAccountId(blackId);
							gameEvent.setGameId(game.getGameId());
							gameEventRepository.save(gameEvent);
							
							continue;
						}
					} else {
						Optional<SessionModel> expiredSession = sessionRepository.getActiveSessionForAccountOlderThan(timeoutThreshold, redId);
						
						if (expiredSession.isPresent()) {
							game.setWinnerId(blackId);
							game.setInProgress(false);
							game.setActive(false);
							gameRepository.save(game);
							deactivatedGames.add(game);
							
							GameEventModel gameEvent = new GameEventModel();
							gameEvent.setActive(true);
							gameEvent.setCreated(LocalDateTime.now());
							gameEvent.setEvent(GameEvent.TIMEOUT.getMessage());
							gameEvent.setEventRecipientAccountId(blackId);
							gameEvent.setGameId(game.getGameId());
							gameEventRepository.save(gameEvent);
							
							GameEventModel lossEvent = new GameEventModel();
							lossEvent.setActive(true);
							lossEvent.setCreated(LocalDateTime.now());
							lossEvent.setEvent(GameEvent.TIMEOUT_LOSS.getMessage());
							lossEvent.setEventRecipientAccountId(redId);
							lossEvent.setGameId(game.getGameId());
							gameEventRepository.save(lossEvent);
							
							continue;
						}
					}
				}
			}
			
			for (GameModel game : deactivatedGames) {
				ratingService.updatePlayerRatings(game.getGameId());
			}
		} catch (Exception e) {
			logger.error("Exception thrown in timeout service body", e);
		}
	}

}


