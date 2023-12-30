package com.checklersplusplus.server.enums;

public enum GameEvent {
	FORFEIT("FORFEIT"),
	BEGIN("BEGIN"),
	LOSE("LOSE"),
	WIN("WIN"),
	DRAW("DRAW"),
	TIMEOUT("TIMEOUT"),
	TIMEOUT_LOSS("TIMEOUT_LOSS");
	
	private String message;
	
	GameEvent(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
