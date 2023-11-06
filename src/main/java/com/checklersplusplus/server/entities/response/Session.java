package com.checklersplusplus.server.entities.response;

import java.util.UUID;

public class Session extends CheckersPlusPlusResponse {
	private UUID sessionId;
	private UUID gameId;
	
	// TODO fill this out
	private UUID accountId;
	
	public Session() {
	}
	
	public Session(UUID sessionId, UUID gameId) {
		this.gameId = gameId;
		this.sessionId = sessionId;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public UUID getGameId() {
		return gameId;
	}

	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}
	
	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}
}
