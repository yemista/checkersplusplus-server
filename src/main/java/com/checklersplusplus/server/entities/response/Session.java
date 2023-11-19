package com.checklersplusplus.server.entities.response;

import java.io.Serializable;
import java.util.UUID;

public class Session extends CheckersPlusPlusResponse implements Serializable {
	private static final long serialVersionUID = 8690591879893264238L;

	private UUID sessionId;
	private UUID gameId;
	private UUID accountId;
	
	public Session() {
	}
	
	public Session(UUID sessionId, UUID gameId, UUID accountId) {
		this.gameId = gameId;
		this.sessionId = sessionId;
		this.accountId = accountId;
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

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}
}
