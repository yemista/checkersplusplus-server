package com.checklersplusplus.server.exception;

public class CannotJoinGameException extends CheckersPlusPlusServerException {

	public CannotJoinGameException() {
		super("Failed to join game.");
	}

}
