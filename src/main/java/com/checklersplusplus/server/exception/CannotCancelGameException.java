package com.checklersplusplus.server.exception;

public class CannotCancelGameException extends CheckersPlusPlusServerException {

	public CannotCancelGameException() {
		super("Cannot cancel game because it has started.");
	}

}
