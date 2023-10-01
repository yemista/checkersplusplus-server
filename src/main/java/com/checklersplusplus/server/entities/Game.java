package com.checklersplusplus.server.entities;

import java.io.Serializable;
import java.util.UUID;

import com.checklersplusplus.server.model.GameModel;

public class Game implements Serializable {
	private UUID gameId;
	private String gameState;
	
	public Game(UUID gameId, String gameState) {
		this.gameId = gameId;
		this.gameState = gameState;
	}
	
	public UUID getGameId() {
		return gameId;
	}
	
	public String getGameState() {
		return gameState;
	}

	public static Game fromModel(GameModel gameModel) {
		return new Game(gameModel.getGameId(), gameModel.getGameState());
	}

	public void setGameState(String boardState) {
		this.gameState = boardState;
	}
}
