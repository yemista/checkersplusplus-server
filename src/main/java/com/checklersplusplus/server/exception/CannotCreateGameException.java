package com.checklersplusplus.server.exception;

public class CannotCreateGameException extends CheckersPlusPlusServerException {

	public CannotCreateGameException() {
		super("Cannot create a game because user is currently in a game.");
	}

}
