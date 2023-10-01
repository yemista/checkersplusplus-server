package com.checklersplusplus.server.entities;

import java.io.Serializable;
import java.util.UUID;

import com.checklersplusplus.server.model.GameModel;

public class Game implements Serializable {
	private UUID gameId;
	private String gameState;
	private boolean redSeated;
	private boolean blackSeated;
	
	private Game(UUID gameId, String gameState, boolean blackSeated, boolean redSeated) {
		this.gameId = gameId;
		this.gameState = gameState;
		this.blackSeated = blackSeated;
		this.redSeated = redSeated;
	}
	
	public UUID getGameId() {
		return gameId;
	}
	
	public String getGameState() {
		return gameState;
	}

	public static Game fromModel(GameModel gameModel) {
		return new Game(gameModel.getGameId(), gameModel.getGameState(), gameModel.getBlackId() != null, gameModel.getRedId() != null);
	}

	public void setGameState(String boardState) {
		this.gameState = boardState;
	}

	public boolean isRedSeated() {
		return redSeated;
	}

	public boolean isBlackSeated() {
		return blackSeated;
	}
}
