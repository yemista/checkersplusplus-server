package com.checklersplusplus.server.service.job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.engine.ai.TrainingOpponent;
import com.checkersplusplus.engine.enums.Color;
import com.checkersplusplus.engine.moves.Move;
import com.checklersplusplus.server.dao.BotRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.exception.InvalidMoveException;
import com.checklersplusplus.server.model.BotModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.service.GameService;
import com.checklersplusplus.server.util.MoveUtil;

@Profile("bot")
@Service
public class BotMoveService {

	private static final Logger logger = LoggerFactory.getLogger(BotMoveService.class);

	private static final long TWO_SECOND_MILLIS = 1000 * 2;
	
	@Autowired
	private BotRepository botRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private GameService gameService;
	
	@Scheduled(fixedDelay = TWO_SECOND_MILLIS)
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void doBotMove() {
		Random random = new Random();
		int secondsToSleep = random.nextInt(0, 3);
		
		try {
			Thread.sleep(1000 * secondsToSleep);
		} catch (Exception e) {
			logger.error("Error occured in BotMoveService while sleeping", e);
		}
		
		List<BotModel> bots = botRepository.findByInUseTrue();
		
		for (BotModel bot : bots) {
			Optional<GameModel> game = gameRepository.getActiveGameByAccountId(bot.getBotAccountId());
			
			try {
				if (game.isPresent() && game.get().isInProgress()) {
					Color botColor = bot.getBotAccountId().equals(game.get().getBlackId()) ? Color.BLACK : Color.RED;

					if (isBotsTurn(botColor, game.get().getGameState())) {
						int botLevel = bot.getLevel() == null ? 1 : bot.getLevel();
						List<Move> moves = TrainingOpponent.getBestDeepMove(game.get().getGameState(), botColor, botLevel);
						
						if (moves.isEmpty()) {
							bot.setInUse(false);
							botRepository.save(bot);
							gameService.forfeitGameInternal(bot.getBotAccountId(), game.get().getGameId());
						} else {
							gameService.botMove(bot.getBotAccountId(), game.get().getGameId(), moves);
						}
					}
				}
				
				if (game.isPresent() && !game.get().isInProgress() && gameIsTooOld(game.get().getLastModified())) {
					bot.setInUse(false);
					botRepository.save(bot);
					gameRepository.delete(game.get());
				}
			} catch (InvalidMoveException ex) {
				logger.error("InvalidMoveException occured in BotMoveService: " + MoveUtil.convertCoordinatePairsToString(ex.getCoordinates()), ex);
				bot.setInUse(false);
				botRepository.save(bot);
				
				try {
					gameService.forfeitGameInternal(bot.getBotAccountId(), game.get().getGameId());
				} catch (CheckersPlusPlusServerException e) {
					logger.error("Error occured in BotMoveService catch clause", e);
				}
			} catch (Exception e) {
				logger.error("Error occured in BotMoveService", e);
			}
		}
	}

	private boolean gameIsTooOld(LocalDateTime lastModified) {
		LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(6);
		return lastModified.isBefore(fifteenMinutesAgo);
	}

	private boolean isBotsTurn(Color botColor, String gameState) {
		String[] parts = gameState.split("\\|");
		
		if (botColor == Color.BLACK) {
			return Integer.parseInt(parts[1]) % 2 == 0;
		} else {
			return Integer.parseInt(parts[1]) % 2 == 1;
		}
	}
}
