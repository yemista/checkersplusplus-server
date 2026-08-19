package com.checklersplusplus.server.entities.response;

import java.io.Serializable;
import java.util.UUID;

import com.checklersplusplus.server.enums.GameResult;
import com.checklersplusplus.server.enums.GameStatus;
import com.checklersplusplus.server.model.GameModel;

public class Game extends CheckersPlusPlusResponse implements Serializable {

	private static final long serialVersionUID = 5106934896447291913L;

	/**
	 * Sentinel stored in game.winner_id when a game ends in a draw. Clients should read
	 * {@link #getResult()} rather than comparing against this value directly.
	 */
	public static final UUID DRAW_WINNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	private UUID gameId;
	private String gameState;
	private UUID redAccountId;
	private UUID blackAccountId;
	private String blackUsername;
	private String redUsername;
	private UUID currentTurnId;

	// Completion state. These exist so that a polling client can tell a game is over
	// without depending on a pushed event.
	private boolean active;
	private boolean inProgress;
	private UUID winnerId;
	private Integer currentMoveNumber;
	private GameStatus status;
	private GameResult result;

	public Game(UUID gameId, String gameState, UUID blackAccountId, UUID redAccountId, UUID currentTurnId) {
		this.gameId = gameId;
		this.gameState = gameState;
		this.blackAccountId = blackAccountId;
		this.redAccountId = redAccountId;
		this.currentTurnId = currentTurnId;
	}

	public Game() {
	}

	public static Game fromModel(GameModel gameModel) {
		UUID currentTurnId = null;
		String[] gameStateParts = gameModel.getGameState().split("\\|");

		if (gameStateParts.length > 1) {
			int currentTurn = Integer.parseInt(gameStateParts[1]);
			currentTurnId = currentTurn % 2 == 0 ? gameModel.getBlackId() : gameModel.getRedId();
		} else {
			currentTurnId = gameModel.getBlackId();
		}

		Game game = new Game(gameModel.getGameId(), gameModel.getGameState(), gameModel.getBlackId(),
				gameModel.getRedId(), currentTurnId);

		game.setActive(gameModel.isActive());
		game.setInProgress(gameModel.isInProgress());
		game.setWinnerId(gameModel.getWinnerId());
		game.setCurrentMoveNumber(gameModel.getCurrentMoveNumber());
		game.setStatus(deriveStatus(gameModel));
		game.setResult(deriveResult(gameModel));

		return game;
	}

	private static GameStatus deriveStatus(GameModel gameModel) {
		if (gameModel.isActive()) {
			return gameModel.isInProgress() ? GameStatus.IN_PROGRESS : GameStatus.WAITING_FOR_OPPONENT;
		}

		// Inactive with no winner recorded means it was withdrawn before it ever started.
		return gameModel.getWinnerId() == null ? GameStatus.CANCELED : GameStatus.COMPLETE;
	}

	private static GameResult deriveResult(GameModel gameModel) {
		UUID winnerId = gameModel.getWinnerId();

		if (winnerId == null) {
			return null;
		}
		if (DRAW_WINNER_ID.equals(winnerId)) {
			return GameResult.DRAW;
		}
		if (winnerId.equals(gameModel.getBlackId())) {
			return GameResult.BLACK_WON;
		}
		if (winnerId.equals(gameModel.getRedId())) {
			return GameResult.RED_WON;
		}

		return null;
	}

	public UUID getGameId() {
		return gameId;
	}

	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}

	public String getGameState() {
		return gameState;
	}

	public void setGameState(String boardState) {
		this.gameState = boardState;
	}

	public UUID getRedId() {
		return redAccountId;
	}

	public UUID getBlackId() {
		return blackAccountId;
	}

	public UUID getRedAccountId() {
		return redAccountId;
	}

	public void setRedAccountId(UUID redAccountId) {
		this.redAccountId = redAccountId;
	}

	public UUID getBlackAccountId() {
		return blackAccountId;
	}

	public void setBlackAccountId(UUID blackAccountId) {
		this.blackAccountId = blackAccountId;
	}

	public String getBlackUsername() {
		return blackUsername;
	}

	public void setBlackUsername(String blackUsername) {
		this.blackUsername = blackUsername;
	}

	public String getRedUsername() {
		return redUsername;
	}

	public void setRedUsername(String redUsername) {
		this.redUsername = redUsername;
	}

	public UUID getCurrentTurnId() {
		return currentTurnId;
	}

	public void setCurrentTurnId(UUID currentTurnId) {
		this.currentTurnId = currentTurnId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}

	public UUID getWinnerId() {
		return winnerId;
	}

	public void setWinnerId(UUID winnerId) {
		this.winnerId = winnerId;
	}

	public Integer getCurrentMoveNumber() {
		return currentMoveNumber;
	}

	public void setCurrentMoveNumber(Integer currentMoveNumber) {
		this.currentMoveNumber = currentMoveNumber;
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}

	public GameResult getResult() {
		return result;
	}

	public void setResult(GameResult result) {
		this.result = result;
	}
}
