package com.checklersplusplus.server.entities;

import java.io.Serializable;
import java.util.UUID;

import com.checklersplusplus.server.model.GameModel;

public class Game implements Serializable {
	private UUID gameId;
	
	public Game(UUID gameId) {
		this.gameId = gameId;
	}
	
	public UUID getGameId() {
		return gameId;
	}

	public static Game fromModel(GameModel gameModel) {
		return new Game(gameModel.getGameId());
	}
}
