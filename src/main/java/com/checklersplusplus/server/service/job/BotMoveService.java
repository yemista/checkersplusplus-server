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
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.engine.ai.TrainingOpponent;
import com.checkersplusplus.engine.enums.Color;
import com.checkersplusplus.engine.moves.Move;
import com.checklersplusplus.server.dao.BotRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.model.BotModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.service.GameService;

@Profile("server")
@Service
public class BotMoveService {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

	private static final long TWO_SECOND_MILLIS = 1000 * 2;
	
	@Autowired
	private BotRepository botRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private GameService gameService;
	
	@Scheduled(fixedDelay = TWO_SECOND_MILLIS)
	@Transactional
	public void doBotMove() {
		Random random = new Random();
		int secondsToSleep = random.nextInt(0, 7);
		
		try {
			Thread.sleep(1000 * secondsToSleep);
		} catch (Exception e) {
			logger.error("Error occured in BotMoveService while sleeping", e);
		}
		
		List<BotModel> bots = botRepository.findByInUseTrue();
		
		for (BotModel bot : bots) {
			Optional<GameModel> game = gameRepository.getActiveGameByAccountId(bot.getBotAccountId());
			
			try {
				if (game.isPresent()) {
					Color botColor = bot.getBotAccountId().equals(game.get().getBlackId()) ? Color.BLACK : Color.RED;

					if (isBotsTurn(botColor, game.get().getGameState())) {
						List<Move> moves = TrainingOpponent.getBestMove(game.get().getGameState(), botColor);
						gameService.botMove(bot.getBotAccountId(), game.get().getGameId(), moves);
					} else if (!game.get().isInProgress() && gameIsTooOld(game.get().getLastModified())) {
						game.get().setActive(false);
						gameRepository.save(game.get());
						bot.setInUse(false);
						botRepository.save(bot);
					}
				}
			} catch (CheckersPlusPlusServerException e) {
				logger.error("Error occured in BotMoveService", e);
			}
		}
	}

	private boolean gameIsTooOld(LocalDateTime lastModified) {
		LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
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
