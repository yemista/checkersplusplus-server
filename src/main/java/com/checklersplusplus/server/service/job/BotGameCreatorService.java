package com.checklersplusplus.server.service.job;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.BotRepository;
import com.checklersplusplus.server.model.BotModel;
import com.checklersplusplus.server.service.GameService;

@Profile("bot")
@Service
public class BotGameCreatorService {

	private static final Logger logger = LoggerFactory.getLogger(BotGameCreatorService.class);

	private static final long TWO_MINUTE_MILLIS = 1000 * 60 * 2;
	
	@Autowired
	private BotRepository botRepository;
	
	@Autowired
	private GameService gameService;
	
	@Transactional(isolation = Isolation.READ_COMMITTED)
	@Scheduled(fixedDelay = TWO_MINUTE_MILLIS)
	public void createBots() {
		Optional<BotModel> bot = botRepository.findFirstByInUseFalseOrderByLastModifiedAsc();
		
		if (bot.isEmpty()) {
			return;
		}

		bot.get().setInUse(true);
		bot.get().setLastModified(LocalDateTime.now());
		botRepository.save(bot.get());
		
		boolean joinExisting = Math.random() > Double.valueOf(0.5);
		
		
		boolean firstMove = Math.random() > Double.valueOf(0.5);
		//boolean firstMove = true;
		gameService.botCreateGame(bot.get().getBotAccountId(), firstMove);
		logger.debug(String.format("BOT IN USE: %s", bot.get().getBotAccountId().toString()));
	}
}
