package com.checklersplusplus.server.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.engine.Coordinate;
import com.checkersplusplus.engine.CoordinatePair;
import com.checkersplusplus.engine.enums.Color;
import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.BotRepository;
import com.checklersplusplus.server.dao.GameEventRepository;
import com.checklersplusplus.server.dao.GameMoveRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.LastMoveSentRepository;
import com.checklersplusplus.server.dao.OpenGameRepository;
import com.checklersplusplus.server.dao.RatingRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.entities.request.Move;
import com.checklersplusplus.server.entities.response.Game;
import com.checklersplusplus.server.enums.GameEvent;
import com.checklersplusplus.server.exception.CannotCancelGameException;
import com.checklersplusplus.server.exception.CannotCreateGameException;
import com.checklersplusplus.server.exception.CannotJoinGameException;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.exception.GameCompleteException;
import com.checklersplusplus.server.exception.GameNotFoundException;
import com.checklersplusplus.server.exception.InvalidMoveException;
import com.checklersplusplus.server.exception.SessionNotFoundException;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.BotModel;
import com.checklersplusplus.server.model.GameEventModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.GameMoveModel;
import com.checklersplusplus.server.model.LastMoveSentModel;
import com.checklersplusplus.server.model.RatingModel;
import com.checklersplusplus.server.model.SessionModel;

@Service
@Transactional
public class GameService {
	
	private static final Logger logger = LoggerFactory.getLogger(GameService.class);
	
	// This is just some dummy value to indicate the game finished in a draw. 
	private static final UUID TIE_GAME_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	@Autowired
	private OpenGameRepository openGameRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private RatingRepository ratingRepository;
	
	@Autowired
	private LastMoveSentRepository lastMoveSentRepository;
	
	@Autowired
	private GameMoveRepository gameMoveRepository;
	
	@Autowired
	private GameEventRepository gameEventRepository;
	
	@Autowired
	private BotRepository botRepository;
	
	@Autowired
	private RatingService ratingService;
	
	public List<Game> getOpenGames(Integer ratingLow, Integer ratingHigh, String sortBy, String sortDirection, Integer page, Integer pageSize) {
		PageRequest pageRequest = null;
	
		if (sortBy != null && sortBy.length() > 0) {
			pageRequest = PageRequest.of(page, pageSize, "asc".equalsIgnoreCase(sortDirection) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
		} else {
			pageRequest = PageRequest.of(page, pageSize);
		}
		
		Page<GameModel> openGames = openGameRepository.findByCreatorRatingBetweenAndActiveTrue(ratingLow, ratingHigh, pageRequest);
		List<Game> games = openGames.stream().map(gameModel -> Game.fromModel(gameModel)).collect(Collectors.toList());
		
		for (Game game : games) {
			if (game.getBlackAccountId() != null) {
				Optional<AccountModel> account = accountRepository.findById(game.getBlackAccountId());
				
				if (account.isEmpty()) {
					logger.error("Failed to find account from given black accountId %s for game %s", game.getBlackAccountId().toString(), game.getGameId().toString());
				} else {
					game.setBlackUsername(account.get().getUsername());
				}
				
				Optional<RatingModel> rating = ratingRepository.findByAccountId(game.getBlackAccountId());
				
				if (rating.isEmpty()) {
					logger.error("Failed to find rating from given black accountId %s for game %s", game.getBlackAccountId().toString(), game.getGameId().toString());
				} else {
					game.setBlackUsername(account.get().getUsername() + "(" + rating.get().getRating() + ")");
				}
				
			}
			
			if (game.getRedAccountId() != null) {
				Optional<AccountModel> account = accountRepository.findById(game.getRedAccountId());
				
				if (account.isEmpty()) {
					logger.error("Failed to find account from given red accountId %s for game %s", game.getRedAccountId().toString(), game.getGameId().toString());
				} else {
					game.setRedUsername(account.get().getUsername());
				}
				
				Optional<RatingModel> rating = ratingRepository.findByAccountId(game.getRedAccountId());
				
				if (rating.isEmpty()) {
					logger.error("Failed to find rating from given black accountId %s for game %s", game.getRedAccountId().toString(), game.getGameId().toString());
				} else {
					game.setRedUsername(account.get().getUsername() + "(" + rating.get().getRating() + ")");
				}
			}
		}
		
		return games;
	}
	
	@Transactional(propagation = Propagation.MANDATORY)
	public void makeLogicalMove(UUID accountId, UUID gameId, List<CoordinatePair> coordinates, boolean isBot) throws InvalidMoveException {
		Optional<GameModel> gameModel = gameRepository.getByGameId(gameId);
		com.checkersplusplus.engine.Game logicalGame = new com.checkersplusplus.engine.Game(gameModel.get().getGameState());
		
		if (logicalGame.isMoveLegal(coordinates)) {
    		logicalGame.doMove(coordinates);
    		
    		Color winner = logicalGame.getWinner();
    		
    		if (winner != null) {
    			UUID winnerId = winner == Color.BLACK ? gameModel.get().getBlackId() : gameModel.get().getRedId();
    			UUID loserId = winner == Color.RED ? gameModel.get().getBlackId() : gameModel.get().getRedId();
    			
    			if (isBot) {
    				Optional<BotModel> bot = botRepository.findByBotAccountId(accountId);
    				bot.get().setInUse(false);
    				botRepository.save(bot.get());
    				
    				if (!winnerId.equals(accountId)) {
    					GameEventModel winnerEvent = new GameEventModel();
    	    			winnerEvent.setActive(true);
    	    			winnerEvent.setEvent(GameEvent.WIN.getMessage());
    	    			winnerEvent.setEventRecipientAccountId(winnerId);
    	    			winnerEvent.setGameId(gameId);
    	    			gameEventRepository.save(winnerEvent);
    				} else {
    					GameEventModel loserEvent = new GameEventModel();
    	    			loserEvent.setActive(true);
    	    			loserEvent.setEvent(GameEvent.LOSE.getMessage() + "|" + logicalGame.getCurrentMove() + "|" + convertCoordinatePairsToString(coordinates));
    	    			loserEvent.setEventRecipientAccountId(loserId);
    	    			loserEvent.setGameId(gameId);
    	    			gameEventRepository.save(loserEvent);
    				}
    			} else {
    				GameEventModel winnerEvent = new GameEventModel();
	    			winnerEvent.setActive(true);
	    			winnerEvent.setEvent(GameEvent.WIN.getMessage());
	    			winnerEvent.setEventRecipientAccountId(winnerId);
	    			winnerEvent.setGameId(gameId);
	    			gameEventRepository.save(winnerEvent);
	    			
	    			GameEventModel loserEvent = new GameEventModel();
	    			loserEvent.setActive(true);
	    			loserEvent.setEvent(GameEvent.LOSE.getMessage() + "|" + logicalGame.getCurrentMove() + "|" + convertCoordinatePairsToString(coordinates));
	    			loserEvent.setEventRecipientAccountId(loserId);
	    			loserEvent.setGameId(gameId);
	    			gameEventRepository.save(loserEvent);
    			}
    			
    			gameModel.get().setActive(false);
    			gameModel.get().setInProgress(false);
    			gameModel.get().setWinnerId(winnerId);
    			logger.info(String.format("GameId: %s   WinnerId: %s", gameId, winnerId));
    		}
    		
    		boolean isDraw = logicalGame.isDraw();
    		
    		if (isDraw) {
    			if (isBot) {
    				Optional<BotModel> bot = botRepository.findByBotAccountId(accountId);
    				bot.get().setInUse(false);
    				botRepository.save(bot.get());
    				
    				if (accountId.equals(gameModel.get().getBlackId())) {
    					GameEventModel redEvent = new GameEventModel();
    	    			redEvent.setActive(true);
    	    			redEvent.setEvent(GameEvent.DRAW.getMessage());
    	    			redEvent.setEventRecipientAccountId(gameModel.get().getRedId());
    	    			redEvent.setGameId(gameId);
    	    			gameEventRepository.save(redEvent);
    				} else {
    					GameEventModel blackEvent = new GameEventModel();
    	    			blackEvent.setActive(true);
    	    			blackEvent.setEvent(GameEvent.DRAW.getMessage());
    	    			blackEvent.setEventRecipientAccountId(gameModel.get().getBlackId());
    	    			blackEvent.setGameId(gameId);
    	    			gameEventRepository.save(blackEvent);
    				}
    			} else {
	    			GameEventModel blackEvent = new GameEventModel();
	    			blackEvent.setActive(true);
	    			blackEvent.setEvent(GameEvent.DRAW.getMessage());
	    			blackEvent.setEventRecipientAccountId(gameModel.get().getBlackId());
	    			blackEvent.setGameId(gameId);
	    			gameEventRepository.save(blackEvent);
	    			
	    			GameEventModel redEvent = new GameEventModel();
	    			redEvent.setActive(true);
	    			redEvent.setEvent(GameEvent.DRAW.getMessage());
	    			redEvent.setEventRecipientAccountId(gameModel.get().getRedId());
	    			redEvent.setGameId(gameId);
	    			gameEventRepository.save(redEvent);
    			}
    			
    			gameModel.get().setActive(false);
    			gameModel.get().setInProgress(false);
    			gameModel.get().setWinnerId(TIE_GAME_ID);
    			logger.info(String.format("GameId: %s   DRAW", gameId));
    		}
    		
    		gameModel.get().setGameState(logicalGame.getGameState());
    		gameModel.get().setLastModified(LocalDateTime.now());
    		gameModel.get().setCurrentMoveNumber(logicalGame.getCurrentMove());
    		gameRepository.save(gameModel.get());
    		
    		GameMoveModel gameMoveModel = new GameMoveModel();
    		gameMoveModel.setAccountId(accountId);
    		gameMoveModel.setGameId(gameId);
    		gameMoveModel.setCreated(LocalDateTime.now());
    		gameMoveModel.setMoveNumber(logicalGame.getCurrentMove());
    		gameMoveModel.setMoveList(convertCoordinatePairsToString(coordinates));
    		gameMoveRepository.save(gameMoveModel);
    		
    		LastMoveSentModel lastMoveSentModel = new LastMoveSentModel();
    		lastMoveSentModel.setGameId(gameId);
    		lastMoveSentModel.setAccountId(accountId);
    		lastMoveSentModel.setLastMoveSent(logicalGame.getCurrentMove());
    		lastMoveSentModel.setCreated(LocalDateTime.now());
    		lastMoveSentRepository.save(lastMoveSentModel);
    	} else {
    		throw new InvalidMoveException();
    	}
	}
	
	public void botMove(UUID accountId, UUID gameId, List<com.checkersplusplus.engine.moves.Move> moves) throws CheckersPlusPlusServerException {
		List<CoordinatePair> coordinates = moves.stream()
    			.map(move -> new CoordinatePair(new Coordinate(move.getStart().getCol(), move.getStart().getRow()), new Coordinate(move.getEnd().getCol(), move.getEnd().getRow())))
    			.collect(Collectors.toList());
		makeLogicalMove(accountId, gameId, coordinates, true);
	}
	
	public Game move(UUID sessionId, UUID gameId, List<Move> moves) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		Optional<GameModel> gameModel = gameRepository.getByGameId(gameId);
		
		if (gameModel.isEmpty() || !gameModel.get().isActive()) {
			throw new GameNotFoundException();
		}
		
		if (!gameModel.get().isInProgress()) {
			throw new GameCompleteException();
		}
		
		if (userNotPlayingGame(sessionModel.get().getAccountId(), gameModel.get())) {
			throw new GameNotFoundException();
		}

    	List<CoordinatePair> coordinates = moves.stream()
    			.map(move -> new CoordinatePair(new Coordinate(move.getStartCol(), move.getStartRow()), new Coordinate(move.getEndCol(), move.getEndRow())))
    			.collect(Collectors.toList());
    	    	
    	makeLogicalMove(sessionModel.get().getAccountId(), gameId, coordinates, false);
    	    	
    	logger.info(String.format("GameId: %s   SessionId: %s    Committed Move: %s", gameId.toString(), sessionId.toString(), convertMoveListToString(moves)));
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
		logger.info(String.format("SessionId: %s   Cancelled game: %s", sessionId.toString(), gameId.toString()));
	}

	public Game joinGame(UUID sessionId, UUID gameId) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		Optional<GameModel> activeGame = gameRepository.getActiveGameByAccountId(sessionModel.get().getAccountId());
		
		if (activeGame.isPresent()) {
			throw new CannotJoinGameException();
		}
		
		Optional<GameModel> gameModel = gameRepository.getByGameId(gameId);
		
		if (gameModel.isEmpty() || !gameModel.get().isActive() || gameModel.get().isInProgress()) {
			throw new GameNotFoundException();
		}
		
		Optional<RatingModel> rating = ratingRepository.findByAccountId(sessionModel.get().getAccountId());
		
		if (rating.isEmpty()) {
			throw new CheckersPlusPlusServerException("Illegal state. Missing rating");
		}
		
		if (gameModel.get().getBlackId() == null) {
			gameModel.get().setBlackId(sessionModel.get().getAccountId());
			gameModel.get().setBlackRating(rating.get().getRating());
			createBeginEvent(gameModel.get().getGameId(), gameModel.get().getRedId());
		} else {
			gameModel.get().setRedId(sessionModel.get().getAccountId());
			gameModel.get().setRedRating(rating.get().getRating());
			createBeginEvent(gameModel.get().getGameId(), gameModel.get().getBlackId());
		}
		
		gameModel.get().setInProgress(true);
		gameRepository.save(gameModel.get());
		logger.info(String.format("SessionId: %s   Joined game: %s", sessionId.toString(), gameId.toString()));
		return Game.fromModel(gameModel.get());
	}
	
	private void createBeginEvent(UUID gameId, UUID opponentId) {
		GameEventModel beginEvent = new GameEventModel();
		beginEvent.setActive(true);
		beginEvent.setEvent(GameEvent.BEGIN.getMessage());
		beginEvent.setEventRecipientAccountId(opponentId);
		beginEvent.setGameId(gameId);
		gameEventRepository.save(beginEvent);
	}
	
	public void botCreateGame(UUID accountId, boolean isBlack) {
		GameModel gameModel = new GameModel();
		gameModel.setActive(true);
		gameModel.setInProgress(false);
		gameModel.setCreated(LocalDate.now());
		gameModel.setLastModified(LocalDateTime.now());
		
		Optional<RatingModel> rating = ratingRepository.findByAccountId(accountId);
		
		if (isBlack) {
			gameModel.setBlackId(accountId);
			gameModel.setBlackRating(rating.get().getRating());
			gameModel.setCreatorRating(rating.get().getRating());
		} else {
			gameModel.setRedId(accountId);
			gameModel.setRedRating(rating.get().getRating());
			gameModel.setCreatorRating(rating.get().getRating());
		}
		
		com.checkersplusplus.engine.Game game = new com.checkersplusplus.engine.Game();
		gameModel.setGameState(game.getGameState());
		gameModel.setCurrentMoveNumber(0);
		gameRepository.save(gameModel);
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
		gameModel.setCreated(LocalDate.now());
		gameModel.setLastModified(LocalDateTime.now());
		
		Optional<RatingModel> rating = ratingRepository.findByAccountId(sessionModel.get().getAccountId());
		
		if (rating.isEmpty()) {
			throw new CheckersPlusPlusServerException("Illegal state. Missing rating");
		}
		
		if (isBlack) {
			gameModel.setBlackId(sessionModel.get().getAccountId());
			gameModel.setBlackRating(rating.get().getRating());
			gameModel.setCreatorRating(rating.get().getRating());
		} else {
			gameModel.setRedId(sessionModel.get().getAccountId());
			gameModel.setRedRating(rating.get().getRating());
			gameModel.setCreatorRating(rating.get().getRating());
		}
		
		com.checkersplusplus.engine.Game game = new com.checkersplusplus.engine.Game();
		gameModel.setGameState(game.getGameState());
		gameModel.setCurrentMoveNumber(0);
		gameRepository.save(gameModel);
		logger.info(String.format("SessionId: %s   Created game: %s", sessionId.toString(), gameModel.getGameId().toString()));
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
		
		UUID opponentId = null;
		
		if (accountId.equals(gameModel.get().getBlackId())) {
			opponentId = gameModel.get().getRedId();
		} else if (accountId.equals(gameModel.get().getRedId())) {
			opponentId = gameModel.get().getBlackId();
		} else {
			throw new CheckersPlusPlusServerException("User not found for game.");
		}
		
		Optional<BotModel> bot = botRepository.findByBotAccountId(opponentId);
		
		if (bot.isEmpty()) {
			GameEventModel forfeitEvent = new GameEventModel();
			forfeitEvent.setActive(true);
			forfeitEvent.setEvent(GameEvent.FORFEIT.getMessage());
			forfeitEvent.setEventRecipientAccountId(opponentId);
			forfeitEvent.setGameId(gameId);
			gameEventRepository.save(forfeitEvent);
		} else {
			bot.get().setInUse(false);
			botRepository.save(bot.get());
		}
		
		gameModel.get().setWinnerId(opponentId);
		gameModel.get().setActive(false);
		gameModel.get().setInProgress(false);
		gameRepository.save(gameModel.get());
		
		ratingService.updatePlayerRatings(gameId);
		logger.info(String.format("SessionId: %s   Forfeited game: %s", sessionId.toString(), gameModel.get().getGameId().toString()));
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
	
	private String convertMoveListToString(List<Move> moves) {
		StringBuilder sb = new StringBuilder();
		
		for (Move move : moves) {
			sb.append(String.format("c:%d,r:%d-c:%d,r:%d+", move.getStartCol(), move.getStartRow(), move.getEndCol(), move.getEndRow()));
		}
		
		return sb.toString();
	}
	
	private String convertCoordinatePairsToString(List<CoordinatePair> pairs) {
		StringBuilder sb = new StringBuilder();
		
		for (CoordinatePair move : pairs) {
			sb.append(String.format("c:%d,r:%d-c:%d,r:%d+", move.getStart().getCol(), move.getStart().getRow(), move.getEnd().getCol(), move.getEnd().getRow()));
		}
		
		return sb.toString();
	}
}
