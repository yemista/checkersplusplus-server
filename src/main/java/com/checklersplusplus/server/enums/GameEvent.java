package com.checklersplusplus.server.enums;

public enum GameEvent {
	FORFEIT("FORFEIT"),
	BEGIN("BEGIN"),
	LOSE("LOSE"),
	TIMEOUT("TIMEOUT");
	
	private String message;
	
	GameEvent(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
