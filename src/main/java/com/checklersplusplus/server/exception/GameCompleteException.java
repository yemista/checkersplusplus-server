package com.checklersplusplus.server.exception;

public class GameCompleteException extends CheckersPlusPlusServerException {

	public GameCompleteException() {
		super("Game finished.");
	}

}
