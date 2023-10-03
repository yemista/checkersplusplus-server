package com.checklersplusplus.server.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.entities.response.Game;
import com.checklersplusplus.server.exception.CannotCreateGameException;
import com.checklersplusplus.server.exception.CannotJoinGameException;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.exception.GameNotFoundException;
import com.checklersplusplus.server.exception.NoActiveGameException;
import com.checklersplusplus.server.exception.SessionNotFoundException;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.SessionModel;

@Service
@Transactional
public class GameService {

	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	public List<Game> getOpenGames() {
		return gameRepository.getOpenGames().stream().map(gameModel -> Game.fromModel(gameModel)).collect(Collectors.toList());
	}
	
	public Game joinGame(UUID gameId, UUID sessionId) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		Optional<GameModel> gameModel = gameRepository.getByGameId(gameId);
		
		if (gameModel.isEmpty()) {
			throw new GameNotFoundException();
		}
		
		if (gameModel.get().getBlackId() != null && gameModel.get().getRedId() != null) {
			throw new CannotJoinGameException();
		} else if (gameModel.get().getBlackId() == null) {
			gameModel.get().setBlackId(sessionModel.get().getAccountId());
		} else {
			gameModel.get().setRedId(sessionModel.get().getAccountId());
		}
		
		gameRepository.save(gameModel.get());
		return Game.fromModel(gameModel.get());
	}
	
	public Game getActiveGame(UUID sessionId) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		Optional<GameModel> activeGameModel = gameRepository.getByAccountId(sessionModel.get().getAccountId());
		
		if (activeGameModel.isEmpty()) {
			throw new NoActiveGameException();
		}
		
		return Game.fromModel(activeGameModel.get());
	}
	
	public Game createGame(UUID sessionId, boolean isBlack) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		Optional<GameModel> activeGameModel = gameRepository.getByAccountId(sessionModel.get().getAccountId());
		
		if (activeGameModel.isPresent()) {
			throw new CannotCreateGameException();
		}
		
		GameModel gameModel = new GameModel();
		gameModel.setActive(false);
		gameModel.setCreated(Timestamp.valueOf(LocalDateTime.now()));
		gameModel.setLastModified(Timestamp.valueOf(LocalDateTime.now()));
		
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
	
	public void forefeitGame(UUID gameId, UUID sessionId) throws CheckersPlusPlusServerException {
		Optional<GameModel> gameModel = gameRepository.findById(gameId);
		
		if (gameModel.isEmpty() || !gameModel.get().isActive()) {
			throw new GameNotFoundException();
		}
		
		Optional<SessionModel> sessionModel = sessionRepository.getBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		UUID accountId = sessionModel.get().getAccountId();
		
		if (gameModel.get().getBlackId().equals(accountId)) {
			gameModel.get().setWinnerId(gameModel.get().getRedId());
		} else if (gameModel.get().getRedId().equals(accountId)) {
			gameModel.get().setWinnerId(gameModel.get().getBlackId());
		} else {
			throw new CheckersPlusPlusServerException("User not found for game.");
		}
		
		gameRepository.save(gameModel.get());
	}

	public Optional<Game> findByGameId(UUID gameId) {
		Optional<GameModel> gameModel = gameRepository.findById(gameId);
		
		if (gameModel.isEmpty()) {
			return Optional.empty();
		}
		
		return Optional.of(Game.fromModel(gameModel.get()));
	}
}
