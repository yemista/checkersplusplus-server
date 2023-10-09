package com.checklersplusplus.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.engine.Coordinate;
import com.checkersplusplus.engine.CoordinatePair;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.entities.request.Move;
import com.checklersplusplus.server.entities.response.Game;
import com.checklersplusplus.server.exception.CannotCancelGameException;
import com.checklersplusplus.server.exception.CannotCreateGameException;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.exception.GameNotFoundException;
import com.checklersplusplus.server.exception.InvalidMoveException;
import com.checklersplusplus.server.exception.SessionNotFoundException;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.SessionModel;

@Service
@Transactional
public class GameService {

	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
		
	public List<Game> getOpenGames() {
		return gameRepository.getOpenGames().stream().map(gameModel -> Game.fromModel(gameModel)).collect(Collectors.toList());
	}
	
	public Game move(UUID sessionId, UUID gameId, List<Move> moves) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		Optional<GameModel> gameModel = gameRepository.getByGameId(gameId);
		
		if (gameModel.isEmpty() || !gameModel.get().isActive() || !gameModel.get().isInProgress()) {
			throw new GameNotFoundException();
		}
		
		if (userNotPlayingGame(sessionModel.get().getAccountId(), gameModel.get())) {
			throw new GameNotFoundException();
		}

    	List<CoordinatePair> coordinates = moves.stream()
    			.map(move -> new CoordinatePair(new Coordinate(move.getStartCol(), move.getStartRow()), new Coordinate(move.getEndCol(), move.getEndRow())))
    			.collect(Collectors.toList());
    	com.checkersplusplus.engine.Game logicalGame = new com.checkersplusplus.engine.Game(gameModel.get().getGameState());
    	
    	if (logicalGame.isMoveLegal(coordinates)) {
    		logicalGame.doMove(coordinates);
    		gameModel.get().setGameState(logicalGame.getGameState());
    		gameRepository.save(gameModel.get());
    	} else {
    		throw new InvalidMoveException();
    	}
    	
    	return Game.fromModel(gameModel.get());
	}
	
	public void cancelGame(UUID sessionId, UUID gameId) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		Optional<GameModel> gameModel = gameRepository.getByGameId(gameId);
		
		if (gameModel.isEmpty() || !gameModel.get().isActive()) {
			throw new GameNotFoundException();
		}
		
		if (userNotPlayingGame(sessionModel.get().getAccountId(), gameModel.get())) {
			throw new GameNotFoundException();
		}
		
		if (gameStarted(gameModel.get())) {
			throw new CannotCancelGameException();
		}
		
		gameModel.get().setActive(false);
		gameRepository.save(gameModel.get());
	}

	public Game joinGame(UUID sessionId, UUID gameId) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		Optional<GameModel> gameModel = gameRepository.getByGameId(gameId);
		
		if (gameModel.isEmpty() || !gameModel.get().isActive() || gameModel.get().isInProgress()) {
			throw new GameNotFoundException();
		}
		
		if (gameModel.get().getBlackId() == null) {
			gameModel.get().setBlackId(sessionModel.get().getAccountId());
		} else {
			gameModel.get().setRedId(sessionModel.get().getAccountId());
		}
		
		gameModel.get().setInProgress(true);
		gameRepository.save(gameModel.get());
		return Game.fromModel(gameModel.get());
	}
	
	public Game createGame(UUID sessionId, boolean isBlack) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		Optional<GameModel> activeGameModel = gameRepository.getActiveGameByAccountId(sessionModel.get().getAccountId());
		
		if (activeGameModel.isPresent()) {
			throw new CannotCreateGameException();
		}
		
		GameModel gameModel = new GameModel();
		gameModel.setActive(true);
		gameModel.setInProgress(false);
		gameModel.setCreated(LocalDateTime.now());
		gameModel.setLastModified(LocalDateTime.now());
		
		if (isBlack) {
			gameModel.setBlackId(sessionModel.get().getAccountId());
		} else {
			gameModel.setRedId(sessionModel.get().getAccountId());
		}
		
		com.checkersplusplus.engine.Game game = new com.checkersplusplus.engine.Game();
		gameModel.setGameState(game.getGameState());
		gameRepository.save(gameModel);
		return Game.fromModel(gameModel);
	}
	
	public void forfeitGame(UUID sessionId, UUID gameId) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		UUID accountId = sessionModel.get().getAccountId();
		
		Optional<GameModel> gameModel = gameRepository.findById(gameId);
		
		if (gameModel.isEmpty() || !gameModel.get().isActive() || !gameModel.get().isInProgress()) {
			throw new GameNotFoundException();
		}
		
		if (accountId.equals(gameModel.get().getBlackId())) {
			gameModel.get().setWinnerId(gameModel.get().getRedId());
		} else if (accountId.equals(gameModel.get().getRedId())) {
			gameModel.get().setWinnerId(gameModel.get().getBlackId());
		} else {
			throw new CheckersPlusPlusServerException("User not found for game.");
		}
		
		gameModel.get().setActive(false);
		gameModel.get().setInProgress(false);
		gameRepository.save(gameModel.get());
	}

	public Optional<Game> findByGameId(UUID gameId) {
		Optional<GameModel> gameModel = gameRepository.findById(gameId);
		
		if (gameModel.isEmpty()) {
			return Optional.empty();
		}
		
		return Optional.of(Game.fromModel(gameModel.get()));
	}
	
	private boolean gameStarted(GameModel gameModel) {
		return gameModel.isInProgress() && gameModel.isActive();
	}

	private boolean userNotPlayingGame(UUID accountId, GameModel gameModel) {
		return !(accountId.equals(gameModel.getBlackId()) || accountId.equals(gameModel.getRedId()));
	}
}
