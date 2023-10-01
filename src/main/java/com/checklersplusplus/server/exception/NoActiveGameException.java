package com.checklersplusplus.server.exception;

public class NoActiveGameException extends CheckersPlusPlusServerException {

	public NoActiveGameException() {
		super("No games currenntly active for user.");
	}

}
