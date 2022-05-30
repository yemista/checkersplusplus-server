package com.checkersplusplus.service;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.ActiveGameRepository;
import com.checkersplusplus.dao.GameRepository;
import com.checkersplusplus.dao.SessionRepository;
import com.checkersplusplus.dao.models.ActiveGameModel;
import com.checkersplusplus.dao.models.GameModel;
import com.checkersplusplus.exceptions.CannotJoinGameException;
import com.checkersplusplus.service.enums.GameStatus;
import com.checkersplusplus.service.models.ActiveGame;
import com.checkersplusplus.service.models.Game;

@Service
@Transactional
public class NewGameService {
	
	private static final Logger logger = Logger.getLogger(NewGameService.class);

	@Autowired
	private SessionRepository sessionRespository;
	
	@Autowired
	private ActiveGameRepository activeGameRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	public Game joinGame(String userId, String gameId) throws Exception {
		insertIntoActiveGames(userId, gameId);
		Optional<GameModel> gameModel = gameRepository.findById(gameId);
		
		if (!gameModel.isPresent()) {
			logger.debug("Could not join game " + gameId + " because it was not found");
			throw new CannotJoinGameException();
		}
		
		if (StringUtils.isNotBlank(gameModel.get().getBlackId())) {
			logger.debug("Could not join game " + gameId + " because it was full");
			throw new CannotJoinGameException();
		}
		
		gameModel.get().setBlackId(userId);
		gameRepository.save(gameModel.get());
		return new Game(gameModel.get().getId(), gameModel.get().getState(), getGameStatus(gameModel.get()), gameModel.get().getRedId(), 
				gameModel.get().getBlackId(), gameModel.get().getWinnerId());
	}
	
	private ActiveGame insertIntoActiveGames(String userId, String gameId) {
		ActiveGameModel activeGameModel = new ActiveGameModel();
		activeGameModel.setUserId(userId);
		activeGameModel.setGameId(gameId);
		activeGameRepository.save(activeGameModel);
		return new ActiveGame(activeGameModel.getUserId(), activeGameModel.getGameId());
	}
	
	private GameStatus getGameStatus(GameModel gameModel) {
		if (gameModel.getBlackId() == null) {
			return GameStatus.PENDING;
		}
		
		if (gameModel.getWinnerId() != null) {
			return GameStatus.COMPLETE;
		}
		
		if (gameModel.getForfeitId() != null) {
			return GameStatus.ABORTED;
		}
		
		if (gameModel.getBlackId() != null && gameModel.getRedId() != null) {
			return GameStatus.RUNNING;
		}
		
		return GameStatus.CANCELED;
	}
}
