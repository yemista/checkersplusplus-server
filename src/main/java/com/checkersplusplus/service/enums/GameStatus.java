package com.checkersplusplus.service.enums;

public enum GameStatus {
	PENDING("PENDING"),
	ABORTED("ABORTED"),
	RUNNING("RUNNING"),
	COMPLETE("COMPLETE"),
	CANCELED("CANCELED");
	
	private String strVal;
	
	GameStatus(String strVal) {
		this.strVal = strVal;
	}
	
	@Override
	public String toString() {
		return strVal;
	}
}
