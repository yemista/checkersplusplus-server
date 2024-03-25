package com.checklersplusplus.server.service.job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
import com.checklersplusplus.server.dao.BotRepository;
import com.checklersplusplus.server.dao.GameEventRepository;
import com.checklersplusplus.server.dao.GameMoveRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.enums.GameEvent;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.BotModel;
import com.checklersplusplus.server.model.GameEventModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.GameMoveModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.service.RatingService;

@Profile("websocket")
@Service
@Transactional
public class TimeoutService {

	private static final int ONE_SECONDS_MILLIS = 1 * 1000;
	private static final int ONE_MINUTE_MILLIS = 60 * 1000;
	
	private static final Logger logger = LoggerFactory.getLogger(TimeoutService.class);

	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private BotRepository botRepository;
	
	@Autowired
	private GameMoveRepository gameMoveRepository;
	
	@Autowired
	private GameEventRepository gameEventRepository;
	
	@Autowired
	private RatingService ratingService;
	
	@Value("${checkersplusplus.timeout.minutes}")
	private Integer timeoutMinutes;
	
	@Value("${checkersplusplus.timeout.move.minutes}")
	private Integer moveTimeoutMinutes;
	
//	@Scheduled(fixedDelay = ONE_MINUTE_MILLIS)
//	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
//	public void checkForSessionTimeouts() {
//		LocalDateTime now = LocalDateTime.now();
//		LocalDateTime timeoutThreshold = now.minusMinutes(timeoutMinutes);
//		List<SessionModel> expiredSessions = sessionRepository.findByActiveAndLastModifiedLessThan(true, timeoutThreshold);
//		Set<UUID> expiredSessionAccountIds = new HashSet<>();
//		expiredSessions.forEach(session -> expiredSessionAccountIds.add(session.getAccountId()));
//		
//		for (UUID accountId : expiredSessionAccountIds) {
//			Optional<GameModel> game = gameRepository.getActiveGameByAccountId(accountId);
//			
//			if (game.isPresent()) {
//				continue;
//			}
//			
//			sessionRepository.inactiveExistingSessions(accountId);
//		}
//	}
	
	@Scheduled(fixedDelay = ONE_SECONDS_MILLIS)
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
	public void checkForMoveTimeouts() {
		try {
			List<GameModel> activeGames = gameRepository.getRunningGames();
			
			for (GameModel game : activeGames) {
				if (!game.isInProgress()) {
					continue;
				}
				
				UUID blackId = game.getBlackId();
				UUID redId = game.getRedId();
				Optional<GameMoveModel> latestMove = gameMoveRepository.findFirstByGameIdOrderByMoveNumberDesc(game.getGameId());
				
				if (latestMove.isPresent() && moveTooOld(latestMove.get().getCreated())) {
					UUID nextToMove = latestMove.get().getMoveNumber() % 2 == 1 ? redId : blackId;
					UUID opponent = nextToMove.equals(blackId) ? redId : blackId;
					
					Optional<SessionModel> nextToMoveSession = sessionRepository.getActiveByAccountId(nextToMove);
					Optional<AccountModel> nextToMoveAccount = accountRepository.findById(nextToMove);
					
					if (nextToMoveAccount.isPresent()) {
						game.setWinnerId(opponent);
						game.setInProgress(false);
						game.setActive(false);
						game.setFinalized(true);
						game.setLastModified(LocalDateTime.now());
						gameRepository.save(game);
						Map<UUID, Integer> newRatings = ratingService.updatePlayerRatings(game.getGameId());
						
						GameEventModel gameEvent = new GameEventModel();
						gameEvent.setActive(true);
						gameEvent.setCreated(LocalDateTime.now());
						gameEvent.setEvent(GameEvent.TIMEOUT.getMessage() + "|" + newRatings.get(opponent));
						gameEvent.setEventRecipientAccountId(opponent);
						gameEvent.setGameId(game.getGameId());
						gameEventRepository.save(gameEvent);
						
						if (nextToMoveAccount.get().isBot()) {
							Optional<BotModel> bot = botRepository.findById(nextToMove);
							bot.get().setInUse(false);
							botRepository.save(bot.get());
						} else if (nextToMoveSession.isPresent()) {
							GameEventModel lossEvent = new GameEventModel();
							lossEvent.setActive(true);
							lossEvent.setCreated(LocalDateTime.now());
							lossEvent.setEvent(GameEvent.TIMEOUT_LOSS.getMessage() + "|" + newRatings.get(nextToMove));
							lossEvent.setEventRecipientAccountId(nextToMove);
							lossEvent.setGameId(game.getGameId());
							gameEventRepository.save(lossEvent);
						}
					}					
				} else {
					// No move has been made yet
					if (gameTooOld(game.getLastModified())) {
						UUID opponent = redId;
						
						game.setWinnerId(opponent);
						game.setInProgress(false);
						game.setActive(false);
						game.setFinalized(true);
						game.setLastModified(LocalDateTime.now());
						gameRepository.save(game);
						Map<UUID, Integer> newRatings = ratingService.updatePlayerRatings(game.getGameId());
						
						GameEventModel gameEvent = new GameEventModel();
						gameEvent.setActive(true);
						gameEvent.setCreated(LocalDateTime.now());
						gameEvent.setEvent(GameEvent.TIMEOUT.getMessage() + "|" + newRatings.get(opponent));
						gameEvent.setEventRecipientAccountId(opponent);
						gameEvent.setGameId(game.getGameId());
						gameEventRepository.save(gameEvent);
					}			
				}
			}
		} catch (Exception e) {
			logger.error("Exception thrown in timeout service body", e);
		}
	}

	private boolean gameTooOld(LocalDateTime lastModified) {
		return moveTooOld(lastModified);
	}

	private boolean moveTooOld(LocalDateTime created) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime moveTimeoutThreshold = now.minusMinutes(moveTimeoutMinutes);
		return created.isBefore(moveTimeoutThreshold);
	}

}


