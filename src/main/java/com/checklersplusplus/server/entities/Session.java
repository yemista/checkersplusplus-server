package com.checklersplusplus.server.entities;

import java.util.UUID;

public class Session {
	private UUID sessionId;
	private UUID gameId;
	private String message;
	
	public Session() {
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public UUID getGameId() {
		return gameId;
	}
	
	public String getMessage() {
		return message;
	}

	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}
}
