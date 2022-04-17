package com.checkersplusplus.service.models;

public class ActiveGame {
	private String userId;
	private String gameId;
	
	public ActiveGame(String userId, String gameId) {
		super();
		this.userId = userId;
		this.gameId = gameId;
	}

	public String getUserId() {
		return userId;
	}

	public String getGameId() {
		return gameId;
	}
}
