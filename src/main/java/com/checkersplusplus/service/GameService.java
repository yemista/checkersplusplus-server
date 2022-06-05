package com.checkersplusplus.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.ActiveGameRepository;
import com.checkersplusplus.dao.GameRepository;
import com.checkersplusplus.dao.models.ActiveGameModel;
import com.checkersplusplus.dao.models.GameModel;
import com.checkersplusplus.exceptions.CannotJoinGameException;
import com.checkersplusplus.service.enums.GameStatus;
import com.checkersplusplus.service.models.ActiveGame;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.OpenGames;
import com.checkersplusplus.service.models.Session;

@Service
@Transactional
public class GameService {
	
	private static final Logger logger = Logger.getLogger(GameService.class);

	private static final int PAGE_SIZE = 20;

	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private ActiveGameRepository activeGameRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	public Game getActiveGame(String token) {
		GameModel gameModel = gameRepository.getActiveGameByToken(token);
		
		if (gameModel == null) {
			return null;
		}
		
		return new Game(gameModel.getId(), gameModel.getState(), getGameStatus(gameModel), gameModel.getRedId(), gameModel.getBlackId(), gameModel.getWinnerId());
	}
	
	public List<Game> getActiveGames() {
		List<GameModel> gameModels = gameRepository.getActivesGames();
		return new ArrayList<>(
					gameModels.stream()
							.map(gameModel -> new Game(gameModel.getId(), gameModel.getState(), getGameStatus(gameModel), gameModel.getRedId(), gameModel.getBlackId(), gameModel.getWinnerId()))
							.collect(Collectors.toSet()));
	}
	
	public void forfeitGame(String gameId, String userId) {
		logger.debug("Forfeiting game " + gameId + " by userId " + userId);
		gameRepository.forfeitGame(userId, gameId);
		logger.debug("Forfeited game " + gameId);
	}
	
	public Game createGame(String token) {
		Session session = sessionService.getSession(token);
		logger.debug("initializing game for userId: " + session.getUserId());
		ActiveGame activeGame = insertIntoActiveGames(session.getUserId(), UUID.randomUUID().toString());
		Game game = insertIntoGames(activeGame.getGameId(), session.getUserId());
		logger.debug("successfully initialized game for userId: " + session.getUserId());
		return game;
	}
	
	private Game insertIntoGames(String gameId, String userId) {
		GameModel gameModel = new GameModel();
		gameModel.setRedId(userId);
		gameModel.setCreated(new Date());
		gameModel.setId(gameId);
		com.checkersplusplus.engine.Game gameEngine = new com.checkersplusplus.engine.Game();
		gameModel.setState(gameEngine.getGameState());
		gameRepository.save(gameModel);
		return new Game(gameModel.getId(), gameModel.getState(), GameStatus.PENDING, gameModel.getRedId(), null, null);
	}

	public Game joinGame(String userId, String gameId) throws Exception {
		logger.debug(String.format("Joining game %s by user %s", gameId, userId));
		insertIntoActiveGames(userId, gameId);
		logger.debug(String.format("Inserted game %s by user %s into active_games", gameId, userId));
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
		logger.debug(String.format("Joined game %s by user %s", gameId, userId));
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

	public OpenGames getOpenGames(Integer page) {
		int requestedPage = page == null ? 0 : page;
		Pageable pageable = PageRequest.of(requestedPage, PAGE_SIZE, Sort.by("created").ascending());
		List<GameModel> gameModels = gameRepository.getOpenGames(pageable);
		
		if (CollectionUtils.isEmpty(gameModels)) {
			return new OpenGames(Collections.emptyList());
		}
		
		List<Game> games = gameModels.stream()
									 .map(gameModel -> new Game(gameModel.getId(), gameModel.getState(), getGameStatus(gameModel), gameModel.getRedId(), gameModel.getBlackId(), gameModel.getWinnerId()))
									 .collect(Collectors.toList());
		return new OpenGames(games);
	}
}
