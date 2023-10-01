package com.checklersplusplus.server.exception;

public class GameNotFoundException extends CheckersPlusPlusServerException {

	public GameNotFoundException() {
		super("Game not found.");
	}

}
