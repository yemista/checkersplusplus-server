package com.checklersplusplus.server.enums;

public enum GameEvent {
	FORFEIT("FORFEIT"),
	BEGIN("BEGIN"),
	LOSE("LOSE"),
	WIN("WIN"),
	DRAW("DRAW"),
	TIMEOUT("TIMEOUT");
	
	private String message;
	
	GameEvent(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
