package com.checklersplusplus.server.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameHistoryRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.entities.response.GameHistory;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.exception.SessionNotFoundException;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.SessionModel;

public class GameHistoryService {

	private static final Logger logger = LoggerFactory.getLogger(GameService.class);
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private GameHistoryRepository gameHistoryRepository;
	
	@Autowired
	private AccountRepository accountRepository;
	
	// TODO test
	public List<GameHistory> getGameHistory(UUID sessionId, String sortDirection, Integer page, Integer pageSize) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		PageRequest pageRequest = PageRequest.of(pageSize, page, "asc".equalsIgnoreCase(sortDirection) ? Sort.by("lastModified").ascending() : Sort.by("lastModified").descending());
		Page<GameModel> openGames = gameHistoryRepository.findByRedIdOrBlackId(sessionModel.get().getAccountId(), pageRequest);
		List<GameHistory> gameHistory = openGames.stream().map(gameModel -> GameHistory.fromModel(gameModel)).collect(Collectors.toList());
		
		for (GameHistory game : gameHistory) {
			if (game.getBlackAccountId() != null) {
				Optional<AccountModel> account = accountRepository.findById(game.getBlackAccountId());
				
				if (account.isEmpty()) {
					logger.error("Failed to find account from given black accountId %s for game %s", game.getBlackAccountId().toString(), game.getGameId().toString());
				}
				
				game.setBlackUsername(account.get().getUsername());
			}
			
			if (game.getRedAccountId() != null) {
				Optional<AccountModel> account = accountRepository.findById(game.getRedAccountId());
				
				if (account.isEmpty()) {
					logger.error("Failed to find account from given red accountId %s for game %s", game.getRedAccountId().toString(), game.getGameId().toString());
				}
				
				game.setRedUsername(account.get().getUsername());
			}
		}
		
		return gameHistory;
	}
}
