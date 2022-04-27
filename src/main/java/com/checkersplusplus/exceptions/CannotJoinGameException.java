package com.checkersplusplus.exceptions;

public class CannotJoinGameException extends CheckersPlusPlusException {

	public CannotJoinGameException() {
		super("CheckersPlusPlusException", ErrorCodes.CANNOT_JOIN_GAME);
	}

}
