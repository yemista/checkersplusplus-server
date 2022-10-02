package com.checkersplusplus.service.enums;

public enum GameStatus {
	PENDING("PENDING"),
	ABORTED("ABORTED"),
	RUNNING("RUNNING"),
	COMPLETE("COMPLETE"),
	CANCELED("CANCELED"),
	FORFEIT("FORFEIT"),
	UNKNOWN("UNKNOWN");
	
	private String strVal;
	
	GameStatus(String strVal) {
		this.strVal = strVal;
	}
	
	@Override
	public String toString() {
		return strVal;
	}

	public static GameStatus getEnum(String status) {
		switch (status) {
		case "PENDING":
			return PENDING;
		case "ABORTED":
			return ABORTED;
		case "RUNNING":
			return RUNNING;
		case "COMPLETE":
			return COMPLETE;
		case "CANCELED":
			return CANCELED;
		case "FORFEIT":
			return FORFEIT;
			default:
				return UNKNOWN;
		}
	}
}
