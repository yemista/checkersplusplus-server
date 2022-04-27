package com.checkersplusplus.exceptions;

public class SessionExpiredException extends CheckersPlusPlusException {

	public SessionExpiredException() {
		super("CheckersPlusPlusException", ErrorCodes.SESSION_EXPIRED);
	}
	
	public SessionExpiredException(String message) {
		super(message, ErrorCodes.SESSION_EXPIRED);
	}

	
}
