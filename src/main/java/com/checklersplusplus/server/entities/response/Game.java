package com.checklersplusplus.server.entities.response;

import java.io.Serializable;
import java.util.UUID;

import com.checklersplusplus.server.model.GameModel;

public class Game extends CheckersPlusPlusResponse implements Serializable {
	private UUID gameId;
	private String gameState;
	private UUID redAccountId;
	private UUID blackAccountId;
	private String blackUsername;
	private String redUsername;
	private UUID currentTurnId;
	
	public Game(UUID gameId, String gameState, UUID blackAccountId, UUID redAccountId, UUID currentTurnId) {
		this.gameId = gameId;
		this.gameState = gameState;
		this.blackAccountId = blackAccountId;
		this.redAccountId = redAccountId;
		this.currentTurnId = currentTurnId;
	}
	
	public Game() {
	}

	public UUID getGameId() {
		return gameId;
	}
	
	public String getGameState() {
		return gameState;
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
		
		return new Game(gameModel.getGameId(), gameModel.getGameState(), gameModel.getBlackId(), gameModel.getRedId(), currentTurnId);
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

	public void setGameId(UUID gameId) {
		this.gameId = gameId;
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
	
}
