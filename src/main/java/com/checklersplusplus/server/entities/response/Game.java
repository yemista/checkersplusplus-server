package com.checklersplusplus.server.entities.response;

import java.io.Serializable;
import java.util.UUID;

import com.checklersplusplus.server.model.GameModel;

public class Game extends CheckersPlusPlusResponse implements Serializable {
	private UUID gameId;
	private String gameState;
	private UUID redAccountId;
	private UUID blackAccountId;
	
	public Game(UUID gameId, String gameState, UUID blackAccountId, UUID redAccountId) {
		this.gameId = gameId;
		this.gameState = gameState;
		this.blackAccountId = blackAccountId;
		this.redAccountId = redAccountId;
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
		return new Game(gameModel.getGameId(), gameModel.getGameState(), gameModel.getBlackId(), gameModel.getRedId());
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
	
	
}
